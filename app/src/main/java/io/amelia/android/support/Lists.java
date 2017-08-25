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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Lists
{
	public static <V> V add( List<V> list, V obj )
	{
		Objs.notNull( list );
		Objs.notNull( obj );

		list.add( obj );
		return obj;
	}

	public static <T> T first( Collection<T> obj )
	{
		if ( obj.size() == 0 )
			return null;
		return ( T ) obj.toArray()[0];
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

	public static boolean isOfType( Collection values, Class<?> aClass )
	{
		for ( Object obj : values )
			if ( !aClass.isAssignableFrom( obj.getClass() ) )
				return false;
		return true;
	}

	public static List<Class<?>> listClasses( final Object[] objs )
	{
		return listClasses( Arrays.asList( objs ) );
	}

	public static List<Class<?>> listClasses( final Collection collection )
	{
		return new ArrayList<Class<?>>()
		{{
			for ( Object obj : collection )
				add( obj.getClass() );
		}};
	}

	private Lists()
	{
	}
}
