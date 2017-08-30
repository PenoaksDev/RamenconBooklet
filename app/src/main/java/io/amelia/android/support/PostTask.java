package io.amelia.android.support;

import android.os.AsyncTask;

import java.util.concurrent.Callable;

import io.amelia.android.log.PLog;
import io.amelia.booklet.data.ContentManager;

public class PostTask extends AsyncTask<Void, Void, Void>
{
	private Callable callable;
	private boolean waiting = true;

	public PostTask( Callable callable )
	{
		this.callable = callable;
		executeOnExecutor( ContentManager.getExecutorThreadPool(), new Void[0] );
	}

	public void done()
	{
		PLog.i( "(" + hashCode() + ") PostTask Pre-done!" );

		waiting = false;
	}


	@Override
	protected Void doInBackground( Void... params )
	{
		while ( waiting && !isCancelled() )
			try
			{
				// PLog.i( "(" + hashCode() + ") PostTask Waiting!" );
				Thread.sleep( 250 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		return null;
	}

	@Override
	protected void onPostExecute( Void aVoid )
	{
		super.onPostExecute( aVoid );

		if ( isCancelled() )
			return;

		try
		{
			callable.call();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
