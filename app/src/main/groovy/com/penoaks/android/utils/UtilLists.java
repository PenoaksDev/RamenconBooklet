/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.android.utils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public class UtilLists
{
	private UtilLists()
	{
	}

	public static boolean incremented( Set<String> values )
	{
		Set<Integer> numbers = new TreeSet<>();

		int lowest = -1;

		for ( String s : values )
			try
			{
				int n = Integer.parseInt( s );
				if ( lowest < 0 || n < lowest )
					lowest = n;
				numbers.add( n );
			}
			catch ( NumberFormatException e )
			{
				return false; // String is not a number, auto disqualified
			}

		for ( int i = lowest; i < numbers.size(); i++ )
		{
			if ( !numbers.contains( i ) )
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Cycles through list of elements until lambda function returns non-null.
	 * If the list doesn't contain the returned value, the old value is replaced.
	 *
	 * @param list     The original list
	 * @param function The matching function
	 * @param <V>      Contained list type
	 * @return The matching value
	 */
	public static <V> V findAndReplace( List<V> list, Function<? super V, ? extends V> function )
	{
		UtilObjects.notNull( function );

		for ( V oldValue : list )
		{
			V newValue = function.apply( oldValue );
			if ( newValue != null )
			{
				if ( !list.contains( newValue ) )
				{
					list.remove( oldValue );
					list.add( newValue );
				}
				return newValue;
			}
		}

		return null;
	}

	public static <V> V find( List<V> list, Function<? super V, Boolean> function )
	{
		UtilObjects.notNull( function );

		for ( V value : list )
			if ( function.apply( value ) )
				return value;

		return null;
	}

	public static <V> V findOrNew( List<V> list, Function<? super V, Boolean> function, V newValue )
	{
		V find = find( list, function );
		if ( find == null )
		{
			list.add( newValue );
			find = newValue;
		}
		return find;
	}

	public static <V> V add( List<V> list, V obj )
	{
		UtilObjects.notNull( list );
		UtilObjects.notNull( obj );

		list.add( obj );
		return obj;
	}
}
