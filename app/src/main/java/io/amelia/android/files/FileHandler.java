package io.amelia.android.files;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import io.amelia.android.backport.MapsBackport;
import io.amelia.android.backport.function.Function;
import io.amelia.booklet.data.ContentManager;

public class FileHandler
{
	private final Map<FileFuturePriority, TreeMap<Integer, FileFuture>> builders = new HashMap<>();
	private FileHandlerTask currentTask;
	private FileFutureProgressListener fileFutureProgressListener = null;

	public void cancelAll()
	{
		if ( currentTask != null )
			currentTask.cancel( true );

		/* if ( httpClient != null )
		{
			httpClient.dispatcher().cancelAll();
			httpClient.connectionPool().evictAll();
		} */
	}

	public FileHandler enqueueBuilder( FileBuilder fileBuilder ) throws FileBuilderException
	{
		return enqueueBuilder( FileFuturePriority.NORMAL, fileBuilder );
	}

	public FileHandler enqueueBuilder( FileFuturePriority priority, FileBuilder fileBuilder ) throws FileBuilderException
	{
		TreeMap<Integer, FileFuture> map = MapsBackport.computeIfAbsent( builders, priority, new Function<FileFuturePriority, TreeMap<Integer, FileFuture>>()
		{
			@Override
			public TreeMap<Integer, FileFuture> apply( FileFuturePriority priority )
			{
				return new TreeMap<>();
			}
		} );

		map.put( map.size(), fileBuilder.apply() );

		return this;
	}

	public FileHandler start( FileFutureMode mode )
	{
		if ( builders.size() == 0 )
			return this;

		if ( currentTask != null )
			throw new IllegalStateException( "The FileRequest has already been started!" );

		Map<FileFuturePriority, TreeMap<Integer, FileFuture>> buildersNew = new HashMap<>();

		for ( FileFuturePriority priority : FileFuturePriority.values() )
			if ( builders.containsKey( priority ) )
				MapsBackport.computeIfAbsent( buildersNew, priority, new Function<FileFuturePriority, TreeMap<Integer, FileFuture>>()
				{
					@Override
					public TreeMap<Integer, FileFuture> apply( FileFuturePriority fileFuturePriority )
					{
						return new TreeMap<>();
					}
				} ).putAll( builders.get( priority ) );

		currentTask = new FileHandlerTask( buildersNew, mode, fileFutureProgressListener );
		currentTask.executeOnExecutor( ContentManager.getExecutorThreadPool() );

		return this;
	}

	public FileHandler start()
	{
		return start( FileFutureMode.ASYNC );
	}

	public FileHandler withProgressListener( FileFutureProgressListener fileFutureProgressListener )
	{
		this.fileFutureProgressListener = fileFutureProgressListener;
		return this;
	}
}
