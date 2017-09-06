package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.fragments.PersistentFragment;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.android.ui.widget.TouchImageView;
import io.amelia.booklet.data.ContentFragment;
import io.amelia.booklet.data.MapsHandler;

public class MapsFragment extends ContentFragment<MapsHandler> implements PersistentFragment
{
	private static MapsFragment instance = null;

	public static MapsFragment instance()
	{
		return instance;
	}

	private TwoWayView mTwoWayView;
	private ViewPager mViewPager;
	private Bundle savedState = null;
	private int selectedPosition = 0;

	public MapsFragment() throws IOException
	{
		super( MapsHandler.class );
		instance = this;
	}

	@Override
	public void loadState( Bundle bundle )
	{
		this.savedState = bundle;
	}

	@Override
	public void onCreate( @Nullable Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getActivity().setTitle( "Maps and Locations" );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_maps, container, false );
	}

	@Override
	public void refreshState()
	{
		savedState = new Bundle();
		saveState( savedState );
	}

	@Override
	public void saveState( Bundle bundle )
	{
		if ( getView() == null )
			return;

		bundle.putInt( "selectedPosition", mViewPager == null ? 0 : mViewPager.getCurrentItem() );
	}

	@Override
	public void sectionHandle( BoundData data, boolean isRefresh )
	{
		View root = getView();

		if ( savedState != null )
			selectedPosition = savedState.getInt( "selectedPosition", 0 );

		mTwoWayView = root.findViewById( R.id.maps_listview );
		mTwoWayView.setAdapter( new PagedListAdapater() );
		mTwoWayView.setOnScrollListener( new TwoWayView.OnScrollListener()
		{
			@Override
			public void onScroll( TwoWayView view, int firstVisibleItem, int visibleItemCount, int totalItemCount )
			{
				if ( visibleItemCount == 0 )
					return;

				for ( int i = 0; i < mTwoWayView.getChildCount(); i++ )
					mTwoWayView.getChildAt( i ).setBackgroundColor( 0x00000000 );

				if ( selectedPosition >= firstVisibleItem && selectedPosition < firstVisibleItem + visibleItemCount )
				{
					View child = view.getChildAt( selectedPosition - firstVisibleItem );
					if ( child != null )
						child.setBackgroundColor( ResourcesCompat.getColor( getResources(), R.color.colorAccent, null ) );
				}

				// PLog.i( "Scroll Notice: " + firstVisibleItem + " // " + visibleItemCount + " // " + totalItemCount + " // " + selectedPosition );
			}

			@Override
			public void onScrollStateChanged( TwoWayView view, int scrollState )
			{

			}
		} );

		mViewPager = root.findViewById( R.id.maps_pager );
		mViewPager.setAdapter( new ViewPagerAdapter( getChildFragmentManager(), isRefresh ) );
		mViewPager.setCurrentItem( selectedPosition );
		mViewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrollStateChanged( int state )
			{

			}

			@Override
			public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels )
			{

			}

			@Override
			public void onPageSelected( int position )
			{
				selectedPosition = position;

				try
				{
					int middle = ( mTwoWayView.getChildCount() / 2 ) - 1;

					if ( selectedPosition < middle )
						mTwoWayView.setSelection( 0 );
					else
						mTwoWayView.setSelection( position - middle );
				}
				catch ( Exception e )
				{
					ExceptionHelper.handleExceptionOnce( "maps-page-scroll", e );
					mTwoWayView.setSelection( position );
				}

				if ( mViewPager.getChildAt( position ) != null )
					( ( TouchImageView ) mViewPager.getChildAt( position ).findViewById( R.id.maps_image ) ).resetZoom();
			}
		} );
	}

	public class PagedListAdapater extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return handler.mapsMapModels.size();
		}

		@Override
		public Object getItem( int position )
		{
			return position;
		}

		@Override
		public long getItemId( int position )
		{
			return position;
		}

		@Override
		public View getView( final int position, View convertView, ViewGroup parent )
		{
			TextView tv = new TextView( getContext() );
			tv.setText( handler.mapsMapModels.get( position ).title );
			tv.setPadding( 12, 6, 12, 6 );
			tv.setTextColor( 0xffffffff );
			tv.setGravity( Gravity.CENTER );
			tv.setLayoutParams( new TwoWayView.LayoutParams( ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT ) );

			if ( position == selectedPosition )
				tv.setBackgroundColor( ResourcesCompat.getColor( getResources(), R.color.colorAccent, null ) );

			tv.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					ViewGroup parent = ( ViewGroup ) v.getParent();
					for ( int i = 0; i < parent.getChildCount(); i++ )
						parent.getChildAt( i ).setBackgroundColor( 0x00000000 );

					v.setBackgroundColor( ResourcesCompat.getColor( getResources(), R.color.colorAccent, null ) );

					mViewPager.setCurrentItem( position );

					if ( mViewPager.getChildAt( position ) != null )
						( ( TouchImageView ) mViewPager.getChildAt( position ).findViewById( R.id.maps_image ) ).resetZoom();
				}
			} );

			return tv;
		}
	}

	public class ViewPagerAdapter extends FragmentPagerAdapter
	{
		private final List<Integer> refreshed = new ArrayList<>();
		private boolean isRefresh;

		public ViewPagerAdapter( FragmentManager fm, boolean isRefresh )
		{
			super( fm );
			this.isRefresh = isRefresh;
		}

		@Override
		public int getCount()
		{
			return handler.mapsMapModels.size();
		}

		@Override
		public Fragment getItem( int position )
		{
			Bundle bundle = new Bundle();
			bundle.putInt( "index", position );
			bundle.putBoolean( "isRefresh", isRefresh( position ) );

			MapsChildFragment frag = new MapsChildFragment();
			frag.setArguments( bundle );
			return frag;
		}

		public boolean isRefresh( int position )
		{
			if ( refreshed.contains( position ) )
				return false;

			refreshed.add( position );
			return isRefresh;
		}
	}
}
