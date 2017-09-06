package io.amelia.android.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Executors;

import io.amelia.android.backport.MapsBackport;
import io.amelia.android.backport.function.Function;
import io.amelia.android.data.OkHttpProgress;
import io.amelia.android.log.PLog;
import io.amelia.android.support.LibIO;
import io.amelia.booklet.data.ContentManager;
import io.fabric.sdk.android.services.concurrency.AsyncTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileRequestHandler
{
	private final Map<FileRequestPriority, TreeSet<FileRequest>> builders = new HashMap<>();
	private OkHttpClient httpClient;
	private FileBuilder.ProgressListener progressListener = null;

	public FileRequestHandler enqueueBuilder( FileRequestPriority priority, FileBuilder fileBuilder ) throws FileBuilder.FileBuilderException
	{
		MapsBackport.computeIfAbsent( builders, priority, new Function<FileRequestPriority, TreeSet<FileRequest>>()
		{
			@Override
			public TreeSet<FileRequest> apply( FileRequestPriority priority )
			{
				return new TreeSet<>();
			}
		} ).add( fileBuilder.apply() );

		return this;
	}

	public FileRequestHandler enqueueBuilder( FileBuilder fileBuilder ) throws FileBuilder.FileBuilderException
	{
		return enqueueBuilder( FileRequestPriority.NORMAL, fileBuilder );
	}

	public void start( FileRequestMode mode )
	{
		if ( builders.size() == 0 )
			return;

		if ( httpClient != null )
			throw new FileRequestHandlerException( "The FileRequest has already been started!" );

		new AsyncTask<Void, FileRequest, Void>()
		{
			Map<FileRequestPriority, TreeSet<FileRequest>> buildersSync;
			FileRequest nextRequest = null;

			@Override
			protected Void doInBackground( Void... voids )
			{
				if ( mode == FileRequestMode.SYNC )
					loopSyncPrep();

				while ( !( mode == FileRequestMode.SYNC ? loopSync() : loopAsync() ) )
				{
					try
					{
						Thread.sleep( 1000 );
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}
				}

				return null;
			}

			boolean handleRequest( final FileRequest nextRequest )
			{
				try
				{
					PLog.i( "File Request Handle " + hashCode() + " " + LibIO.relPath( nextRequest.fileBuilder.localFile, ContentManager.getCacheDirectory() ) + " // " + nextRequest.exists + " // " + nextRequest.download + " // " + nextRequest.state );

					if ( nextRequest.state == FileRequestState.INIT )
					{
						if ( nextRequest.exists )
						{
							nextRequest.result.bytes = LibIO.readFileToBytes( nextRequest.fileBuilder.localFile );

							for ( FileResultConsumer fileResultConsumer : nextRequest.fileBuilder.resultConsumerList )
								fileResultConsumer.process( nextRequest );
						}

						setState( nextRequest, FileRequestState.PRE );
					}
					else if ( nextRequest.state == FileRequestState.PRE )
					{
						if ( nextRequest.download )
						{
							setState( nextRequest, FileRequestState.WAITING );

							Request request = new Request.Builder().url( nextRequest.fileBuilder.remoteFile ).tag( nextRequest ).build();
							nextRequest.result.call = httpClient.newCall( request );
							nextRequest.result.call.enqueue( new Callback()
							{
								@Override
								public void onFailure( Call call, IOException e )
								{
									nextRequest.result.lastException = e;
									setState( nextRequest, FileRequestState.ERRORED );
								}

								@Override
								public void onResponse( Call call, Response response ) throws IOException
								{
									nextRequest.result.code = response.code();

									if ( nextRequest.result.code >= 300 )
									{
										nextRequest.result.lastException = new RuntimeException( "The remote server returned code " + response.code() + ", failure." );
										setState( nextRequest, FileRequestState.ERRORED );
									}
									else
									{
										try
										{
											nextRequest.result.bytes = response.body().bytes();
											// TODO Add additional response fields

											for ( FileResultConsumer fileResultConsumer : nextRequest.fileBuilder.resultConsumerList )
												fileResultConsumer.process( nextRequest );

											setState( nextRequest, FileRequestState.FINISHED );
										}
										catch ( Exception e )
										{
											nextRequest.result.lastException = e;
											setState( nextRequest, FileRequestState.ERRORED );
										}
									}
								}
							} );
						}
						else
							setState( nextRequest, FileRequestState.FINISHED );
					}
					else if ( nextRequest.state == FileRequestState.FINISHED || nextRequest.state == FileRequestState.ERRORED || nextRequest.state == FileRequestState.DISPOSED )
						return true;
				}
				catch ( Exception e )
				{
					nextRequest.result.lastException = e;
					setState( nextRequest, FileRequestState.ERRORED );
				}

				return false;
			}

			boolean loopAsync()
			{
				boolean allFinished = true;

				for ( FileRequestPriority priority : FileRequestPriority.values() )
					if ( builders.containsKey( priority ) )
						for ( FileRequest nextRequest : builders.get( priority ) )
						{
							if ( !handleRequest( nextRequest ) )
								allFinished = false;
						}

				return allFinished;
			}

			boolean loopSync()
			{
				if ( nextRequest == null )
					for ( FileRequestPriority priority : FileRequestPriority.values() )
						if ( buildersSync.containsKey( priority ) )
						{
							nextRequest = buildersSync.get( priority ).pollFirst();
							if ( nextRequest != null )
								break;
						}

				if ( nextRequest == null )
					return true;
				else
				{
					if ( handleRequest( nextRequest ) )
						nextRequest = null;

					return false;
				}
			}

			void loopSyncPrep()
			{
				buildersSync = new HashMap<>();
				for ( FileRequestPriority priority : FileRequestPriority.values() )
					if ( builders.containsKey( priority ) )
						MapsBackport.computeIfAbsent( buildersSync, priority, new Function<FileRequestPriority, TreeSet<FileRequest>>()
						{
							@Override
							public TreeSet<FileRequest> apply( FileRequestPriority priority )
							{
								return new TreeSet<>();
							}
						} ).addAll( builders.get( priority ) );
			}

			@Override
			protected void onPostExecute( Void aVoid )
			{
				super.onPostExecute( aVoid );

				for ( TreeSet<FileRequest> set : builders.values() )
					set.clear();
				builders.clear();
				httpClient = null;
			}

			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();

				OkHttpClient.Builder builder = new OkHttpClient.Builder().addNetworkInterceptor( new OkHttpProgress( new OkHttpProgress.ProgressListener()
				{
					@Override
					public void update( Object tag, long bytesRead, long bytesLength, boolean intermittent )
					{
						( ( FileRequest ) tag ).result.bytesReceived = bytesRead;
						( ( FileRequest ) tag ).result.bytesLength = bytesLength < bytesRead ? bytesRead * 2 : bytesLength;
						( ( FileRequest ) tag ).result.isDone = intermittent;

						if ( ( ( FileRequest ) tag ).state == FileRequestState.WAITING )
							publishProgress( ( FileRequest ) tag );
					}
				} ) );

				if ( mode == FileRequestMode.SYNC )
					httpClient = builder.dispatcher( new Dispatcher( Executors.newSingleThreadExecutor() ) ).build();
				else // ASYNC
					httpClient = builder.build();

				PLog.i( "Start Handler " + hashCode() );
			}

			@Override
			protected void onProgressUpdate( FileRequest... values )
			{
				super.onProgressUpdate( values );

				for ( FileRequest request : values )
					try
					{
						// PLog.i( "File Request Progress " + hashCode() + " " + LibIO.relPath( request.fileBuilder.localFile, ContentManager.getCacheDirectory() ) + " // " + request.exists + " // " + request.download + " // " + request.state );

						if ( request.state == FileRequestState.PRE )
						{
							if ( !request.exists && !request.download )
							{
								request.result.lastException = new FileNotFoundException( LibIO.relPath( request.fileBuilder.localFile, ContentManager.getCacheDirectory() ) + " does not exist!" );
								setState( request, FileRequestState.ERRORED );
							}
							else
							{
								if ( request.exists )
								{
									for ( FileResultConsumer fileResultConsumer : request.fileBuilder.resultConsumerList )
										fileResultConsumer.accept( request );
								}

								if ( request.download )
								{
									if ( request.fileBuilder.progressListener != null )
										request.fileBuilder.progressListener.start( request.result );
									if ( progressListener != null )
										progressListener.start( request.result );
								}
							}
						}
						else if ( request.state == FileRequestState.WAITING )
						{
							if ( request.download )
							{
								if ( request.fileBuilder.progressListener != null )
									request.fileBuilder.progressListener.progress( request.result, request.result.bytesReceived, request.result.bytesLength, request.result.isDone );
								if ( progressListener != null )
									progressListener.progress( request.result, request.result.bytesReceived, request.result.bytesLength, request.result.isDone );
							}
						}
						else if ( request.state == FileRequestState.FINISHED )
						{
							request.state = FileRequestState.DISPOSED;

							if ( request.download )
							{
								if ( request.fileBuilder.progressListener != null )
									request.fileBuilder.progressListener.finish( request.result );
								if ( progressListener != null )
									progressListener.finish( request.result );

								for ( FileResultConsumer fileResultConsumer : request.fileBuilder.resultConsumerList )
									fileResultConsumer.accept( request );
							}
						}
						else if ( request.state == FileRequestState.ERRORED )
						{
							request.state = FileRequestState.DISPOSED;

							if ( request.result.lastException == null )
								throw new FileRequestHandlerException( "FileBuilder errored but no exception is reported!" );

							for ( FileResultConsumer fileResultConsumer : request.fileBuilder.resultConsumerList )
								fileResultConsumer.exception( request, request.result.lastException );

							if ( request.fileBuilder.resultExceptionConsumer == null )
								throw new FileRequestHandlerException( "FileRequest " + request.fileBuilder.id + " threw an exception", request.result.lastException );
							request.fileBuilder.resultExceptionConsumer.accept( request.fileBuilder.id, request.result.lastException );
						}
					}
					catch ( Exception e )
					{
						request.result.lastException = e;
						setState( request, FileRequestState.ERRORED );
					}
			}

			void setState( FileRequest nextRequest, FileRequestState state )
			{
				if ( state == FileRequestState.ERRORED )
					nextRequest.result.lastException.printStackTrace();

				nextRequest.state = state;
				publishProgress( nextRequest );
			}

		}.executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	public void start()
	{
		start( FileRequestMode.ASYNC );
	}

	public FileRequestHandler withProgressListener( FileBuilder.ProgressListener progressListener )
	{
		this.progressListener = progressListener;
		return this;
	}

	public enum FileRequestMode
	{
		SYNC,
		ASYNC
	}

	public enum FileRequestPriority
	{
		HIGH,
		NORMAL,
		LOW
	}

	public class FileRequestHandlerException extends RuntimeException
	{
		public FileRequestHandlerException( String message, Exception e )
		{
			super( message, e );
		}

		public FileRequestHandlerException( String message )
		{
			super( message );
		}

		public FileRequestHandler getFileRequest()
		{
			return FileRequestHandler.this;
		}
	}
}
