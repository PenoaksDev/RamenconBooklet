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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * http://java.dzone.com/articles/letting-garbage-collector-do-c
 */
@SuppressWarnings( {"rawtypes", "unchecked"} )
public class GarbageCollectingMap<K, V>
{
	private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
	
	static
	{
		new CleanupThread().start();
	}
	
	private final ConcurrentMap<K, GarbageReference<K, V>> map = new ConcurrentHashMap<K, GarbageReference<K, V>>();
	
	public void clear()
	{
		map.clear();
	}
	
	public V get( K key )
	{
		GarbageReference<K, V> ref = map.get( key );
		return ref == null ? null : ref.value;
	}
	
	public Object getGarbageObject( K key )
	{
		GarbageReference<K, V> ref = map.get( key );
		return ref == null ? null : ref.get();
	}
	
	public Collection<K> keySet()
	{
		return map.keySet();
	}
	
	public void put( K key, V value, Object garbageObject )
	{
		if ( key == null || value == null || garbageObject == null )
			throw new NullPointerException();
		if ( key == garbageObject )
			throw new IllegalArgumentException( "key can't be equal to garbageObject for gc to work" );
		if ( value == garbageObject )
			throw new IllegalArgumentException( "value can't be equal to garbageObject for gc to work" );
		
		GarbageReference<K, V> reference = new GarbageReference( garbageObject, key, value, map );
		map.put( key, reference );
	}
	
	static class GarbageReference<K, V> extends WeakReference<Object>
	{
		final K key;
		final V value;
		final ConcurrentMap<K, V> map;
		
		GarbageReference( Object referent, K key, V value, ConcurrentMap<K, V> map )
		{
			super( referent, referenceQueue );
			this.key = key;
			this.value = value;
			this.map = map;
		}
	}
	
	static class CleanupThread extends Thread
	{
		CleanupThread()
		{
			setPriority( Thread.MAX_PRIORITY );
			setName( "GarbageCollectingConcurrentMap-CleanupThread" );
			// setDaemon( true );
		}
		
		@Override
		public void run()
		{
			for ( ;; )
			{
				try
				{
					GarbageReference ref = ( GarbageReference ) referenceQueue.remove();
					while ( true )
					{
						ref.map.remove( ref.key );
						ref = ( GarbageReference ) referenceQueue.remove();
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
