package com.ramencon.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.penoaks.android.log.PLog;
import com.penoaks.booklet.App;
import com.ramencon.ui.HomeActivity;

import org.acra.ACRA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ImageCache
{
	public static final String REMOTE_IMAGES_URL = "http://booklet.dev.penoaks.com/images/ramencon/";

	private static File cacheDirectory = new File( App.cacheDir, "imageCache" );
	private static BitmapFactory.Options options = new BitmapFactory.Options();

	static
	{
		if ( cacheDirectory.isFile() )
			cacheDirectory.delete();

		if ( !cacheDirectory.exists() )
			cacheDirectory.mkdirs();

		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	}

	private ImageCache()
	{

	}

	public static void cacheRemoteImage( String id, String remoteUrl, boolean forceUpdate, ImageResolveTask.ImageFoundListener foundListener, ImageResolveTask.ImageProgressListener progressListener )
	{
		new ImageResolveTask( id, remoteUrl, forceUpdate, foundListener, progressListener ).executeOnExecutor( App.getExecutorThreadPool() );
	}

	public static class ImageResolveTask extends AsyncTask<Void, Void, Void>
	{
		String id;
		String remoteUrl;
		boolean forceUpdate;
		ImageFoundListener foundListener;
		ImageProgressListener progressListener;

		boolean waiting = false;

		long progressBytesTransferred = 0;
		long progressTotalByteCount = 0;
		Bitmap resultBitmap;
		Exception resultError;

		ImageResolveTask( String id, String remoteUrl, boolean forceUpdate, ImageFoundListener foundListener, ImageProgressListener progressListener )
		{
			this.id = id;
			this.remoteUrl = remoteUrl;
			this.forceUpdate = forceUpdate;
			this.foundListener = foundListener;
			this.progressListener = progressListener;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			if ( progressListener != null )
				progressListener.start();
		}

		@Override
		protected void onPostExecute( Void aVoid )
		{
			super.onPostExecute( aVoid );

			if ( progressListener != null )
				progressListener.finish();
		}

		@Override
		protected void onProgressUpdate( Void... values )
		{
			super.onProgressUpdate( values );

			if ( resultBitmap != null )
			{
				foundListener.update( resultBitmap );
				resultBitmap = null;
			}

			if ( resultError != null )
			{
				foundListener.error( resultError );
				resultError = null;
			}

			if ( progressListener != null )
				progressListener.progress( progressBytesTransferred, progressTotalByteCount );
		}

		@Override
		protected Void doInBackground( Void... params )
		{
			final File dest = new File( cacheDirectory, id + ".png" );
			boolean update = false;

			if ( dest.exists() )
			{
				try
				{
					resultBitmap = BitmapFactory.decodeFile( dest.getAbsolutePath(), options );
					publishProgress();
				}
				catch ( Exception e )
				{
					ACRA.getErrorReporter().handleException( new RuntimeException( "Recoverable Exception in Image Cache", e ) );
				}

				if ( System.currentTimeMillis() - dest.lastModified() > 3600000 ) // Redownload once the image is over a day old.
					update = true;
			}
			else
				update = true;

			if ( update || forceUpdate )
			{
				waiting = true;

				PLog.i( "Loading image " + id + " from uri " + remoteUrl.toString() );

				Ion.with( HomeActivity.instance ).load( remoteUrl ).progress( new ProgressCallback()
				{
					@Override
					public void onProgress( long bytesTransferred, long totalByteCount )
					{
						progressBytesTransferred = bytesTransferred;
						progressTotalByteCount = totalByteCount;
						publishProgress();
					}
				} ).asBitmap().setCallback( new FutureCallback<Bitmap>()
				{
					@Override
					public void onCompleted( Exception e, Bitmap bitmap )
					{
						if ( e != null )
						{
							if ( !dest.exists() )
							{
								resultError = e;
								publishProgress();
							}
						}

						if ( bitmap != null )
						{
							try
							{
								OutputStream out = null;
								try
								{
									out = new FileOutputStream( dest );
									bitmap.compress( Bitmap.CompressFormat.PNG, 100, out );
								}
								finally
								{
									if ( out != null )
										out.close();
								}
							}
							catch ( Exception exception )
							{
								ACRA.getErrorReporter().handleException( new RuntimeException( "Recoverable Exception", exception ) );
							}

							resultBitmap = bitmap;
							publishProgress();

							waiting = false;
						}

						waiting = false;
					}
				} );

				while ( waiting )
				{
					try
					{
						Thread.sleep( 50 );
					}
					catch ( InterruptedException ignore )
					{
					}
				}
			}

			return null;
		}

		public interface ImageProgressListener
		{
			void start();

			void progress( long bytesTransferred, long totalByteCount );

			void finish();
		}

		public interface ImageFoundListener
		{
			void update( Bitmap bitmap );

			void error( Exception exception );
		}
	}
}
