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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 */
public class IndexedWeakHashMap<V> extends WeakHashMap<Integer, V>
{
	/**
	 * Reorganizes the stack from 0 to size()
	 */
	private void collapse()
	{
		Map<Integer, V> dup = new HashMap<>();
		dup.putAll( this );
		clear();
		int i = 0;
		for ( int k : dup.keySet() )
			if ( dup.get( k ) != null )
				put( i, dup.remove( k ) );
	}

	public V getFirst()
	{
		collapse();
		return get( 0 );
	}

	public V getLast()
	{
		collapse();
		return get( size() );
	}

	@Override
	public V put( Integer key, V value )
	{
		V old = super.put( key, value );
		collapse();
		return old;
	}

	public void put( V value )
	{
		collapse();
		super.put( size(), value );
	}

	@Override
	public void putAll( Map<? extends Integer, ? extends V> m )
	{
		super.putAll( m );
		collapse();
	}

	@Override
	public V remove( Object i )
	{
		V old = super.remove( i );
		collapse();
		return old;
	}
}
