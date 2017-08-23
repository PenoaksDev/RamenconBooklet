package io.amelia.android.support;

import android.os.AsyncTask;

import io.amelia.booklet.ContentManager;

public class WaitFor
{
	private Task task;
	private long timeout;
	private Butler current = new Butler();

	public WaitFor( Task task )
	{
		this( task, 0 );
	}

	public WaitFor( Task task, long timeout )
	{
		this.task = task;
		this.timeout = timeout;
		current.executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	public void cancel( boolean mayInterruptIfRunning )
	{
		current.cancel( mayInterruptIfRunning );
	}

	public interface Task
	{
		/**
		 * Called on each while iteration (250 millis each) to check if the task is finished.
		 *
		 * @return isReady to finish, True if so.
		 */
		boolean isReady();

		/**
		 * Called once ready() returns true.
		 */
		void onFinish();

		/**
		 * Called if a timeout is set and the timeout is exceeded.
		 */
		void onTimeout();
	}

	private class Butler extends AsyncTask<Void, Void, Void>
	{
		long time = 0;

		@Override
		protected Void doInBackground( Void... params )
		{
			do
			{
				try
				{
					Thread.sleep( 250 );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
				time = time + 250;
			}
			while ( !task.isReady() && ( timeout < 1 || time < timeout ) );
			return null;
		}

		@Override
		protected void onPostExecute( Void aVoid )
		{
			super.onPostExecute( aVoid );

			if ( timeout > 0 && time < timeout )
				task.onTimeout();
			else
				task.onFinish();
		}
	}
}
