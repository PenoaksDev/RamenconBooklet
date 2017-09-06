package io.amelia.booklet.data;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.amelia.android.data.BoundData;
import io.amelia.android.data.OkHttpProgress;
import io.amelia.android.log.PLog;
import io.amelia.android.support.LibAndroid;
import io.amelia.android.support.Objs;
import io.amelia.booklet.ui.fragment.DownloadFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class BookletDownloadTask
{
	final Booklet booklet;
	List<FileDownload> pendingFiles = new ArrayList<>();
	private InternalDownloadTask internalDownloadTask = null;
	private boolean postOpen;
	private BoundData updatedData;

	public BookletDownloadTask( final Booklet booklet, final boolean postOpen )
	{
		Objs.notNull( booklet );

		this.postOpen = postOpen;

		BookletState bookletState = booklet.getState();
		final String bookletId = booklet.getId();
		final String url = ContentManager.REMOTE_CATALOG_URL + bookletId;

		if ( bookletState == BookletState.BUSY )
			ContentManager.getActivity().uiShowSnakeBar( "Booklet is already updating... Please Wait!", Snackbar.LENGTH_SHORT );

		booklet.setInUse( true );

		ContentManager.getActivity().uiShowSnakeBar( "Downloading Booklet " + booklet.getDataTitle() + ", Please Wait...", Snackbar.LENGTH_SHORT );

		this.booklet = booklet;

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url( url ).build();
		client.newCall( request ).enqueue( new Callback()
		{
			@Override
			public void onFailure( Call call, IOException exception )
			{
				booklet.addException( exception );
			}

			@Override
			public void onResponse( Call call, Response response ) throws IOException
			{
				if ( response.code() != 200 )
				{
					booklet.addException( new RuntimeException( "The URL " + url + " returned HTTP code " + response.code() + ", failure." ) );
					return;
				}

				BoundData data = LibAndroid.readJsonToBoundData( response.body().string() )[0];

				if ( data.getBoundData( "details" ).getInt( "code" ) == 0 )
				{
					booklet.addException( new RuntimeException( "The URL " + url + " returned code 0, failure." ) );
					return;
				}

				updatedData = data.getBoundData( "data" );

				List<String> sections = updatedData.getStringList( Booklet.KEY_SECTIONS );

				synchronized ( pendingFiles )
				{
					for ( Map.Entry<String, File> entry : booklet.getSectionFiles( "data.json" ).entrySet() )
					{
						String remoteFile = ContentManager.REMOTE_DATA_URL + booklet.getId() + "/" + entry.getKey() + ".json";
						pendingFiles.add( new FileDownload( bookletId + "-" + entry.getKey(), remoteFile, entry.getValue() ) );
					}
				}

				internalDownloadTask = new InternalDownloadTask();
				internalDownloadTask.executeOnExecutor( ContentManager.getExecutorThreadPool() );
			}
		} );
	}

	public void cancel()
	{
		internalDownloadTask.cancel( false );
	}

	private class FileDownload
	{
		public Call call;
		long bytesRead = 0;
		long contentLength = 0;
		boolean done = false;
		boolean finished = false;
		String id;
		Exception lastException = null;
		File localFile;
		String remoteFile;

		FileDownload( String id, String remoteFile, File localFile )
		{
			this.id = id;
			this.remoteFile = remoteFile;
			this.localFile = localFile;
		}
	}

	private class InternalDownloadTask extends AsyncTask<Void, FileDownload, Void>
	{
		AtomicInteger finishTimeout = new AtomicInteger( -1 );

		@Override
		protected Void doInBackground( Void... params )
		{
			for ( final FileDownload fileDownload : pendingFiles )
			{
				OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor( new OkHttpProgress( new OkHttpProgress.ProgressListener()
				{
					@Override
					public void update( Object tag, long bytesRead, long contentLength, boolean intermittent )
					{
						fileDownload.bytesRead = bytesRead;
						fileDownload.contentLength = contentLength;
						fileDownload.done = intermittent;

						publishProgress( fileDownload );
					}
				} ) ).build();
				Request request = new Request.Builder().url( fileDownload.remoteFile ).build();
				fileDownload.call = client.newCall( request );
				fileDownload.call.enqueue( new Callback()
				{
					@Override
					public void onFailure( Call call, IOException exception )
					{
						fileDownload.lastException = exception;
					}

					@Override
					public void onResponse( Call call, Response response ) throws IOException
					{
						PLog.i( "Pre-finished " + fileDownload.remoteFile );

						if ( response.code() != 200 )
						{
							//  URL " + fileDownload.remoteFile + "
							fileDownload.lastException = new RuntimeException( "The remote booklet server returned code " + response.code() + ", failure." );
							return;
						}

						BoundData data = LibAndroid.readJsonToBoundData( response.body().string() )[0];
						PLog.i( "Data: " + data );

						if ( data.getBoundData( "details" ).getInt( "code" ) == 0 )
						{
							fileDownload.lastException = new RuntimeException( "The remote booklet server returned code 0, failure." );
							return;
						}

						/* if ( ContentManager.downloadImages() && data.hasBoundData( "files" ) )
						{
							BoundData files = data.getBoundData( "files" );
							for ( Map.Entry<String, String> entry : files.getStringEntrySet() )
								pendingFiles.add( new FileDownload( fileDownload.fileName + "/" + entry.getKey(), LibIO.fileExtension( entry.getValue() ), entry.getValue() ) );
						} */

						fileDownload.localFile.getParentFile().mkdirs();
						LibAndroid.writeBoundDataToJsonFile( data.hasBoundData( "data" ) ? data.getBoundData( "data" ) : data, fileDownload.localFile );

						PLog.i( "Finished Downloading " + fileDownload.remoteFile );

						fileDownload.finished = true;
					}
				} );

			}

			for ( ; ; )
			{
				int currentTimeout = finishTimeout.addAndGet( 1 );
				if ( currentTimeout > 25 ) // 5 Seconds
					PLog.i( "Waiting for files to finish!" );

				boolean allDone = true;
				boolean exception = false;

				for ( final FileDownload fileDownload : pendingFiles )
					if ( fileDownload.lastException != null )
					{
						fileDownload.lastException.printStackTrace();
						// Toast.makeText( ContentManager.getDownloadFragment().getActivity(), fileDownload.lastException.getMessage(), Toast.LENGTH_LONG ).show();
						exception = true;
						break;
					}
					else if ( !fileDownload.finished )
					{
						if ( currentTimeout > 25 ) // 5 Seconds
							PLog.i( "File " + fileDownload.remoteFile + " not finished!" );
						allDone = false;
						break;
					}

				if ( exception )
				{
					for ( final FileDownload fileDownload : pendingFiles )
						if ( fileDownload.lastException != null )
							booklet.addException( fileDownload.lastException );
					break;
				}

				if ( allDone )
					break;

				if ( currentTimeout > 120 ) // 30 Seconds
					PLog.w( "Nearing File Download Termination in " + ( 60 - ( currentTimeout / 4 ) ) + " seconds." );

				if ( currentTimeout > 240 ) // 60 Seconds
				{
					for ( final FileDownload fileDownload : pendingFiles )
						fileDownload.call.cancel();
					break;
				}

				try
				{
					Thread.sleep( 250 );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute( Void aVoid )
		{
			booklet.clearImageCache();

			booklet.setData( updatedData );
			booklet.saveData();

			booklet.setInUse( false );

			ContentManager.getActivity().uiShowSnakeBar( "Booklet " + booklet.getDataTitle() + " has successfully been downloaded!", Snackbar.LENGTH_LONG );

			if ( postOpen )
				booklet.goOpen();
		}

		@Override
		protected void onProgressUpdate( FileDownload... values )
		{
			for ( FileDownload fileDownload : values )
				if ( fileDownload.contentLength > 0 )
					PLog.i( "File " + fileDownload.remoteFile + " progress: " + ( 100 * ( fileDownload.bytesRead / fileDownload.contentLength ) ) + " (" + fileDownload.bytesRead + " of " + fileDownload.contentLength + " bytes)" );

			int percentDone = 0;
			boolean isDone = true;

			for ( FileDownload fileDownload : pendingFiles )
			{
				if ( fileDownload.contentLength > 0 )
					percentDone = percentDone + ( int ) ( 100 * ( fileDownload.bytesRead / fileDownload.contentLength ) );

				if ( !fileDownload.done )
					isDone = false;
			}

			DownloadFragment.instance().uiUpdateProgressBar( booklet.getId(), percentDone, pendingFiles.size() * 100, isDone );
		}
	}
}