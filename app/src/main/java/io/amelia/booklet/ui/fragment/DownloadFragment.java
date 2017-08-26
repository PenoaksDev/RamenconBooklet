package io.amelia.booklet.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

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
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View root = inflater.inflate( R.layout.activity_download, container, false );

		uiThread = Thread.currentThread();

		getActivity().setTitle( "Ramencon Booklet" );

		FloatingActionButton settingsFab = ( FloatingActionButton ) root.findViewById( R.id.settings_fab );
		settingsFab.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				( ( BootActivity ) getActivity() ).stacker.setFragment( SettingsFragment.class, true );
			}
		} );

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

					new AlertDialog.Builder( getActivity() ).setTitle( "Error" ).setIcon( R.drawable.errored ).setNeutralButton( "OK :(", new DialogInterface.OnClickListener()
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
