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

import android.support.annotation.NonNull;

import com.penoaks.android.utils.UtilObjects;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a self garbage collecting List, that is Thread-safe
 */
public class WeakReferenceList<V> implements List<V>
{
	private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

	static
	{
		new CleanupThread().start();
	}

	private final List<GarbageReference<V>> list = new CopyOnWriteArrayList<>();

	public void clear()
	{
		list.clear();
	}

	public V get( int index )
	{
		return list.get( index ).get();
	}

	@Override
	public V set( int index, V element )
	{
		GarbageReference<V> old = list.set( index, new GarbageReference<>( element, list ) );
		return old == null ? null : old.get();
	}

	public V remove( int index )
	{
		return list.remove( index ).get();
	}

	@Override
	public int indexOf( Object o )
	{
		return list().indexOf( o );
	}

	@Override
	public int lastIndexOf( Object o )
	{
		return list().lastIndexOf( o );
	}

	@Override
	public ListIterator<V> listIterator()
	{
		return list().listIterator();
	}

	@NonNull
	@Override
	public ListIterator<V> listIterator( int index )
	{
		return list().listIterator( index );
	}

	@NonNull
	@Override
	public List<V> subList( int fromIndex, int toIndex )
	{
		return list().subList( fromIndex, toIndex );
	}

	@NonNull
	@Override
	public <T> T[] toArray( @NonNull T[] a )
	{
		return list().toArray( a );
	}

	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public boolean contains( Object obj )
	{
		for ( GarbageReference<V> ref : list )
			if ( ref.get() == obj )
				return true;
		return list.contains( obj );
	}

	@Override
	public boolean remove( Object obj )
	{
		for ( GarbageReference<V> ref : list )
			if ( ref.get() == obj )
			{
				list.remove( ref );
				return true;
			}
		return list.remove( obj );
	}

	public List<V> list()
	{
		List<V> values = new ArrayList<>();
		for ( GarbageReference<V> ref : list )
			if ( ref.get() != null )
				values.add( ( V ) ref.get() );
		return values;
	}

	public Set<V> set()
	{
		Set<V> values = new HashSet<>();
		for ( GarbageReference<V> ref : list )
			if ( ref.get() != null )
				values.add( ref.get() );
		return values;
	}

	@Override
	public Iterator<V> iterator()
	{
		return set().iterator();
	}

	@NonNull
	@Override
	public Object[] toArray()
	{
		return list().toArray();
	}

	@Override
	public void add( int index, V element )
	{
		UtilObjects.notNull( element );
		list.add( index, new GarbageReference<>( element, list ) );
	}

	public boolean add( V element )
	{
		UtilObjects.notNull( element );
		return list.add( new GarbageReference<>( element, list ) );
	}

	@Override
	public boolean containsAll( @NonNull Collection<?> values )
	{
		return list().containsAll( values );
	}

	@Override
	public boolean addAll( @NonNull Collection<? extends V> values )
	{
		for ( V v : values )
			add( v );
		return true;
	}

	@Override
	public boolean addAll( int index, @NonNull Collection<? extends V> values )
	{
		AtomicInteger i = new AtomicInteger( index );
		for ( V v : values )
			add( i.getAndIncrement(), v );
		return true;
	}

	@Override
	public boolean removeAll( @NonNull Collection<?> values )
	{
		for ( Object obj : values )
			remove( obj );
		return true;
	}

	@Override
	public boolean retainAll( @NonNull Collection<?> values )
	{
		for ( GarbageReference<V> ref : list )
			if ( !values.contains( ref.get() ) )
				list.remove( ref );
		return true;
	}

	static class GarbageReference<V> extends WeakReference<V>
	{
		final List<GarbageReference<V>> list;

		GarbageReference( V value, List<GarbageReference<V>> list )
		{
			super( value, referenceQueue );
			this.list = list;
		}
	}

	static class CleanupThread extends Thread
	{
		CleanupThread()
		{
			setPriority( Thread.MAX_PRIORITY );
			setName( "GarbageCollectingList-CleanupThread" );
			// setDaemon( true );
		}

		@Override
		public void run()
		{
			while ( true )
			{
				try
				{
					GarbageReference<?> ref;
					while ( true )
					{
						ref = ( GarbageReference<?> ) referenceQueue.remove();
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
