package io.amelia.booklet;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ProgressBar;

import com.ramencon.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.amelia.android.data.BoundData;
import io.amelia.android.data.OkHttpProgress;
import io.amelia.android.log.PLog;
import io.amelia.android.support.LibAndroid;
import io.amelia.android.support.Objs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class BookletDownloadTask
{
	final Booklet booklet;
	final ProgressBar progressBar;
	Exception lastException;
	List<FileDownload> pendingFiles = new ArrayList<>();
	String response;
	private InternalDownloadTask internalDownloadTask = null;
	private BoundData updatedData;

	public BookletDownloadTask( final Booklet booklet, final View view ) throws UpdateAbortException
	{
		Objs.notNull( booklet );

		BookletState bookletState = booklet.getState();
		final String bookletId = booklet.getId();
		final String url = ContentManager.REMOTE_CATALOG_URL + bookletId;

		if ( bookletState == BookletState.BUSY )
			if ( view == null )
				throw new IllegalStateException( "Booklet is already updating!" );
			else
			{
				Snackbar.make( view, "Booklet is already updating... Please Wait!", Snackbar.LENGTH_LONG ).show();
				throw new UpdateAbortException();
			}

		booklet.setInUse( true );

		if ( view != null )
			Snackbar.make( view, "Updating Booklet " + bookletId + " // " + bookletState, Snackbar.LENGTH_LONG ).show();

		progressBar = view == null ? null : ( ProgressBar ) view.findViewById( R.id.booklet_progress );
		if ( progressBar != null )
		{
			progressBar.setIndeterminate( true );
			progressBar.setMax( 100 );
		}

		this.booklet = booklet;

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url( url ).build();
		client.newCall( request ).enqueue( new Callback()
		{
			@Override
			public void onFailure( Call call, IOException exception )
			{
				booklet.handleException( exception );
			}

			@Override
			public void onResponse( Call call, Response response ) throws IOException
			{
				if ( response.code() != 200 )
				{
					booklet.handleException( new RuntimeException( "The URL " + url + " returned HTTP code " + response.code() + ", failure." ) );
					return;
				}

				BoundData data = LibAndroid.readJsonToBoundData( response.body().string() )[0];

				if ( data.getBoundData( "details" ).getInt( "code" ) == 0 )
				{
					booklet.handleException( new RuntimeException( "The URL " + url + " returned code 0, failure." ) );
					return;
				}

				updatedData = data.getBoundData( "data" );

				List<String> files = updatedData.getStringList( Booklet.KEY_FILES );

				synchronized ( pendingFiles )
				{
					for ( String file : files )
					{
						File localFile = new File( booklet.getDataDirectory(), file + ".json" );
						String remoteFile = ContentManager.REMOTE_DATA_URL + bookletId + "/" + file + ".json";

						pendingFiles.add( new FileDownload( bookletId + "-" + file, remoteFile, localFile ) );
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
		long bytesRead = 0;
		long contentLength = 0;
		boolean done = false;
		boolean error = false;
		String id;
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
		@Override
		protected Void doInBackground( Void... params )
		{
			for ( final FileDownload fileDownload : pendingFiles )
			{
				OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor( new OkHttpProgress( new OkHttpProgress.ProgressListener()
				{
					@Override
					public void update( long bytesRead, long contentLength, boolean done )
					{
						fileDownload.bytesRead = bytesRead;
						fileDownload.contentLength = contentLength;
						fileDownload.done = done;

						publishProgress( fileDownload );
					}
				} ) ).build();
				Request request = new Request.Builder().url( fileDownload.remoteFile ).build();
				client.newCall( request ).enqueue( new Callback()
				{
					@Override
					public void onFailure( Call call, IOException exception )
					{
						booklet.handleException( exception );
						fileDownload.error = true;
					}

					@Override
					public void onResponse( Call call, Response response ) throws IOException
					{
						if ( response.code() != 200 )
						{
							booklet.handleException( new RuntimeException( "The URL " + fileDownload.remoteFile + " returned code " + response.code() + ", failure." ) );
							fileDownload.error = true;
							return;
						}

						BoundData data = LibAndroid.readJsonToBoundData( response.body().string() )[0];
						PLog.i( "Data: " + data );

						if ( data.getBoundData( "details" ).getInt( "code" ) == 0 )
						{
							booklet.handleException( new RuntimeException( "The URL " + fileDownload.remoteFile + " returned code 0, failure." ) );
							return;
						}

						LibAndroid.writeBoundDataToJsonFile( data, fileDownload.localFile );
					}
				} );

			}

			for ( ; ; )
			{
				PLog.i( "Waiting for files to finish!" );

				boolean allDone = true;

				for ( final FileDownload fileDownload : pendingFiles )
					if ( !fileDownload.done && !fileDownload.error )
					{
						allDone = false;
						break;
					}

				if ( allDone )
					break;

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
			if ( progressBar != null )
				progressBar.setVisibility( View.GONE );

			booklet.data = updatedData;
			booklet.saveData();

			booklet.setInUse( false );

			PLog.i( "Finished downloading booklet " + booklet.getId() );
		}

		@Override
		protected void onProgressUpdate( FileDownload... values )
		{
			for ( FileDownload fileDownload : values )
				PLog.i( "File " + fileDownload.remoteFile + " Progress: " + ( 100 * ( fileDownload.bytesRead / fileDownload.contentLength ) ) + " (" + fileDownload.bytesRead + " of " + fileDownload.contentLength + " bytes)" );

			if ( progressBar != null )
			{
				int average = 0;
				boolean isDone = true;

				for ( FileDownload fileDownload : pendingFiles )
				{
					if ( fileDownload.contentLength > 0 )
					{
						int percent = ( int ) ( 100 * ( fileDownload.bytesRead / fileDownload.contentLength ) );
						average = average + percent;
					}

					if ( !fileDownload.done )
						isDone = false;
				}

				average = average / pendingFiles.size();

				progressBar.setProgress( average );
				progressBar.setIndeterminate( isDone );
			}
		}
	}

	protected class UpdateAbortException extends Exception
	{
	}
}