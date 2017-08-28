package io.amelia.booklet.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import io.amelia.R;
import io.amelia.android.log.PLog;
import io.amelia.android.support.DateAndTime;
import io.amelia.booklet.Booklet;
import io.amelia.booklet.ContentManager;
import io.amelia.booklet.data.BookletAdapter;
import io.amelia.booklet.ui.activity.BootActivity;

public class DownloadFragment extends Fragment
{
	public static final int SHOW_PROGRESS_BAR = 0x00;
	public static final int HIDE_PROGRESS_BAR = 0x01;
	public static final int SHOW_ERROR_DIALOG = 0x02;
	public static final int UPDATE_BOOKLETS = 0x03;

	public static DownloadFragment instance()
	{
		return new DownloadFragment();
	}

	private ExpandableListView bookletListview;
	private ProgressDialog loading;
	private SwipeRefreshLayout refreshLayout;
	private Thread uiThread;
	private UIUpdater uiUpdater;

	public DownloadFragment()
	{
	}

	public UIUpdater getUiUpdater()
	{
		return uiUpdater;
	}

	public void hideProgressBar()
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "type", DownloadFragment.HIDE_PROGRESS_BAR );
		uiUpdater.putBundle( bundle );
	}

	public boolean isUIThread()
	{
		return uiThread == Thread.currentThread();
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

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View root = inflater.inflate( R.layout.activity_download, container, false );

		uiThread = Thread.currentThread();

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

		refreshLayout = ( SwipeRefreshLayout ) root.findViewById( R.id.booklet_refresher );
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

		PLog.i( "Starting DownloadActivity" );

		ContentManager.setDownloadFragment( this );

		updateBookletsView();

		uiUpdater = new UIUpdater();
		uiUpdater.executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	@Override
	public void onStop()
	{
		super.onStop();

		ContentManager.setDownloadFragment( null );

		uiUpdater.cancel( false );
		uiUpdater = null;
	}

	public void showErrorDialog( String message )
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "type", DownloadFragment.SHOW_ERROR_DIALOG );
		bundle.putString( "message", message );
		uiUpdater.putBundle( bundle );
	}

	public void showProgressBar( String title, String message )
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "type", DownloadFragment.SHOW_PROGRESS_BAR );
		bundle.putString( "title", title );
		bundle.putString( "message", message );
		uiUpdater.putBundle( bundle );
	}

	public void updateBooklets()
	{
		Bundle bundle = new Bundle();

		bundle.putInt( "type", DownloadFragment.UPDATE_BOOKLETS );

		uiUpdater.putBundle( bundle );
	}

	private void updateBookletsView()
	{
		bookletListview.setAdapter( new BookletAdapter( getActivity(), Booklet.getBooklets() ) );
	}

	public class UIUpdater extends AsyncTask<Void, Bundle, Void>
	{
		private Map.Entry<Long, Bundle> currentEntry = null;
		private NavigableMap<Long, Bundle> pending = new TreeMap<>();

		@Override
		protected Void doInBackground( Void... params )
		{
			do
			{
				if ( currentEntry == null )
					currentEntry = pending.firstEntry();

				if ( currentEntry == null || currentEntry.getKey() < DateAndTime.epoch() )
					try
					{
						Thread.sleep( 250 );
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}
				else
				{
					publishProgress( currentEntry.getValue() );
					currentEntry = null;
				}
			}
			while ( !isCancelled() );

			pending.clear();

			return null;
		}

		@Override
		protected void onProgressUpdate( Bundle... values )
		{
			super.onProgressUpdate( values );

			for ( Bundle bundle : values )
			{
				int type = bundle.getInt( "type" );

				if ( type == DownloadFragment.SHOW_PROGRESS_BAR )
				{
					String title = bundle.getString( "title", "" );
					String message = bundle.getString( "message", "Please Wait..." );

					loading = ProgressDialog.show( getActivity(), title, message, true );
				}
				else if ( type == DownloadFragment.HIDE_PROGRESS_BAR )
					loading.cancel();
				else if ( type == DownloadFragment.SHOW_ERROR_DIALOG )
				{
					String message = bundle.getString( "message", "Encountered an internal error." );

					new AlertDialog.Builder( getContext() ).setTitle( "Error" ).setIcon( R.drawable.errored ).setNeutralButton( "BUMMER! :(", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( DialogInterface dialog, int which )
						{
							dialog.dismiss();
						}
					} ).setMessage( message ).create().show();
				}
				else if ( type == DownloadFragment.UPDATE_BOOKLETS )
					updateBookletsView();
			}
		}

		public void putBundle( Bundle bundle )
		{
			pending.put( DateAndTime.epoch(), bundle );
		}

		public void putBundle( Long future, Bundle bundle )
		{
			pending.put( future, bundle );
		}
	}
}
