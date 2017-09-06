package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.log.PLog;
import io.amelia.android.support.UIUpdater;
import io.amelia.android.support.WaitFor;
import io.amelia.booklet.data.Booklet;
import io.amelia.booklet.data.BookletAdapter;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.ui.activity.BootActivity;

public class DownloadFragment extends Fragment implements UIUpdater.Receiver
{
	public static final int UPDATE_BOOKLETS = 0x10;
	public static final int SET_BOOKLET_INUSE = 0x11;
	public static final int UPDATE_PROGRESS_BAR = 0x12;
	public static final int COMPUTE_VISIBILITIES = 0x13;

	private static DownloadFragment instance;

	public static DownloadFragment instance()
	{
		return instance;
	}

	private ExpandableListView bookletListview;
	private SwipeRefreshLayout refreshLayout;

	public DownloadFragment()
	{
		instance = this;
	}

	public BookletAdapter getBookletAdapter()
	{
		return ( BookletAdapter ) bookletListview.getExpandableListAdapter();
	}

	private BootActivity getBootActivity()
	{
		return ( BootActivity ) getActivity();
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setHasOptionsMenu( true );
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
	{
		inflater.inflate( R.menu.default_options_menu, menu );

		menu.findItem( R.id.options_settings ).setIcon( new IconDrawable( getActivity(), FontAwesomeIcons.fa_gear ).colorRes( R.color.colorWhite ).actionBarSize() );
		menu.findItem( R.id.options_refresh ).setIcon( new IconDrawable( getActivity(), FontAwesomeIcons.fa_refresh ).colorRes( R.color.colorWhite ).actionBarSize() );
	}

	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		View root = inflater.inflate( R.layout.fragment_download, container, false );
		getActivity().setTitle( "Ramencon Booklet" );
		bookletListview = root.findViewById( R.id.bootlet_listview );
		return root;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch ( item.getItemId() )
		{
			case R.id.options_settings:
				( ( BootActivity ) getActivity() ).stacker.setFragment( SettingsFragment.class, true );
				return true;
			case R.id.options_refresh:
				ContentManager.refreshBooklets();

				Animation animation = new RotateAnimation( 0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );
				animation.setDuration( 1000 );
				animation.setRepeatCount( Animation.INFINITE );
				animation.setAnimationListener( new Animation.AnimationListener()
				{
					@Override
					public void onAnimationEnd( Animation animation )
					{

					}

					@Override
					public void onAnimationRepeat( Animation animation )
					{
						if ( !ContentManager.isBookletsRefreshing() )
						{
							item.getActionView().clearAnimation();
							item.setActionView( null );

							uiUpdateBooklets();
						}
					}

					@Override
					public void onAnimationStart( Animation animation )
					{

					}
				} );

				ImageView iv = new ImageView( getContext(), null, R.style.Widget_AppCompat_ActionButton );
				iv.setImageDrawable( new IconDrawable( getActivity(), FontAwesomeIcons.fa_refresh ).colorRes( R.color.colorWhite ).actionBarSize() );
				// iv.setImageResource( R.drawable.ic_options_refresh );
				iv.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
				float dpScale = getResources().getDisplayMetrics().density;
				iv.setPadding( ( int ) ( 14 * dpScale * 0.5f ), 0, ( int ) ( 14 * dpScale * 0.5f ), 0 );

				iv.setOnClickListener( new View.OnClickListener()
				{
					@Override
					public void onClick( View view )
					{
						Toast.makeText( getContext(), "Hold Your Horses!", Toast.LENGTH_SHORT ).show();
					}
				} );

				iv.startAnimation( animation );
				item.setActionView( iv );
				return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onStart()
	{
		super.onStart();

		ContentManager.refreshBooklets();
		getBootActivity().uiShowProgressBar( null, null );

		new WaitFor( new WaitFor.Task()
		{
			@Override
			public boolean isReady()
			{
				return !ContentManager.isBookletsRefreshing();
			}

			@Override
			public void onFinish()
			{
				getBootActivity().uiHideProgressBar();
				updateBookletsView();
			}

			@Override
			public void onTimeout()
			{
				getBootActivity().uiHideProgressBar();
				updateBookletsView();
			}
		}, 10000 );
	}

	@Override
	public void processUpdate( int type, BoundData data )
	{
		if ( type == UPDATE_BOOKLETS )
			updateBookletsView();
		if ( getBookletAdapter() != null )
		{
			if ( type == SET_BOOKLET_INUSE )
			{
				getBookletAdapter().showProgressBar( data.getString( "bookletId" ), data.getBoolean( "inUse" ) );
				getBookletAdapter().computeVisibilities( data.getString( "bookletId" ) );
			}
			if ( type == UPDATE_PROGRESS_BAR )
				getBookletAdapter().updateProgressBar( data.getString( "bookletId" ), data.getInt( "progressValue" ), data.getInt( "progressMax" ), data.getBoolean( "isDone" ) );
			if ( type == COMPUTE_VISIBILITIES )
				getBookletAdapter().computeVisibilities( data.getString( "bookletId" ) );
		}
	}

	public void uiComputeVisibilities( String bookletId )
	{
		if ( getBootActivity() == null )
			return;
		BoundData data = new BoundData();
		data.put( "bookletId", bookletId );
		getBootActivity().putUIUpdate( COMPUTE_VISIBILITIES, data );
	}

	public void uiSetBookletInUse( String bookletId, boolean inUse )
	{
		if ( getBootActivity() == null )
			return;
		BoundData data = new BoundData();
		data.put( "bookletId", bookletId );
		data.put( "inUse", inUse );
		getBootActivity().putUIUpdate( SET_BOOKLET_INUSE, data );
	}

	public void uiUpdateBooklets()
	{
		if ( getBootActivity() == null )
			return;
		getBootActivity().putUIUpdate( UPDATE_BOOKLETS, null );
	}

	public void uiUpdateProgressBar( String bookletId, int progressValue, int progressMax, boolean isDone )
	{
		if ( getBootActivity() == null )
			return;
		if ( progressMax < progressValue )
			progressMax = progressValue;

		BoundData data = new BoundData();
		data.put( "bookletId", bookletId );
		data.put( "progressValue", progressValue );
		data.put( "progressMax", progressMax );
		data.put( "isDone", isDone );
		getBootActivity().putUIUpdate( UPDATE_PROGRESS_BAR, data );
	}

	private void updateBookletsView()
	{
		bookletListview.setAdapter( new BookletAdapter( bookletListview, Booklet.getBooklets() ) );

		// First Run
		if ( !ContentManager.hasActiveBooklet() )
		{
			Booklet booklet = ContentManager.getLatestBooklet();
			PLog.i( "Latest Booklet: " + booklet );
			if ( booklet != null )
				booklet.goDownloadAndOpen();
		}
	}
}
