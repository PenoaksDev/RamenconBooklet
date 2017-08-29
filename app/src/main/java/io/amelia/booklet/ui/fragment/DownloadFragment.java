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

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.log.PLog;
import io.amelia.android.support.UIUpdater;
import io.amelia.booklet.Booklet;
import io.amelia.booklet.ContentManager;
import io.amelia.booklet.data.BookletAdapter;
import io.amelia.booklet.ui.activity.BootActivity;

public class DownloadFragment extends Fragment implements UIUpdater.Receiver
{
	public static final int UPDATE_BOOKLETS = 0x03;

	public static DownloadFragment instance()
	{
		return new DownloadFragment();
	}

	private ExpandableListView bookletListview;
	private SwipeRefreshLayout refreshLayout;

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
	}

	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		View root = inflater.inflate( R.layout.fragment_download, container, false );

		getActivity().setTitle( "Ramencon Booklet" );

		/* FloatingActionButton settingsFab = ( FloatingActionButton ) root.findViewById( R.id.settings_fab );
		settingsFab.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				( ( BootActivity ) getActivity() ).stacker.setFragment( SettingsFragment.class, true );
			}
		} ); */

		refreshLayout = root.findViewById( R.id.booklet_refresher );
		refreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				ContentManager.refreshBooklets();

				refreshLayout.setRefreshing( false );
				// TODO Make this stay on screen until finished!
			}
		} );

		bookletListview = ( ExpandableListView ) root.findViewById( R.id.bootlet_listview );

		return root;
	}

	public void onListItemClick( Booklet booklet )
	{
		// ( ( ContentActivity ) getActivity() ).stacker.setFragment( BookletFragment.instance( booklet.bookletId ), true );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		PLog.i( "Option Item Click: " + item.getTitle() + " // " + item.getItemId() );

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
						}
					}

					@Override
					public void onAnimationStart( Animation animation )
					{

					}
				} );

				ImageView iv = new ImageView( getContext(), null, R.style.Widget_AppCompat_ActionButton );
				iv.setImageResource( R.drawable.ic_options_refresh );
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

		PLog.i( "Starting DownloadFragment" );

		ContentManager.refreshBooklets();
		updateBookletsView();
	}

	@Override
	public void processUpdate( int type, BoundData data )
	{
		if ( type == DownloadFragment.UPDATE_BOOKLETS )
			updateBookletsView();
	}

	public void updateBooklets()
	{
		getBootActivity().putUIUpdate( UPDATE_BOOKLETS, null );
	}

	private void updateBookletsView()
	{
		bookletListview.setAdapter( new BookletAdapter( Booklet.getBooklets() ) );
	}
}
