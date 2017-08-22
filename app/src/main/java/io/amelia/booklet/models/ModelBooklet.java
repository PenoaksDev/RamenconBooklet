package io.amelia.booklet.models;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ProgressBar;

import io.amelia.android.log.PLog;
import io.amelia.android.data.OkHttpProgress;
import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.configuration.types.json.JsonConfiguration;
import io.amelia.booklet.App;
import com.ramencon.R;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Represents an available Booklet
 */
public class ModelBooklet implements Serializable
{
	private static final Map<String, ModelBooklet> loadedModels = new HashMap<>();

	public static ModelBooklet getModel( String appId )
	{
		return loadedModels.get( appId );
	}

	private BookletState state;
	private JsonConfiguration data;

	public String bookletId;
	public String published;
	public String lastUpdated;
	public String image;
	public String title;
	public String whenFrom;
	public String whenTo;
	public String where;
	public String description;

	public ModelBooklet( final ConfigurationSection raw )
	{
		bookletId = raw.getString( "bookletId" );
		published = raw.getString( "published" );
		lastUpdated = raw.getString( "lastUpdated" );
		image = raw.getString( "image" );
		title = raw.getString( "title" );
		whenFrom = raw.getString( "whenFrom" );
		whenTo = raw.getString( "whenTo" );
		where = raw.getString( "where" );
		description = raw.getString( "description" );

		loadedModels.put( bookletId, this );

		final File bookletFile = getBookletFile();
		if ( bookletFile.exists() )
		{
			data = JsonConfiguration.loadConfiguration( bookletFile );
			if ( !lastUpdated.equals( data.getString( "__lastUpdated" ) ) )
			{
				state = BookletState.OUTDATED; /* The Booklet is downloaded but is outdated */
				update( null, null );
			}
			else
				state = BookletState.READY; /* The Booklet is ready for viewing */
		}
		else
			state = BookletState.AVAILABLE; /* The Booklet is available for download and was never downloaded */
	}

	public JsonConfiguration getBookletData()
	{
		return data;
	}

	public File getBookletFile()
	{
		return new File( App.cacheDir, "booklet-" + bookletId + ".json" );
	}

	public BookletState getBookletState()
	{
		return state;
	}

	private void update( final View view, final Runnable postTask )
	{
		if ( state == BookletState.UPDATING )
			throw new IllegalStateException( "Booklet is already updating!" );

		final ProgressBar progressBar = view == null ? null : ( ProgressBar ) view.findViewById( R.id.booklet_progress );
		if ( progressBar != null )
		{
			progressBar.setIndeterminate( true );
			progressBar.setMax( 100 );
		}

		state = BookletState.UPDATING;

		new UpdateTask( this, new UpdateTask.TaskProgress()
		{
			@Override
			public void onFinish( ConfigurationSection data ) throws Exception
			{
				PLog.i( "Response JSON: " + data.dump() );

				if ( !data.isConfigurationSection( "data" ) )
					throw new IllegalStateException( "The data section does not exist!" );

				JsonConfiguration newData = new JsonConfiguration();
				newData.merge( data.getConfigurationSection( "data" ) );
				newData.set( "__lastUpdated", lastUpdated );
				newData.save( ModelBooklet.this.getBookletFile() );
				ModelBooklet.this.data = newData;

				state = BookletState.READY;

				if ( progressBar != null )
					progressBar.setVisibility( View.GONE );

				if ( postTask != null )
					postTask.run();

				PLog.i( "Finished downloading booklet " + bookletId );
			}

			@Override
			public void onFailure( Exception e )
			{
				e.printStackTrace();
				state = BookletState.FAILURE;

				if ( view != null )
					Snackbar.make( view, "Booklet has failed to download... " + e.getMessage(), Snackbar.LENGTH_LONG ).show();

				if ( progressBar != null )
					progressBar.setVisibility( View.GONE );
			}

			@Override
			public void onProgress( long bytesRead, long contentLength, boolean done )
			{
				if ( progressBar != null )
				{
					progressBar.setProgress( ( int ) ( 100 * ( bytesRead / contentLength ) ) );
					progressBar.setIndeterminate( done );
				}
			}
		} ).executeOnExecutor( App.getExecutorThreadPool() );
	}

	public void updateAndOpen( View view, boolean force, Runnable postTask )
	{
		if ( state == BookletState.UPDATING )
			Snackbar.make( view, "Booklet is already updating... Please Wait!", Snackbar.LENGTH_LONG ).show();
		else
		{
			if ( state != BookletState.READY || force )
			{
				update( view, postTask );

				Snackbar.make( view, "Updating Booklet " + bookletId + " // " + state, Snackbar.LENGTH_LONG ).show();
			}
			else
			{
				Snackbar.make( view, "Displaying Booklet " + bookletId + " // " + state, Snackbar.LENGTH_LONG ).show();
				postTask.run();
			}
		}
	}

	public enum BookletState
	{
		AVAILABLE, OUTDATED, UPDATING, FAILURE, READY
	}

	static class UpdateTask extends AsyncTask<Void, Void, Void>
	{
		final ModelBooklet model;
		final TaskProgress task;
		String response;
		Exception exception;
		long bytesRead;
		long contentLength;
		boolean done;

		public UpdateTask( ModelBooklet model, TaskProgress task )
		{
			this.model = model;
			this.task = task;
		}

		@Override
		protected void onProgressUpdate( Void... values )
		{
			task.onProgress( bytesRead, contentLength, done );
		}

		@Override
		protected void onPostExecute( Void aVoid )
		{
			if ( exception != null )
				task.onFailure( exception );
			else if ( response != null )
				try
				{
					task.onFinish( JsonConfiguration.loadConfiguration( response ) );
				}
				catch ( Exception e )
				{
					task.onFailure( e );
				}
		}

		@Override
		protected Void doInBackground( Void... params )
		{
			final AtomicInteger waiting = new AtomicInteger();
			OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor( new OkHttpProgress( new OkHttpProgress.ProgressListener()
			{
				@Override
				public void update( long bytesRead, long contentLength, boolean done )
				{
					UpdateTask.this.bytesRead = bytesRead;
					UpdateTask.this.contentLength = contentLength;
					UpdateTask.this.done = done;

					publishProgress();
				}
			} ) ).build();
			Request request = new Request.Builder().url( App.REMOTE_DATA_URL + model.bookletId ).build();
			client.newCall( request ).enqueue( new Callback()
			{
				@Override
				public void onFailure( Call call, IOException exception )
				{
					UpdateTask.this.exception = exception;
					waiting.set( 1 );
				}

				@Override
				public void onResponse( Call call, Response response ) throws IOException
				{
					UpdateTask.this.response = response.body().string();
					waiting.set( 1 );
				}
			} );

			while ( waiting.get() == 0 )
				try
				{
					Thread.sleep( 250 );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			return null;
		}

		public interface TaskProgress
		{
			void onFinish( ConfigurationSection data ) throws Exception;

			void onProgress( long bytesRead, long contentLength, boolean done );

			void onFailure( Exception e );
		}
	}
}
