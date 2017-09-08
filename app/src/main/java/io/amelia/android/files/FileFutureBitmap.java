package io.amelia.android.files;

import android.graphics.Bitmap;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.FileNotFoundException;

import io.amelia.android.log.PLog;
import io.amelia.android.support.LibIO;
import io.amelia.booklet.data.ContentManager;

public class FileFutureBitmap extends FileFuture
{
	public Bitmap bitmap;

	@Override
	public void handle( FileHandlerTask task ) throws Exception
	{
		if ( !fileBuilder.exists && !fileBuilder.download )
			setException( new FileNotFoundException( LibIO.relPath( fileBuilder.localFile, ContentManager.getCacheDirectory() ) + " does not exist!" ) );

		task.doUpdate( this, FileFutureUpdateType.START );

		if ( fileBuilder.exists )
		{
			Ion.with( ContentManager.getApplicationContext() ).load( fileBuilder.localFile ).asBitmap().setCallback( new FutureCallback<Bitmap>()
			{
				@Override
				public void onCompleted( Exception e, Bitmap result )
				{
					if ( e != null )
						setException( e );
					else
						bitmap = result;

					if ( fileBuilder.download )
						task.doUpdate( FileFutureBitmap.this, FileFutureUpdateType.PRELOAD );
					else
					{
						task.doUpdate( FileFutureBitmap.this, FileFutureUpdateType.FINISHED );
						finished = true;
					}
				}
			} );
		}

		if ( fileBuilder.download )
		{
			Ion.with( ContentManager.getApplicationContext() ).load( fileBuilder.remoteFile ).progress( new ProgressCallback()
			{
				@Override
				public void onProgress( long downloaded, long total )
				{
					bytesReceived = downloaded;
					bytesLength = total;
					task.doUpdate( FileFutureBitmap.this, FileFutureUpdateType.PROGRESS );
				}
			} ).asBitmap().setCallback( new FutureCallback<Bitmap>()
			{
				@Override
				public void onCompleted( Exception e, Bitmap result )
				{
					if ( e != null )
						setException( e );
					if ( result != null )
						bitmap = result;

					task.doUpdate( FileFutureBitmap.this, FileFutureUpdateType.FINISHED );
					finished = true;

					task.doPollNext();
				}
			} );
		}
	}
}
