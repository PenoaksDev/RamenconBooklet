package io.amelia.android.files;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.support.LibIO;
import io.amelia.android.support.Objs;

public class FileBuilder
{
	// public static final BitmapFactory.Options BITMAP_FACTORY_OPTIONS = new BitmapFactory.Options();
	public static final String REMOTE_IMAGES_URL = "http://booklet.dev.penoaks.com/images/";

	final String id;
	long cacheTimeout = 0;
	boolean forceDownload = false;
	File localFile;
	ProgressListener progressListener;
	String remoteFile = null;
	List<FileResultConsumer> resultConsumerList = new ArrayList<>();
	BiConsumer<String, Exception> resultExceptionConsumer;

	public FileBuilder( String id )
	{
		this.id = id;
	}

	FileRequest apply() throws FileBuilderException
	{
		FileRequest request = new FileRequest( this );

		if ( localFile == null && remoteFile == null )
			throw new FileBuilderException( "Both local file and remote file are null!" );
		if ( forceDownload && remoteFile == null )
			throw new FileBuilderException( "You force remote download without providing a remote url!" );
		if ( cacheTimeout >= 0 && ( localFile == null || remoteFile == null ) )
			throw new FileBuilderException( "You can't set a cacheTimeout without providing a local file and/or remote url!" );

		if ( localFile.exists() )
		{
			request.exists = true;
			if ( forceDownload )
				request.download = true;
			else if ( cacheTimeout > 0 && remoteFile != null )
			{
				if ( System.currentTimeMillis() - localFile.lastModified() > cacheTimeout )
					request.download = true;
				else
					request.download = false;
			}
			else
				request.download = false;
		}
		else if ( remoteFile != null )
			request.download = true;

		try
		{
			for ( FileResultConsumer fileResultConsumer : resultConsumerList )
				fileResultConsumer.verify( this );
		}
		catch ( Exception e )
		{
			throw new FileBuilderException( "FileBuilder failed verification!", e );
		}

		return request;
	}

	public FileBuilder forceDownload( boolean forceDownload )
	{
		this.forceDownload = forceDownload;
		return this;
	}

	public FileRequestHandler request() throws FileBuilderException
	{
		return new FileRequestHandler().enqueueBuilder( this );
	}

	public FileBuilder withCacheTimeout( long cacheTimeout )
	{
		// 0 = Disable
		this.cacheTimeout = cacheTimeout;
		return this;
	}

	public FileBuilder withExceptionHandler( BiConsumer<String, Exception> exceptionConsumer )
	{
		this.resultExceptionConsumer = exceptionConsumer;
		return this;
	}

	public FileBuilder withFileResultConsumer( FileResultConsumer fileResultConsumer )
	{
		resultConsumerList.add( fileResultConsumer );
		return this;
	}

	public FileBuilder withImageView( @NonNull ImageView imageView ) throws FileBuilderException
	{
		Objs.notNull( imageView );

		withFileResultConsumer( new FileResultConsumer()
		{
			private Bitmap result;

			@Override
			public void accept( FileRequest request ) throws Exception
			{
				if ( result == null )
					throw new IllegalStateException( "There was an internal exception." );

				imageView.setImageBitmap( result );
			}

			boolean isImage( @NonNull String fileName )
			{
				for ( String ext : new String[] {"jpg", "jpeg", "png", "bmp", "gif"} )
					if ( fileName.toLowerCase().endsWith( ext ) )
						return true;
				return false;
			}

			@Override
			public void process( FileRequest request ) throws Exception
			{
				result = BitmapFactory.decodeStream( request.result.resultInputStream() );
			}

			@Override
			public void verify( FileBuilder fileBuilder ) throws Exception
			{
				if ( fileBuilder.remoteFile != null && !isImage( fileBuilder.remoteFile ) )
					throw new IllegalArgumentException( "Remote file " + fileBuilder.remoteFile + " does not appear to be an image!" );
				if ( fileBuilder.localFile != null && !isImage( fileBuilder.localFile.getName() ) )
					throw new IllegalArgumentException( "Local file " + fileBuilder.localFile + " does not appear to be an image!" );
			}
		} );

		return this;
	}

	public FileBuilder withLocalFile( @NonNull File localFile )
	{
		this.localFile = localFile;

		withFileResultConsumer( new FileResultConsumer()
		{
			@Override
			public void accept( FileRequest request ) throws Exception
			{
				if ( request.state == FileRequestState.FINISHED )
				{
					if ( localFile == null )
						return;
					LibIO.writeBytesToFile( localFile, request.result.resultBytes() );
				}
			}

			@Override
			public void process( FileRequest request ) throws Exception
			{

			}
		} );

		return this;
	}

	public FileBuilder withProgressListener( ProgressListener progressListener )
	{
		this.progressListener = progressListener;
		return this;
	}

	public FileBuilder withRemoteFile( String remoteFile )
	{
		this.remoteFile = remoteFile;
		return this;
	}

	public interface ProgressListener
	{
		void finish( FileResult result );

		void progress( FileResult result, long bytesTransferred, long totalByteCount, boolean indeterminate );

		void start( FileResult result );
	}

	public class FileBuilderException extends Exception
	{
		public FileBuilderException( String message )
		{
			super( message );
		}

		public FileBuilderException( String message, Exception exception )
		{
			super( message, exception );
		}

		public FileBuilder getFileBuilder()
		{
			return FileBuilder.this;
		}

		public String getId()
		{
			return getFileBuilder().id;
		}
	}
}
