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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Maps
{
	/**
	 * Checks and converts the string key to an integer. Non-numeric keys are removed from the treemap.
	 *
	 * @param map The map to sort
	 * @param <T> The value type
	 * @return The sorted map as a TreeMap
	 */
	public static <T> Map<Integer, T> asNumericallySortedMap( final Map<String, T> map )
	{
		return new TreeMap<Integer, T>()
		{{
			for ( Map.Entry<String, T> entry : map.entrySet() )
			{
				if ( Numbers.isNumber( entry.getKey() ) )
					put( Integer.parseInt( entry.getKey() ), entry.getValue() );
			}
		}};
	}

	private static void flattenMap( Map<String, Object> result, String path, Map<String, Object> map )
	{
		for ( Map.Entry<String, Object> entry : map.entrySet() )
		{
			String key = path.isEmpty() ? entry.getKey() : path + "/" + entry.getKey();

			if ( entry.getValue() instanceof Map )
				flattenMap( result, key, ( Map<String, Object> ) entry.getValue() );
			else
				result.put( key, entry.getValue() );
		}
	}

	public static Map<String, Object> flattenMap( Map<String, Object> map )
	{
		Map<String, Object> result = new HashMap<>();
		flattenMap( result, "", map );
		return result;
	}

	public static <V> Map<Integer, V> incrementedMap( List<V> values )
	{
		return new HashMap<Integer, V>()
		{{
			for ( int i = 0; i < values.size(); i++ )
				put( i, values.get( i ) );
		}};
	}

	public static <K, V> HashMap<K, V> newHashMap( final K key, final V value )
	{
		return new HashMap<K, V>()
		{{
			put( key, value );
		}};
	}

	private Maps()
	{
	}
}
