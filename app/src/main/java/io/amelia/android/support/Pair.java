/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.android.support;

import java.util.Map;

/**
 * Represents a pair with a Key and Value Object
 */
public class Pair<K, V> implements Map.Entry<K, V>
{
	private K key;
	private V val;

	public Pair()
	{
		this.key = null;
		this.val = null;
	}

	public Pair( K key, V val )
	{
		this.key = key;
		this.val = val;
	}

	@Override
	public K getKey()
	{
		return key;
	}

	public void setKey( K key )
	{
		this.key = key;
	}

	@Override
	public V getValue()
	{
		return val;
	}

	@Override
	public V setValue( V val )
	{
		V old = this.val;
		this.val = val;
		return old;
	}
}
