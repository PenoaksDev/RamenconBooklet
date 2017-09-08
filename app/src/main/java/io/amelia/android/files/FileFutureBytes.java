package io.amelia.android.files;

import android.graphics.Bitmap;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.amelia.android.support.LibIO;
import io.amelia.booklet.data.ContentManager;

public class FileFutureBytes extends FileFuture
{
	public byte[] bytes;

	@Override
	public void handle( FileHandlerTask task ) throws Exception
	{
		if ( !fileBuilder.exists && !fileBuilder.download )
			setException( new FileNotFoundException( LibIO.relPath( fileBuilder.localFile, ContentManager.getCacheDirectory() ) + " does not exist!" ) );

		task.doUpdate( this, FileFutureUpdateType.START );

		if ( fileBuilder.exists )
		{
			Ion.with( ContentManager.getApplicationContext() ).load( fileBuilder.localFile ).asByteArray().setCallback( new FutureCallback<byte[]>()
			{
				@Override
				public void onCompleted( Exception e, byte[] result )
				{
					if ( e != null )
						setException( e );
					else
						bytes = result;

					if ( fileBuilder.download )
						task.doUpdate( FileFutureBytes.this, FileFutureUpdateType.PRELOAD );
					else
					{
						task.doUpdate( FileFutureBytes.this, FileFutureUpdateType.FINISHED );
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
					task.doUpdate( FileFutureBytes.this, FileFutureUpdateType.PROGRESS );
				}
			} ).asByteArray().setCallback( new FutureCallback<byte[]>()
			{
				@Override
				public void onCompleted( Exception e, byte[] result )
				{
					if ( e != null )
						setException( e );
					if ( result != null )
						bytes = result;

					task.doUpdate( FileFutureBytes.this, FileFutureUpdateType.FINISHED );
					finished = true;

					task.doPollNext();
				}
			} );
		}
	}

	public byte[] resultBytes() throws IOException
	{
		return bytes;
	}

	public InputStream resultInputStream() throws IOException
	{
		return new ByteArrayInputStream( bytes );
	}

	public String resultString() throws IOException
	{
		return new String( bytes, Charset.defaultCharset() );
	}
}
