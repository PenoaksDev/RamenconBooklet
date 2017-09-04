package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.Objs;
import io.amelia.android.ui.widget.TouchImageView;
import io.amelia.booklet.data.ContentFragment;
import io.amelia.booklet.data.GuideHandler;
import io.amelia.booklet.data.GuidePageModel;

public class GuideFragment extends ContentFragment<GuideHandler> implements PersistentFragment
{
	private static GuideFragment instance = null;

	public static GuideFragment instance()
	{
		return instance;
	}

	private TwoWayView mTwoWayView;
	private ViewPager mViewPager;
	private Bundle savedState = null;
	private int selectedPosition = 0;

	public GuideFragment() throws IOException
	{
		super( GuideHandler.class );
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

		getActivity().setTitle( "Official Guide" );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_guide, container, false );
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

		mTwoWayView = root.findViewById( R.id.guide_listview );
		mTwoWayView.setAdapter( new PagedListAdapter() );
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
						child.setBackgroundColor( 0xFFff4081 );
				}

				// PLog.i( "Scroll Notice: " + firstVisibleItem + " // " + visibleItemCount + " // " + totalItemCount + " // " + selectedPosition );
			}

			@Override
			public void onScrollStateChanged( TwoWayView view, int scrollState )
			{

			}
		} );

		mViewPager = root.findViewById( R.id.guide_pager );
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
					ACRAHelper.handleExceptionOnce( "guide-booklet-page-scroll", e );
					mTwoWayView.setSelection( position );
				}

				if ( mViewPager.getChildAt( position ) != null )
					( ( TouchImageView ) mViewPager.getChildAt( position ).findViewById( R.id.guide_image ) ).resetZoom();
			}
		} );
	}

	public class PagedListAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return handler.guidePageModels.size();
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
			TextView guidePageLabel = new TextView( getContext() );
			GuidePageModel model = handler.guidePageModels.get( position );
			guidePageLabel.setText( Objs.isEmpty( model.title ) ? model.pageNo : model.title );
			guidePageLabel.setPadding( 12, 6, 12, 6 );
			guidePageLabel.setTextColor( 0xffffffff );
			guidePageLabel.setGravity( Gravity.CENTER );
			guidePageLabel.setLayoutParams( new TwoWayView.LayoutParams( ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT ) );

			if ( position == selectedPosition )
				guidePageLabel.setBackgroundColor( 0xFFff4081 );

			guidePageLabel.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					mViewPager.setCurrentItem( position );
				}
			} );

			return guidePageLabel;
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
			return handler.guidePageModels.size();
		}

		@Override
		public Fragment getItem( int position )
		{
			Bundle bundle = new Bundle();
			bundle.putInt( "index", position );
			bundle.putBoolean( "isRefresh", isRefresh( position ) );

			GuideChildFragment frag = new GuideChildFragment();
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
