package com.penoaks.booklet;

import android.os.AsyncTask;

import com.penoaks.android.log.PLog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppTicks extends AsyncTask<Void, Void, Void>
{
	private final static ExecutorService threadPool = Executors.newCachedThreadPool();
	public static int currentTick = ( int ) ( System.currentTimeMillis() / 50 );
	private static boolean isRunning = false;
	private static Throwable lastException;

	private Map<String, WeakReference<EventReceiver>> registeredReceivers = new ConcurrentHashMap<>();
	private static AppTicks instance;

	public static AppTicks getInstance()
	{
		if ( instance == null )
			instance = new AppTicks();
		return instance;
	}

	public static ExecutorService getExecutorThreadPool()
	{
		return threadPool;
	}

	private AppTicks()
	{
		executeOnExecutor( threadPool, new Void[0] );
	}

	@Override
	protected Void doInBackground( Void... params )
	{
		try
		{
			isRunning = true;
			long i = System.currentTimeMillis();

			long q = 0L;
			long j = 0L;
			for ( ; ; )
			{
				long k = System.currentTimeMillis();
				long l = k - i;

				if ( l > 2000L && i - q >= 15000L )
				{
					PLog.w( "Can't keep up! Did the system time change, or is the Android OS overloaded?" );
					l = 2000L;
					q = i;
				}

				if ( l < 0L )
				{
					PLog.w( "Time ran backwards! Did the system time change?" );
					l = 0L;
				}

				j += l;
				i = k;

				while ( j > 50L )
				{
					currentTick = ( int ) ( System.currentTimeMillis() / 50 );
					j -= 50L;

					for ( Map.Entry<String, WeakReference<EventReceiver>> receiver : registeredReceivers.entrySet() )
						if ( receiver.getValue() == null || receiver.getValue().get() == null )
							registeredReceivers.remove( receiver.getKey() );
						else
							receiver.getValue().get().onTick( currentTick );
				}

				if ( !isRunning )
					break;
				Thread.sleep( 1L );
			}
		}
		catch ( Throwable t )
		{
			lastException = t;
			t.printStackTrace();
			for ( WeakReference<EventReceiver> receiver : registeredReceivers.values() )
				if ( receiver != null && receiver.get() != null )
					receiver.get().onException( t );
		}
		finally
		{
			for ( WeakReference<EventReceiver> receiver : registeredReceivers.values() )
				if ( receiver != null && receiver.get() != null )
					receiver.get().onShutdown();
			instance = null;
		}
		return null;
	}

	public static void forceStop()
	{
		isRunning = false;
	}

	public static Throwable getLastException()
	{
		return lastException;
	}

	public static boolean isRunning()
	{
		return isRunning;
	}

	public void registerReceiver( String key, EventReceiver receiver )
	{
		registeredReceivers.put( key, new WeakReference<>( receiver ) );
	}

	public void unregisterReceiver( String key )
	{
		registeredReceivers.remove( key );
	}

	public void unregisterReceiver( EventReceiver receiver )
	{
		for ( WeakReference<EventReceiver> ref : registeredReceivers.values() )
			if ( ref.get() == receiver )
				registeredReceivers.remove( ref );
	}

	public static long epoch()
	{
		return System.currentTimeMillis() / 1000;
	}

	public interface EventReceiver
	{
		void onTick( long tick );

		void onException( Throwable cause );

		void onShutdown();
	}
}
