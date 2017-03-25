package com.penoaks.booklet;

import com.penoaks.android.log.PLog;

public class AppTicks implements Runnable
{
	public static int currentTick = ( int ) ( System.currentTimeMillis() / 50 );
	private static boolean isRunning = false;

	private final AppService app;

	AppTicks( AppService app )
	{
		this.app = app;
	}

	@Override
	public void run()
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

					app.tick( currentTick );
				}

				if ( !isRunning )
					break;
				Thread.sleep( 1L );
			}
		}
		catch ( Throwable t )
		{
			app.onException( t );
		}
		finally
		{
			app.onFinish();
		}
	}

	public static void stop()
	{
		isRunning = false;
	}

	public static boolean isRunning()
	{
		return isRunning;
	}
}
