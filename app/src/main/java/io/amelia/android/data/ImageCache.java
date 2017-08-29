package io.amelia.android.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.acra.ACRA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import io.amelia.android.log.PLog;
import io.amelia.android.support.ACRAHelper;
import io.amelia.booklet.ContentManager;

public class ImageCache
{
	public static final String REMOTE_IMAGES_URL = "http://booklet.dev.penoaks.com/images/ramencon/";

	private static File cacheDirectory = new File( ContentManager.getCacheDir(), "imageCache" );
	private static BitmapFactory.Options options = new BitmapFactory.Options();

	static
	{
		if ( cacheDirectory.isFile() )
			cacheDirectory.delete();

		if ( !cacheDirectory.exists() )
			cacheDirectory.mkdirs();

		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	}

	public static void cacheRemoteImage( Context context, String id, String remoteUrl, boolean forceUpdate, ImageResolveTask.ImageFoundListener foundListener, ImageResolveTask.ImageProgressListener progressListener )
	{
		new ImageResolveTask( context, id, remoteUrl, forceUpdate, foundListener, progressListener ).executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	private ImageCache()
	{

	}

	public static class ImageResolveTask extends AsyncTask<Void, Void, Void>
	{
		Context context;
		boolean forceUpdate;
		ImageFoundListener foundListener;
		String id;
		long progressBytesTransferred = 0;
		ImageProgressListener progressListener;
		long progressTotalByteCount = 0;
		String remoteUrl;
		Bitmap resultBitmap;
		Exception resultError;
		boolean waiting = false;

		ImageResolveTask( Context context, String id, String remoteUrl, boolean forceUpdate, ImageFoundListener foundListener, ImageProgressListener progressListener )
		{
			this.context = context;
			this.id = id;
			this.remoteUrl = remoteUrl;
			this.forceUpdate = forceUpdate;
			this.foundListener = foundListener;
			this.progressListener = progressListener;
		}

		@Override
		protected Void doInBackground( Void... params )
		{
			boolean downloadImages = ContentManager.downloadImages();

			final File dest = new File( cacheDirectory, id + ".png" );
			boolean update = false;

			if ( !downloadImages )
				dest.delete();

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

				Ion.with( context ).load( remoteUrl ).progress( new ProgressCallback()
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
							if ( downloadImages )
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
									ACRAHelper.handleExceptionOnce( "IMAGE_CACHE_" + id, new RuntimeException( "Recoverable Exception", exception ) );
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

		@Override
		protected void onPostExecute( Void aVoid )
		{
			super.onPostExecute( aVoid );

			if ( progressListener != null )
				progressListener.finish();
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			if ( progressListener != null )
				progressListener.start();
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

		public interface ImageFoundListener
		{
			void error( Exception exception );

			void update( Bitmap bitmap );
		}

		public interface ImageProgressListener
		{
			void finish();

			void progress( long bytesTransferred, long totalByteCount );

			void start();
		}
	}
}
