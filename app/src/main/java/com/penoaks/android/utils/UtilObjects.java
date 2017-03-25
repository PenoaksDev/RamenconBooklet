/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package com.penoaks.android.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UtilObjects
{
	private UtilObjects()
	{

	}

	@SuppressWarnings( "unchecked" )
	public static <O> O castTo( Object obj, Class<O> clz )
	{
		try
		{
			if ( clz == Integer.class )
				return ( O ) castToIntWithException( obj );
			if ( clz == Long.class )
				return ( O ) castToLongWithException( obj );
			if ( clz == Double.class )
				return ( O ) castToDoubleWithException( obj );
			if ( clz == Boolean.class )
				return ( O ) castToBoolWithException( obj );
			if ( clz == String.class )
				return ( O ) castToStringWithException( obj );
		}
		catch ( Exception e1 )
		{
			try
			{
				return ( O ) obj;
			}
			catch ( Exception e2 )
			{
				try
				{
					return ( O ) castToStringWithException( obj );
				}
				catch ( Exception e3 )
				{
					try
					{
						/*
						 * Last and final attempt to config something out of this
						 * object even if it results in the toString() method.
						 */
						return ( O ) ( "" + obj );
					}
					catch ( Exception e4 )
					{
						// Ignore
					}
				}
			}
		}

		return null;
	}

	public static Boolean castToBool( Object value )
	{
		try
		{
			return castToBoolWithException( value );
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	public static Boolean castToBoolWithException( Object value ) throws ClassCastException
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Boolean" );

		if ( value.getClass() == boolean.class || value.getClass() == Boolean.class )
			return ( boolean ) value;

		String val = castToStringWithException( value );

		if ( val == null )
			throw new ClassCastException( "Uncaught Convertion to Boolean of Type: " + value.getClass().getName() );

		switch ( val.trim().toLowerCase() )
		{
			case "yes":
				return true;
			case "no":
				return false;
			case "true":
				return true;
			case "false":
				return false;
			case "1":
				return true;
			case "0":
				return false;
			default:
				throw new ClassCastException( "Uncaught Convertion to Boolean of Type: " + value.getClass().getName() );
		}
	}

	public static Double castToDouble( Object value )
	{
		try
		{
			return castToDoubleWithException( value );
		}
		catch ( Exception e )
		{
			return 0D;
		}
	}

	public static Double castToDoubleWithException( Object value )
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Double" );

		if ( value instanceof Long )
			return ( ( Long ) value ).doubleValue();
		if ( value instanceof String )
			return Double.parseDouble( ( String ) value );
		if ( value instanceof Integer )
			return ( ( Integer ) value ).doubleValue();
		if ( value instanceof Double )
			return ( Double ) value;
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1D : 0D;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).doubleValue();

		throw new ClassCastException( "Uncaught Convertion to Integer of Type: " + value.getClass().getName() );
	}

	public static Integer castToInt( Object value )
	{
		try
		{
			return castToIntWithException( value );
		}
		catch ( Exception e )
		{
			return -1;
		}
	}

	public static Integer castToIntWithException( Object value )
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Integer" );

		if ( value instanceof Long )
			if ( ( long ) value < Integer.MIN_VALUE || ( long ) value > Integer.MAX_VALUE )
				return ( Integer ) value;
			else
				return null;
		if ( value instanceof String )
			return Integer.parseInt( ( String ) value );
		if ( value instanceof Integer )
			return ( Integer ) value;
		if ( value instanceof Double )
			return ( Integer ) value;
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1 : 0;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).intValue();

		throw new ClassCastException( "Uncaught Conversion to Integer of Type: " + value.getClass().getName() );
	}

	public static Long castToLong( Object value )
	{
		try
		{
			return castToLongWithException( value );
		}
		catch ( ClassCastException e )
		{
			e.printStackTrace();
			return 0L;
		}
	}

	public static Long castToLongWithException( Object value )
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Long" );

		if ( value instanceof Long )
			return ( Long ) value;
		if ( value instanceof String )
			return Long.parseLong( ( String ) value );
		if ( value instanceof Integer )
			return Long.parseLong( "" + value );
		if ( value instanceof Double )
			return Long.parseLong( "" + value );
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1L : 0L;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).longValue();

		throw new ClassCastException( "Uncaught Convertion to Long of Type: " + value.getClass().getName() );
	}

	public static String castToString( Object value )
	{
		try
		{
			return castToStringWithException( value );
		}
		catch ( ClassCastException e )
		{
			return null;
		}
	}

	@SuppressWarnings( "rawtypes" )
	public static String castToStringWithException( final Object value ) throws ClassCastException
	{
		if ( value == null )
			return null;
		if ( value instanceof Long )
			return Long.toString( ( long ) value );
		if ( value instanceof String )
			return ( String ) value;
		if ( value instanceof Integer )
			return Integer.toString( ( int ) value );
		if ( value instanceof Double )
			return Double.toString( ( double ) value );
		if ( value instanceof Boolean )
			return ( boolean ) value ? "true" : "false";
		if ( value instanceof BigDecimal )
			return value.toString();
		if ( value instanceof Map )
			return value.toString();
		if ( value instanceof List )
			return value.toString();
		throw new ClassCastException( "Uncaught Convertion to String of Type: " + value.getClass().getName() );
	}

	public static <T> void notFalse( T bool )
	{
		notFalse( bool, "Object is false" );
	}

	public static <T> void notFalse( T bool, String message, String... objects )
	{
		if ( !castToBool( bool ) )
			throw new IllegalArgumentException( objects == null || objects.length == 0 ? message : String.format( message, ( Object[] ) objects ) );
	}

	public static <T> boolean isTrue( T bool )
	{
		try
		{
			notFalse( bool );
			return true;
		}
		catch ( IllegalArgumentException e )
		{
			return false;
		}
	}

	public static boolean stackTraceAntiLoop( Class<?> cls, String method )
	{
		return stackTraceAntiLoop( cls.getCanonicalName(), method, 1 );
	}

	public static boolean stackTraceAntiLoop( Class<?> cls, String method, int max )
	{
		return stackTraceAntiLoop( cls.getCanonicalName(), method, max );
	}

	public static boolean stackTraceAntiLoop( String cls, String method )
	{
		return stackTraceAntiLoop( cls, method, 1 );
	}

	/**
	 * Detects if the specified class and method has been called in a previous stack trace event.
	 *
	 * @param cls    The class to check.
	 * @param method The method to check, null to ignore.
	 * @param max    The maximum number of recurrence until failure.
	 * @return True if no loop was detected.
	 */
	public static boolean stackTraceAntiLoop( String cls, String method, int max )
	{
		int cnt = 0;
		for ( StackTraceElement ste : Thread.currentThread().getStackTrace() )
			if ( ste.getClassName().equals( cls ) && ( method == null || ste.getMethodName().equals( method ) ) )
			{
				cnt++;
				if ( cnt >= max )
					return false;
			}
		return true;
	}

	public static <T extends CharSequence> T notEmpty( final T chars, final String message, final Object... values )
	{
		if ( chars == null )
			throw new NullPointerException( String.format( message, values ) );
		if ( chars.length() == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );
		return chars;
	}

	public static <T> void notNull( final T object )
	{
		notNull( object, "Object is null" );
	}

	public static <T> void notNull( final T object, String message, Object... values )
	{
		if ( object == null )
			throw new NullPointerException( values == null || values.length == 0 ? message : String.format( message, values ) );
	}

	public static int safeLongToInt( long l )
	{
		if ( l < Integer.MIN_VALUE )
			return Integer.MIN_VALUE;
		if ( l > Integer.MAX_VALUE )
			return Integer.MAX_VALUE;
		return ( int ) l;
	}

	@SuppressWarnings( {"unchecked"} )
	public static <T> boolean instanceOf( Object obj, Class<T> castClass )
	{
		try
		{
			T testCast = ( T ) obj;
			return testCast != null && testCast.getClass().isInstance( castClass );
		}
		catch ( ClassCastException e )
		{
			return false;
		}
	}

	public static <T> T notEmpty( final T obj )
	{
		return notEmpty( obj, "Object is empty" );
	}

	public static <T> T notEmpty( final T obj, final String message, final Object... values )
	{
		if ( obj == null )
			throw new NullPointerException( String.format( message, values ) );

		Method methodIsEmpty = getMethodSafe( obj, "isEmpty" );
		Method methodLength = getMethodSafe( obj, "length" );
		Method methodSize = getMethodSafe( obj, "size" );

		if ( methodIsEmpty != null && invokeMethodSafe( methodIsEmpty, obj, false ) )
			throw new IllegalArgumentException( String.format( message, values ) );

		if ( methodLength != null && invokeMethodSafe( methodLength, obj, -1 ) == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );

		if ( methodSize != null && invokeMethodSafe( methodSize, obj, -1 ) == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );

		return obj;
	}

	private static <T> T invokeMethodSafe( Method method, Object obj, T rtn, Object... args )
	{
		try
		{
			return ( T ) method.invoke( obj, args );
		}
		catch ( IllegalAccessException e )
		{
			return rtn;
		}
		catch ( InvocationTargetException e )
		{
			return rtn;
		}
	}

	private static <T> Method getMethodSafe( T obj, String methodName )
	{
		try
		{
			return obj.getClass().getMethod( methodName );
		}
		catch ( NoSuchMethodException e )
		{
			return null;
		}
	}

	public static <T> boolean isNull( T obj )
	{
		try
		{
			notNull( obj );
			return false;
		}
		catch ( Throwable t )
		{
			return true;
		}
	}

	public static <T> boolean isEmpty( T obj )
	{
		try
		{
			notEmpty( obj );
			return false;
		}
		catch ( Throwable t )
		{
			return true;
		}
	}

	public static <V> Collection<V> castCollection( Collection<?> col, Class<V> clz )
	{
		Collection<V> newCol = new LinkedList<V>();

		for ( Object e : col )
		{
			V v = UtilObjects.castTo( e, clz );

			if ( v != null )
				newCol.add( v );
		}

		return newCol;
	}

	public static <K, V> Map<K, V> castMap( Map<?, ?> map, Class<K> keyClz, Class<V> valClz )
	{
		UtilObjects.notNull( map );

		Map<K, V> newMap = new LinkedHashMap<>();

		for ( Map.Entry<?, ?> e : map.entrySet() )
		{
			K k = UtilObjects.castTo( e.getKey(), keyClz );
			V v = UtilObjects.castTo( e.getValue(), valClz );

			if ( k != null && v != null )
				newMap.put( k, v );
		}

		return newMap;
	}

	public static boolean containsKeys( Map<String, ?> origMap, Collection<String> keys )
	{
		for ( String key : keys )
			if ( origMap.containsKey( key ) )
				return true;
		return false;
	}
}
