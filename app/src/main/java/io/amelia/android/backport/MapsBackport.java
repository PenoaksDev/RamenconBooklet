package io.amelia.android.backport;

import java.util.Map;

import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.backport.function.BiFunction;
import io.amelia.android.backport.function.Function;
import io.amelia.android.support.Objs;

public class MapsBackport
{
	public static <K, V> V compute( Map<K, V> map, K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction )
	{
		Objs.notNull( remappingFunction );
		V oldValue = map.get( key );

		V newValue = remappingFunction.apply( key, oldValue );
		if ( newValue == null )
		{
			// delete mapping
			if ( oldValue != null || map.containsKey( key ) )
			{
				// something to remove
				map.remove( key );
				return null;
			}
			else
			{
				// nothing to do. Leave things as they were.
				return null;
			}
		}
		else
		{
			// add or replace old mapping
			map.put( key, newValue );
			return newValue;
		}
	}

	public static <K, V> V computeIfAbsent( Map<K, V> map, K key, Function<? super K, ? extends V> mappingFunction )
	{
		Objs.notNull( mappingFunction );
		V v;
		if ( ( v = map.get( key ) ) == null )
		{
			V newValue;
			if ( ( newValue = mappingFunction.apply( key ) ) != null )
			{
				map.put( key, newValue );
				return newValue;
			}
		}

		return v;
	}

	public static <K, V> V computeIfPresent( Map<K, V> map, K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction )
	{
		Objs.notNull( remappingFunction );
		V oldValue;
		if ( ( oldValue = map.get( key ) ) != null )
		{
			V newValue = remappingFunction.apply( key, oldValue );
			if ( newValue != null )
			{
				map.put( key, newValue );
				return newValue;
			}
			else
			{
				map.remove( key );
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public static <K, V> void forEach( Map<K, V> map, BiConsumer<? super K, ? super V> action )
	{
		Objs.notNull( action );
		for ( Map.Entry<K, V> entry : map.entrySet() )
		{
			K k;
			V v;
			k = entry.getKey();
			v = entry.getValue();
			action.accept( k, v );
		}
	}

	public static <K, V> V getOrDefault( Map<K, V> map, Object key, V defaultValue )
	{
		V v;
		return ( ( ( v = map.get( key ) ) != null ) || map.containsKey( key ) ) ? v : defaultValue;
	}

	public static <K, V> V merge( Map<K, V> map, K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction )
	{
		Objs.notNull( remappingFunction );
		Objs.notNull( value );
		V oldValue = map.get( key );
		V newValue = ( oldValue == null ) ? value : remappingFunction.apply( oldValue, value );
		if ( newValue == null )
			map.remove( key );
		else
			map.put( key, newValue );
		return newValue;
	}

	public static <K, V> V putIfAbsent( Map<K, V> map, K key, V value )
	{
		V v = map.get( key );
		if ( v == null )
			v = map.put( key, value );

		return v;
	}

	private MapsBackport()
	{
		// Static
	}
}
