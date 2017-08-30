package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.fragments.PersistentFragment;
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

		mViewPager = root.findViewById( R.id.maps_pager );
		mViewPager.setAdapter( new ViewPagerAdapter( getFragmentManager(), isRefresh ) );
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
				for ( int i = 0; i < mTwoWayView.getChildCount(); i++ )
					mTwoWayView.getChildAt( i ).setBackgroundColor( 0x00000000 );
				if ( mTwoWayView.getChildAt( position ) != null )
					mTwoWayView.getChildAt( position ).setBackgroundColor( 0xFFff4081 );
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
			return handler.maps.size();
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
			tv.setText( handler.maps.get( position ).title );
			tv.setPadding( 12, 6, 12, 6 );
			tv.setTextColor( 0xffffffff );
			tv.setGravity( Gravity.CENTER );
			tv.setLayoutParams( new TwoWayView.LayoutParams( ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT ) );

			if ( position == selectedPosition )
				tv.setBackgroundColor( 0xFFff4081 );

			tv.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					ViewGroup parent = ( ViewGroup ) v.getParent();
					for ( int i = 0; i < parent.getChildCount(); i++ )
						parent.getChildAt( i ).setBackgroundColor( 0x00000000 );

					v.setBackgroundColor( 0xFFff4081 );

					mViewPager.setCurrentItem( position );

					if ( mViewPager.getChildAt( position ) != null )
						( ( TouchImageView ) mViewPager.getChildAt( position ).findViewById( R.id.maps_image ) ).resetZoom();
				}
			} );

			tv.setOnLongClickListener( new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick( View v )
				{

					refreshState();

					Toast.makeText( getContext(), "Maps Refreshed", Toast.LENGTH_LONG ).show();

					return true;
				}
			} );

			return tv;
		}
	}

	public class ViewPagerAdapter extends FragmentStatePagerAdapter
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
			return handler.maps.size();
		}

		@Override
		public Fragment getItem( int position )
		{
			MapsChildFragment.maps = handler.maps;

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
