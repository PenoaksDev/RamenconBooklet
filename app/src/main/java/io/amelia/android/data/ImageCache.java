package io.amelia.android.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.acra.ACRA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import io.amelia.android.log.PLog;
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.Booklet;
import io.amelia.booklet.data.ContentManager;

public class ImageCache
{
	public static final String REMOTE_IMAGES_URL = "http://booklet.dev.penoaks.com/images/";

	private static BitmapFactory.Options options = new BitmapFactory.Options();

	static
	{
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	}

	public static void cacheRemoteImage( Context context, String id, String remoteUrl, String localName, String bookletId, boolean forceUpdate, ImageFoundListener foundListener, @Nullable ImageProgressListener progressListener )
	{
		new ImageResolveTask( context, id, remoteUrl, localName, bookletId, forceUpdate, foundListener, progressListener ).executeOnExecutor( ContentManager.getExecutorThreadPool() );
	}

	private ImageCache()
	{

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

	public static class ImageResolveTask extends AsyncTask<Void, Void, Void>
	{
		final String bookletId;
		Context context;
		boolean forceUpdate;
		ImageFoundListener foundListener;
		String id;
		String localName;
		long progressBytesTransferred = 0;
		ImageProgressListener progressListener;
		long progressTotalByteCount = 0;
		String remoteUrl;
		Bitmap resultBitmap;
		Exception resultError;
		boolean waiting = false;

		ImageResolveTask( Context context, String id, String remoteUrl, String localName, String bookletId, boolean forceUpdate, ImageFoundListener foundListener, @Nullable ImageProgressListener progressListener )
		{
			Objs.notEmpty( context );
			Objs.notEmpty( id );
			Objs.notEmpty( remoteUrl );
			Objs.notEmpty( localName );
			Objs.notNull( foundListener );

			this.context = context;
			this.id = id;
			this.remoteUrl = remoteUrl;
			this.localName = localName;
			this.bookletId = bookletId;
			this.forceUpdate = forceUpdate;
			this.foundListener = foundListener;
			this.progressListener = progressListener;
		}

		@Override
		protected Void doInBackground( Void... params )
		{
			int imageCacheTimeout = ContentManager.getImageCacheTimeout() * 60 * 1000; // Minutes to Millis

			final File dest = new File( bookletId == null ? ContentManager.getActiveBooklet().getDataDirectory() : Booklet.getBooklet( bookletId ).getDataDirectory(), localName );
			dest.getParentFile().mkdirs();

			boolean update = false;

			if ( imageCacheTimeout == 0 )
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

				if ( System.currentTimeMillis() - dest.lastModified() > imageCacheTimeout )
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
							if ( imageCacheTimeout > 0 )
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

	}
}
