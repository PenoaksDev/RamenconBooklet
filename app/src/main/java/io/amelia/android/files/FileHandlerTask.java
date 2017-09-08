package io.amelia.android.files;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.amelia.android.log.PLog;
import io.amelia.android.support.Pair;

public final class FileHandlerTask extends AsyncTask<Void, Pair<FileFuture, FileFutureUpdateType>, Void>
{
	private final Map<FileFuturePriority, TreeMap<Integer, FileFuture>> futures;
	private final List<FileFuture> futuresWaiting = new ArrayList<>();
	private final FileFutureMode mode;
	private FileFutureProgressListener fileFutureProgressListener = null;
	private FileFuture nextFuture = null;

	FileHandlerTask( Map<FileFuturePriority, TreeMap<Integer, FileFuture>> futures, FileFutureMode mode, FileFutureProgressListener fileFutureProgressListener )
	{
		this.futures = futures;
		this.mode = mode;
		this.fileFutureProgressListener = fileFutureProgressListener;
	}

	@Override
	protected Void doInBackground( Void... voids )
	{
		if ( mode == FileFutureMode.ASYNC )
		{
			for ( FileFuturePriority priority : FileFuturePriority.values() )
				if ( futures.containsKey( priority ) )
					for ( FileFuture nextFuture : futures.get( priority ).values() )
						try
						{
							futuresWaiting.add( nextFuture );
							nextFuture.handle( this );
						}
						catch ( Exception e )
						{
							nextFuture.setException( e );
						}
		}
		else
			doPollNext();

		while ( true )
		{
			boolean complete = true;

			for ( FileFuture fileFuture : futuresWaiting )
				if ( !fileFuture.isFinished() && fileFuture.getException() == null )
					complete = false;

			// Break cycle if all waiting futures have FINISHED or have thrown an exception.
			if ( complete )
				break;

			try
			{
				Thread.sleep( 50 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	void doPollNext()
	{
		nextFuture = null;

		for ( FileFuturePriority priority : FileFuturePriority.values() )
			if ( futures.containsKey( priority ) )
			{
				Map.Entry<Integer, FileFuture> entry = futures.get( priority ).pollFirstEntry();
				if ( entry != null )
				{
					nextFuture = entry.getValue();
					break;
				}
			}

		if ( nextFuture == null )
			cancel( false );
		else
			try
			{
				futuresWaiting.add( nextFuture );
				nextFuture.handle( this );
			}
			catch ( Exception e )
			{
				nextFuture.setException( e );
			}
	}

	public void doUpdate( FileFuture fileFuture, FileFutureUpdateType fileFutureUpdateType )
	{
		publishProgress( new Pair<>( fileFuture, fileFutureUpdateType ) );
	}

	@Override
	protected void onProgressUpdate( Pair<FileFuture, FileFutureUpdateType>... values )
	{
		super.onProgressUpdate( values );

		FileFuture fileFuture = values[0].getKey();
		FileFutureUpdateType updateType = values[0].getValue();

		// PLog.i( "onProgressUpdate(" + fileFuture + ", " + updateType + ")" );

		try
		{
			if ( updateType == FileFutureUpdateType.START )
			{
				if ( fileFuture.fileBuilder.download )
				{
					if ( fileFuture.fileBuilder.fileFutureProgressListener != null )
						fileFuture.fileBuilder.fileFutureProgressListener.start( fileFuture );
					if ( fileFutureProgressListener != null )
						fileFutureProgressListener.start( fileFuture );
				}
			}
			else if ( updateType == FileFutureUpdateType.PROGRESS )
			{
				if ( fileFuture.fileBuilder.download )
				{
					if ( fileFuture.fileBuilder.fileFutureProgressListener != null )
						fileFuture.fileBuilder.fileFutureProgressListener.progress( fileFuture, fileFuture.bytesReceived, fileFuture.bytesLength, fileFuture.indeterminate );
					if ( fileFutureProgressListener != null )
						fileFutureProgressListener.progress( fileFuture, fileFuture.bytesReceived, fileFuture.bytesLength, fileFuture.indeterminate );
				}
			}
			else if ( updateType == FileFutureUpdateType.PRELOAD )
			{
				for ( FileFutureConsumer fileFutureConsumer : fileFuture.fileBuilder.futureConsumerList )
				{
					// PLog.i( "Result Consumer PRELOAD " + fileFuture.fileBuilder.remoteFile + " // " + fileFutureConsumer );
					fileFutureConsumer.accept( fileFuture, updateType );
				}
			}
			else if ( updateType == FileFutureUpdateType.FINISHED )
			{
				if ( fileFuture.fileBuilder.fileFutureProgressListener != null )
					fileFuture.fileBuilder.fileFutureProgressListener.finish( fileFuture );
				if ( fileFutureProgressListener != null )
					fileFutureProgressListener.finish( fileFuture );

				for ( FileFutureConsumer fileFutureConsumer : fileFuture.fileBuilder.futureConsumerList )
				{
					// PLog.i( "Result Consumer FINISHED " + fileFuture.fileBuilder.remoteFile + " // " + fileFutureConsumer );
					fileFutureConsumer.accept( fileFuture, updateType );
				}
			}
			else if ( updateType == FileFutureUpdateType.ERRORED )
			{
				for ( FileFutureConsumer fileFutureConsumer : fileFuture.fileBuilder.futureConsumerList )
					fileFutureConsumer.exception( fileFuture, fileFuture.getException() );

				if ( fileFuture.fileBuilder.resultExceptionConsumer == null )
					throw new FileHandlerTaskException( "FileRequest " + fileFuture.fileBuilder.id + " threw an exception", fileFuture.getException() );
				fileFuture.fileBuilder.resultExceptionConsumer.accept( fileFuture.fileBuilder.id, fileFuture.getException() );
			}
		}
		catch ( FileHandlerTaskException e )
		{
			throw e;
		}
		catch ( Throwable e )
		{
			fileFuture.setException( e );
		}
	}

	public class FileHandlerTaskException extends RuntimeException
	{
		public FileHandlerTaskException( String message, Throwable throwable )
		{
			super( message, throwable );
		}

		public FileHandlerTaskException( String message )
		{
			super( message );
		}
	}
}
