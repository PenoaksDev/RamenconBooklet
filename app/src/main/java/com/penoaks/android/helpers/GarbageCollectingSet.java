/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.android.helpers;

import com.penoaks.android.utils.UtilObjects;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Implements a self garbage collecting Set, that is Thread-safe
 */
public class GarbageCollectingSet<V, G> implements Iterable<V>
{
	static class CleanupThread extends Thread
	{
		CleanupThread()
		{
			setPriority( Thread.MAX_PRIORITY );
			setName( "GarbageCollectingSet-cleanupthread" );
			setDaemon( true );
		}

		@Override
		public void run()
		{
			while ( true )
				try
				{
					GarbageReference<?, ?> ref;
					while ( true )
					{
						ref = ( GarbageReference<?, ?> ) referenceQueue.remove();
						ref.list.remove( ref );
					}
				}
				catch ( InterruptedException e )
				{
					// ignore
				}
		}
	}

	static class GarbageReference<V, G> extends WeakReference<G>
	{
		final V value;
		final Set<GarbageReference<V, G>> list;

		GarbageReference( G referent, V value, Set<GarbageReference<V, G>> list )
		{
			super( referent, referenceQueue );
			this.value = value;
			this.list = list;
		}
	}

	private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

	static
	{
		new CleanupThread().start();
	}

	private final Set<GarbageReference<V, G>> list = new CopyOnWriteArraySet<>();

	public void add( V value, G garbageObject )
	{
		UtilObjects.notNull( garbageObject );
		UtilObjects.notNull( value );

		if ( value == garbageObject )
			throw new IllegalArgumentException( "value can't be equal to garbageObject for gc to work" );

		GarbageReference<V, G> reference = new GarbageReference<V, G>( garbageObject, value, list );
		list.add( reference );
	}

	public void addAll( Iterable<V> values, G garbageObject )
	{
		for ( V v : values )
			add( v, garbageObject );
	}

	public void clear()
	{
		list.clear();
	}

	@Override
	public Iterator<V> iterator()
	{
		return toSet().iterator();
	}

	public Set<V> toSet()
	{
		Set<V> values = new HashSet<V>();
		for ( GarbageReference<V, G> v : list )
			values.add( v.value );
		return values;
	}
}
