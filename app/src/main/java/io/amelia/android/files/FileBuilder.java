package io.amelia.android.files;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.log.PLog;
import io.amelia.android.support.LibIO;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.ContentManager;

public class FileBuilder<FutureType extends FileFuture>
{
	public static final BitmapFactory.Options BITMAP_FACTORY_OPTIONS = new BitmapFactory.Options();
	public static final String REMOTE_IMAGES_URL = "http://booklet.dev.penoaks.com/images/";
	final Class<FutureType> futureTypeClass;
	final String id;
	long cacheTimeout = 0;
	boolean download;
	boolean exists;
	FileFutureProgressListener fileFutureProgressListener;
	boolean forceDownload = false;
	List<FileFutureConsumer<?>> futureConsumerList = new ArrayList<>();
	File localFile;
	String remoteFile = null;
	BiConsumer<String, Throwable> resultExceptionConsumer;

	public FileBuilder( String id, Class<FutureType> futureTypeClass )
	{
		this.id = id;
		this.futureTypeClass = futureTypeClass;
	}

	FutureType apply() throws FileBuilderException
	{
		if ( localFile == null && remoteFile == null )
			throw new FileBuilderException( this, "Both local file and remote file are null!" );
		if ( forceDownload && remoteFile == null )
			throw new FileBuilderException( this, "You force remote download without providing a remote url!" );
		if ( cacheTimeout >= 0 && ( localFile == null || remoteFile == null ) )
			throw new FileBuilderException( this, "You can't set a cacheTimeout without providing a local file and/or remote url!" );

		if ( localFile.exists() )
		{
			exists = true;
			if ( forceDownload )
			{
				PLog.i( "File " + remoteFile + " requested: {result: local, exists: yes, timeout: forced}" );
				download = true;
			}
			else if ( cacheTimeout > 0 && remoteFile != null )
			{
				if ( System.currentTimeMillis() - localFile.lastModified() > cacheTimeout )
				{
					PLog.i( "File " + remoteFile + " requested: {result: local, exists: yes, timeout: true} // " + localFile.lastModified() + " -- " + cacheTimeout + " // " + ( System.currentTimeMillis() - localFile.lastModified() ) );
					download = true;
				}
				else
				{
					PLog.i( "File " + remoteFile + " requested: {result: local, exists: yes, timeout: no}" );
					download = false;
				}
			}
			else
			{
				PLog.i( "File " + remoteFile + " requested: {result: local, exists: yes, timeout: no}" );
				download = false;
			}
		}
		else if ( remoteFile != null )
		{
			PLog.i( "File " + remoteFile + " requested: {result: download, exists: no, timeout: no}" );
			download = true;
		}

		try
		{
			for ( FileFutureConsumer fileFutureConsumer : futureConsumerList )
				fileFutureConsumer.verify( this );
		}
		catch ( Exception e )
		{
			throw new FileBuilderException( this, "FileBuilder failed verification!", e );
		}

		try
		{
			Constructor<FutureType> constructor = futureTypeClass.getDeclaredConstructor();
			FutureType instance = constructor.newInstance();
			instance.fileBuilder = ( FileBuilder<FileFuture> ) this;
			return instance;
		}
		catch ( Exception e )
		{
			throw new FileBuilderException( this, "Expecting a no-arg constructor for FileFuture " + futureTypeClass.getSimpleName(), e );
		}
	}

	public FileBuilder forceDownload( boolean forceDownload )
	{
		this.forceDownload = forceDownload;
		return this;
	}

	public FileHandler request() throws FileBuilderException
	{
		return new FileHandler().enqueueBuilder( this );
	}

	public FileBuilder withCacheTimeout( long cacheTimeout )
	{
		// 0 = Disable
		this.cacheTimeout = cacheTimeout;
		return this;
	}

	public FileBuilder withExceptionHandler( BiConsumer<String, Throwable> exceptionConsumer )
	{
		this.resultExceptionConsumer = exceptionConsumer;
		return this;
	}

	public FileBuilder withFileResultConsumer( FileFutureConsumer<?> fileFutureConsumer )
	{
		futureConsumerList.add( fileFutureConsumer );
		return this;
	}

	public FileBuilder withImageView( @NonNull ImageView imageView ) throws FileBuilderException
	{
		Objs.notNull( imageView );

		if ( !FileFutureBitmap.class.isAssignableFrom( futureTypeClass ) )
			throw new FileBuilderException( this, "FileBuilder.withImageView() can only be used with FileFutureBitmap type." );

		withFileResultConsumer( new FileFutureConsumer<FileFutureBitmap>()
		{
			@Override
			public void accept( FileFutureBitmap request, FileFutureUpdateType updateType ) throws Exception
			{
				imageView.setImageBitmap( request.bitmap );
			}

			@Override
			public void verify( FileBuilder fileBuilder ) throws Exception
			{
				if ( fileBuilder.remoteFile != null && !LibIO.isImage( fileBuilder.remoteFile ) )
					throw new IllegalArgumentException( "Remote file " + fileBuilder.remoteFile + " does not appear to be an image!" );
				if ( fileBuilder.localFile != null && !LibIO.isImage( fileBuilder.localFile.getName() ) )
					throw new IllegalArgumentException( "Local file " + fileBuilder.localFile + " does not appear to be an image!" );
			}
		} );

		return this;
	}

	public FileBuilder withLocalFile( @NonNull File localFile ) throws FileBuilderException
	{
		this.localFile = localFile;

		if ( !FileFutureBitmap.class.isAssignableFrom( futureTypeClass ) )
			throw new FileBuilderException( this, "Selected FileFuture is not compatible withLocalFile()." );

		withFileResultConsumer( new FileFutureConsumer<FutureType>()
		{
			@Override
			public void accept( FutureType request, FileFutureUpdateType updateType ) throws Exception
			{
				if ( request.fileBuilder.download && updateType == FileFutureUpdateType.FINISHED )
				{
					PLog.i( "Saving file " + remoteFile + " to " + LibIO.relPath( localFile, ContentManager.getCacheDirectory() ) );

					if ( localFile == null )
						return;
					if ( request instanceof FileFutureBitmap )
					{
						String name = localFile.getName();
						if ( !LibIO.isImage( name ) )
							throw new IllegalArgumentException( "Local file " + localFile + " does not appear to be an image!" );
						localFile.getParentFile().mkdirs();
						if ( !localFile.exists() )
							localFile.createNewFile();
						( ( FileFutureBitmap ) request ).bitmap.compress( name.toLowerCase().endsWith( "png" ) ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 93, new FileOutputStream( localFile ) );
					}
				}
			}
		} );

		return this;
	}

	public FileBuilder withProgressListener( FileFutureProgressListener fileFutureProgressListener )
	{
		this.fileFutureProgressListener = fileFutureProgressListener;
		return this;
	}

	public FileBuilder withRemoteFile( String remoteFile )
	{
		this.remoteFile = remoteFile;
		return this;
	}
}
