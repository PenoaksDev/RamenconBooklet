package io.amelia.booklet.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.log.PLog;
import io.amelia.android.support.UIUpdater;
import io.amelia.booklet.data.ContentManager;

public abstract class BaseActivity extends AppCompatActivity implements UIUpdater.Receiver
{
	public static final int SHOW_PROGRESS_BAR = 0x00;
	public static final int HIDE_PROGRESS_BAR = 0x01;
	public static final int SHOW_ERROR_DIALOG = 0x02;
	public static final int SHOW_SNACKBAR = 0x03;

	private ProgressDialog loading;
	private UIUpdater uiUpdater;

	public BaseActivity()
	{
		uiUpdater = new UIUpdater( this );
	}

	public UIUpdater getUiUpdater()
	{
		return uiUpdater;
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		uiUpdater.pause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		ContentManager.onStartActivity( this );
		uiUpdater.resume();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if ( uiUpdater.isCancelled() )
			uiUpdater = new UIUpdater( this );
		uiUpdater.executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		uiUpdater.cancel( false );
	}

	@Override
	public final void processUpdate( int type, BoundData data )
	{
		if ( type == SHOW_PROGRESS_BAR )
		{
			String title = data.getString( "title", "" );
			String message = data.getString( "message", "Please Wait..." );

			loading = ProgressDialog.show( this, title, message, true );
		}
		else if ( type == HIDE_PROGRESS_BAR )
		{
			if ( loading != null && loading.isShowing() )
				loading.cancel();
		}
		else if ( type == SHOW_ERROR_DIALOG )
		{
			String message = data.getString( "message", "Encountered an internal error." );

			PLog.i( "Showing Error Dialog: " + message );

			new AlertDialog.Builder( this ).setTitle( "Error" ).setIcon( R.drawable.ic_error ).setNeutralButton( "BUMMER! :(", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick( DialogInterface dialog, int which )
				{
					dialog.dismiss();
				}
			} ).setMessage( message ).create().show();
		}
		else if ( type == SHOW_SNACKBAR )
		{
			String message = data.getString( "message", "Please Wait..." );
			int length = data.getInteger( "length", Snackbar.LENGTH_SHORT );

			PLog.i( "Showing Snackbar: " + message );

			// TODO Get the VIEW some other way!
			if ( getCurrentFocus() == null )
				Toast.makeText( this, message, length == Snackbar.LENGTH_SHORT ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG ).show();
			else
				Snackbar.make( getCurrentFocus(), message, length ).show();
		}
		else
			processUpdate0( type, data );
	}

	protected abstract void processUpdate0( int type, BoundData data );

	public void putUIUpdate( int type, BoundData data )
	{
		if ( data == null )
			data = new BoundData();
		if ( uiUpdater.isUIThread() )
			processUpdate( type, data );
		else
			uiUpdater.putBoundData( type, data );
		// TODO Figure out a way to better differentiate each fragments updates.
	}

	public void uiHideProgressBar()
	{
		putUIUpdate( HIDE_PROGRESS_BAR, null );
	}

	public void uiShowErrorDialog( String message )
	{
		BoundData bundle = new BoundData();
		bundle.put( "message", message );
		putUIUpdate( SHOW_ERROR_DIALOG, bundle );
	}

	public void uiShowProgressBar( String title, String message )
	{
		BoundData bundle = new BoundData();
		bundle.put( "message", message );
		putUIUpdate( SHOW_PROGRESS_BAR, bundle );
	}

	public void uiShowSnakeBar( String message, int length )
	{
		BoundData bundle = new BoundData();
		bundle.put( "message", message );
		bundle.put( "length", length );
		putUIUpdate( SHOW_SNACKBAR, bundle );
	}
}
