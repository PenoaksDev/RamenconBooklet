package io.amelia.booklet.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.ramencon.R;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import io.amelia.android.support.DateAndTime;
import io.amelia.booklet.Booklet;
import io.amelia.booklet.ContentManager;
import io.amelia.booklet.data.BookletAdapter;

public class DownloadActivity extends AppCompatActivity
{
	public static final int SHOW_PROGRESS_BAR = 0x00;
	public static final int HIDE_PROGRESS_BAR = 0x01;
	public static final int SHOW_ERROR_DIALOG = 0x02;
	public static final int UPDATE_BOOKLETS = 0x03;
	private ExpandableListView bookletListview;
	private ProgressDialog loading;
	private SwipeRefreshLayout refreshLayout;
	private Thread uiThread;
	private UIUpdater uiUpdater;

	public UIUpdater getUiUpdater()
	{
		return uiUpdater;
	}

	public void hideProgressBar()
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "type", HIDE_PROGRESS_BAR );
		uiUpdater.putBundle( bundle );
	}

	public boolean isUIThread()
	{
		return uiThread == Thread.currentThread();
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_download );

		uiThread = Thread.currentThread();

		setTitle( "Ramencon Booklet" );

		refreshLayout = ( SwipeRefreshLayout ) findViewById( R.id.booklet_refresher );
		refreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				ContentManager.refreshBooklets();
			}
		} );

		bookletListview = ( ExpandableListView ) findViewById( R.id.bootlet_listview );
	}

	public void onListItemClick( Booklet booklet )
	{
		// ( ( ContentActivity ) getActivity() ).stacker.setFragment( BookletFragment.instance( booklet.bookletId ), true );
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		ContentManager.setDownloadActivity( this );

		uiUpdater = new UIUpdater();
		uiUpdater.executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		ContentManager.setDownloadActivity( null );

		uiUpdater.cancel( false );
		uiUpdater = null;
	}

	public void showErrorDialog( String message )
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "type", SHOW_ERROR_DIALOG );
		bundle.putString( "message", message );
		uiUpdater.putBundle( bundle );
	}

	public void showProgressBar( String title, String message )
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "type", SHOW_PROGRESS_BAR );
		bundle.putString( "title", title );
		bundle.putString( "message", message );
		uiUpdater.putBundle( bundle );
	}

	public void updateBooklets()
	{
		Bundle bundle = new Bundle();

		bundle.putInt( "type", UPDATE_BOOKLETS );

		uiUpdater.putBundle( bundle );
	}

	private void updateBookletsView()
	{
		bookletListview.setAdapter( new BookletAdapter( this, Booklet.getBooklets() ) );
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

				if ( type == SHOW_PROGRESS_BAR )
				{
					String title = bundle.getString( "title", "" );
					String message = bundle.getString( "message", "Please Wait..." );

					loading = ProgressDialog.show( DownloadActivity.this, title, message, true );
				}
				else if ( type == HIDE_PROGRESS_BAR )
					loading.cancel();
				else if ( type == SHOW_ERROR_DIALOG )
				{
					String message = bundle.getString( "message", "Encountered an internal error." );

					new AlertDialog.Builder( DownloadActivity.this ).setTitle( "Error" ).setIcon( R.drawable.errored ).setNeutralButton( "OK :(", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick( DialogInterface dialog, int which )
						{
							dialog.dismiss();
						}
					} ).setMessage( message ).create().show();
				}
				else if ( type == UPDATE_BOOKLETS )
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
