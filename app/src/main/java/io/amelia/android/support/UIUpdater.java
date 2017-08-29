package io.amelia.android.support;

import android.os.AsyncTask;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import io.amelia.android.data.BoundData;

public class UIUpdater extends AsyncTask<Void, BoundData, Void>
{
	private final Receiver receiver;
	private Map.Entry<Long, BoundData> currentEntry = null;
	private boolean isPaused = false;
	private NavigableMap<Long, BoundData> pending = new TreeMap<>();
	private Thread uiThread;

	public UIUpdater( Receiver receiver )
	{
		// TODO Check if we are truly called from the UI thread.

		this.receiver = receiver;
		uiThread = Thread.currentThread();
	}

	@Override
	protected Void doInBackground( Void... params )
	{
		do
		{
			if ( currentEntry == null )
				currentEntry = pending.firstEntry();

			if ( isPaused || currentEntry == null || currentEntry.getKey() < DateAndTime.epoch() )
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

	public boolean isUIThread()
	{
		return uiThread == Thread.currentThread();
	}

	@Override
	protected void onProgressUpdate( BoundData... values )
	{
		super.onProgressUpdate( values );

		for ( BoundData bundle : values )
		{
			int type = bundle.getInt( "type" );
			receiver.processUpdate( type, bundle );
		}
	}

	public void pause()
	{
		isPaused = true;
	}

	public void putBoundData( int type, BoundData bundle )
	{
		bundle.put( "type", type );
		pending.put( DateAndTime.epoch(), bundle );
	}

	public void putBoundData( Long future, int type, BoundData bundle )
	{
		bundle.put( "type", type );
		pending.put( future, bundle );
	}

	public void resume()
	{
		isPaused = false;
	}

	public interface Receiver
	{
		void processUpdate( int type, BoundData data );
	}
}
