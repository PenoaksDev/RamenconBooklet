/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package com.penoaks.android.helpers;

import com.penoaks.android.utils.UtilObjects;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements a self garbage collecting List, that is Thread-safe
 */
public class GarbageCollectingList<V, G> implements Iterable<V>
{
	private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

	static
	{
		new CleanupThread().start();
	}

	private final List<GarbageReference<V, G>> list = new CopyOnWriteArrayList<>();

	public void clear()
	{
		list.clear();
	}

	public V get( int index )
	{
		return list.get( index ).value;
	}

	public G getGarbageObject( int index )
	{
		return list.get( index ).get();
	}

	public Set<V> toSet()
	{
		Set<V> values = new HashSet<>();
		for ( GarbageReference<V, G> v : list )
			values.add( v.value );
		return values;
	}

	@Override
	public Iterator<V> iterator()
	{
		return toSet().iterator();
	}

	public void addAll( Iterable<V> values, G garbageObject )
	{
		for ( V v : values )
			add( v, garbageObject );
	}

	public void add( V value, G garbageObject )
	{
		UtilObjects.notNull( garbageObject );
		UtilObjects.notNull( value );

		if ( value == garbageObject )
			throw new IllegalArgumentException( "value can't be equal to garbageObject for gc to work" );

		GarbageReference<V, G> reference = new GarbageReference<V, G>( garbageObject, value, list );
		list.add( reference );
	}

	static class GarbageReference<V, G> extends WeakReference<G>
	{
		final V value;
		final List<GarbageReference<V, G>> list;

		GarbageReference( G referent, V value, List<GarbageReference<V, G>> list )
		{
			super( referent, referenceQueue );
			this.value = value;
			this.list = list;
		}
	}

	static class CleanupThread extends Thread
	{
		CleanupThread()
		{
			setPriority( Thread.MAX_PRIORITY );
			setName( "GarbageCollectingList-CleanupThread" );
			setDaemon( true );
		}

		@Override
		public void run()
		{
			while ( true )
			{
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
	}
}
