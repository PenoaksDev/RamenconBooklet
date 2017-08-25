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

import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Objs
{
	public static <V> Collection<V> castCollection( Collection<?> col, Class<V> clz )
	{
		Collection<V> newCol = new LinkedList<>();

		for ( Object e : col )
		{
			V v = Objs.castTo( e, clz );

			if ( v != null )
				newCol.add( v );
		}

		return newCol;
	}

	public static <Type> List<Type> castList( List<?> list, Class<Type> typeClass )
	{
		Objs.notNull( list );

		List<Type> newList = new ArrayList<>();

		for ( Object o : list )
			newList.add( castTo( o, typeClass ) );

		return newList;
	}

	public static <K, V> Map<K, V> castMap( Map<?, ?> map, Class<K> keyClz, Class<V> valClz )
	{
		Objs.notNull( map );

		Map<K, V> newMap = new LinkedHashMap<>();

		for ( Map.Entry<?, ?> e : map.entrySet() )
		{
			K k = Objs.castTo( e.getKey(), keyClz );
			V v = Objs.castTo( e.getValue(), valClz );

			if ( k != null && v != null )
				newMap.put( k, v );
		}

		return newMap;
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
				return ( O ) castToBooleanWithException( obj );
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
						 * Last and final attempt to get something out of this
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

	public static Boolean castToBoolean( Object value )
	{
		return castToBoolean( value, false );
	}

	public static Boolean castToBoolean( Object value, Boolean def )
	{
		if ( value == null )
			return def;

		try
		{
			return castToBooleanWithException( value );
		}
		catch ( Exception e )
		{
			return def;
		}
	}

	public static Boolean castToBooleanWithException( Object value ) throws ClassCastException
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Boolean" );

		if ( value.getClass() == boolean.class || value.getClass() == Boolean.class )
			return ( boolean ) value;

		String val = castToStringWithException( value );

		if ( val == null )
			throw new ClassCastException( "Uncaught Conversion to Boolean of Type: " + value.getClass().getName() );

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
			case "":
				return false;
			default:
				throw new ClassCastException( "Uncaught Conversion to Boolean of Type: " + value.getClass().getName() );
		}
	}

	public static Double castToDouble( Object value )
	{
		return castToDouble( value, 0D );
	}

	public static Double castToDouble( Object value, Double def )
	{
		if ( value == null )
			return def;

		try
		{
			return castToDoubleWithException( value );
		}
		catch ( Exception e )
		{
			return def;
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

		throw new ClassCastException( "Uncaught Conversion to Integer of Type: " + value.getClass().getName() );
	}

	/**
	 * Attempts to cast the object to an Integer.
	 *
	 * @param value The unknown object type
	 * @return The exact value of the object.
	 */
	public static Integer castToInt( Object value )
	{
		try
		{
			return castToIntWithException( value );
		}
		catch ( Exception e )
		{
			// If value is Long, bound value by Integer.MAX_VALUE and Integer.MIN_VALUE.
			if ( value instanceof Long )
				return ( int ) value;
			return -1;
		}
	}

	public static Integer castToInt( Object value, Integer def )
	{
		if ( value == null )
			return def;

		try
		{
			return castToIntWithException( value );
		}
		catch ( Exception e )
		{
			return def;
		}
	}

	/**
	 * Attempts to cast the object to an Integer.
	 *
	 * @param value The unknown object type
	 * @return The exact value of the object.
	 * @throws ClassCastException If the value is null, overflows an int, or is an object type that is not castable to an Integer.
	 */
	public static Integer castToIntWithException( Object value )
	{
		notNull( value, "Can't cast null to Integer." );

		if ( value instanceof Long )
			if ( ( long ) value < Integer.MIN_VALUE || ( long ) value > Integer.MAX_VALUE )
				return ( int ) value;
			else
				throw new ClassCastException( "Long value `" + Long.toString( ( Long ) value ) + "` is +/- than Integer max/min value." );
		if ( value instanceof String )
			return Integer.parseInt( ( String ) value );
		if ( value instanceof Integer || value instanceof Double )
			return ( int ) value;
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1 : 0;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).intValue();

		throw new ClassCastException( "Uncaught Conversion to Integer of Type: " + value.getClass().getName() );
	}

	public static Long castToLong( Object value )
	{
		return castToLong( value, 0L );
	}

	public static Long castToLong( Object value, Long def )
	{
		if ( value == null )
			return def;

		try
		{
			return castToLongWithException( value );
		}
		catch ( Exception e )
		{
			return def;
		}
	}

	public static Long castToLongWithException( Object value )
	{
		notNull( value, "Can't cast null to Long." );

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

		throw new ClassCastException( "Uncaught Conversion to Long of Type: " + value.getClass().getName() );
	}

	public static String castToString( Object value )
	{
		return castToString( value, null );
	}

	public static String castToString( Object value, String def )
	{
		if ( value == null )
			return def;

		try
		{
			return castToStringWithException( value );
		}
		catch ( ClassCastException e )
		{
			return def;
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
		{
			List<String> elements = new ArrayList<>();
			for ( Map.Entry e : ( ( Map<?, ?> ) value ).entrySet() )
				elements.add( castToString( e.getKey() ) + "=\"" + castToString( e.getValue() ) + "\"" );
			return TextUtils.join( ", ", elements );
		}
		if ( value instanceof List )
		{
			List<String> elements = new ArrayList<>();
			for ( Object o : ( List ) value )
				elements.add( castToString( o ) );
			return TextUtils.join( ", ", elements );
		}
		throw new ClassCastException( "Uncaught Conversion to String of Type: " + value.getClass().getName() );
	}

	public static boolean containsKeys( Map<String, ?> origMap, Collection<String> keys )
	{
		for ( String key : keys )
			if ( origMap.containsKey( key ) )
				return true;
		return false;
	}

	@SuppressWarnings( "unchecked" )
	public static boolean equals( Object left, Object right )
	{
		if ( left == null || right == null )
			return left == right;

		if ( left instanceof Comparable && ( ( Comparable ) left ).compareTo( right ) == 0 )
			return true;

		if ( right instanceof Comparable && ( ( Comparable ) right ).compareTo( left ) == 0 )
			return true;

		return left == right;
	}

	public static <T> Class<T> getClassByName( String aClass )
	{
		if ( isEmpty( aClass ) )
			return null;

		try
		{
			return ( Class<T> ) Class.forName( aClass );
		}
		catch ( ClassNotFoundException e )
		{
			throw new RuntimeException( e );
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

	@SuppressWarnings( "unchecked" )
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

	public static int length( Object obj )
	{
		if ( obj == null )
			return 0;

		try
		{
			return castToStringWithException( obj ).length();
		}
		catch ( ClassCastException e )
		{
			// Ignore
		}

		Method methodIsEmpty = getMethodSafe( obj, "isEmpty" );
		Method methodLength = getMethodSafe( obj, "length" );
		Method methodSize = getMethodSafe( obj, "size" );

		if ( methodIsEmpty != null && invokeMethodSafe( methodIsEmpty, obj, false ) )
			return 0;

		if ( methodLength != null )
			return invokeMethodSafe( methodLength, obj, -1 );

		if ( methodSize != null )
			return invokeMethodSafe( methodSize, obj, -1 );

		return -1;
	}

	public static <T extends CharSequence> T notEmpty( final T chars, final String message, final Object... values )
	{
		if ( chars == null )
			throw new NullPointerException( String.format( message, values ) );
		if ( chars.length() == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );
		return chars;
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

	public static <T> T notEmptyOrDef( T obj, T def )
	{
		return isEmpty( obj ) ? def : obj;
	}

	public static <T> T notFalse( T bool )
	{
		return notFalse( bool, "Object is false" );
	}

	public static <T> T notFalse( T bool, String message, Object... objects )
	{
		if ( !castToBoolean( bool ) )
			throw new IllegalArgumentException( objects == null || objects.length == 0 ? message : String.format( message, ( Object[] ) objects ) );
		return bool;
	}

	public static <T extends Number> T notNegative( T number )
	{
		return notNegative( number, "Number must be positive." );
	}

	public static <T extends Number> T notNegative( T number, String message, Object... objects )
	{
		if ( number.longValue() < 0 )
			throw new IllegalArgumentException( objects == null || objects.length == 0 ? message : String.format( message, ( Object[] ) objects ) );
		return number;
	}

	public static <T> T notNull( final T object )
	{
		notNull( object, "Object is null" );
		return object;
	}

	public static <T> T notNull( final T object, String message, Object... values )
	{
		if ( object == null )
			throw new NullPointerException( values == null || values.length == 0 ? message : String.format( message, values ) );
		return object;
	}

	public static <T> T notNullOrDef( T obj, T def )
	{
		return isNull( obj ) ? def : obj;
	}

	public static <T extends Number> T notPositive( T number )
	{
		return notNegative( number, "Number must be negative." );
	}

	public static <T extends Number> T notPostive( T number, String message, Object... objects )
	{
		if ( number.longValue() > 0 )
			throw new IllegalArgumentException( objects == null || objects.length == 0 ? message : String.format( message, ( Object[] ) objects ) );
		return number;
	}

	public static <T extends Number> T notZero( T number )
	{
		return notNegative( number, "Number must be positive or negative." );
	}

	public static <T extends Number> T notZero( T number, String message, Object... objects )
	{
		if ( number.longValue() == 0 )
			throw new IllegalArgumentException( objects == null || objects.length == 0 ? message : String.format( message, ( Object[] ) objects ) );
		return number;
	}

	public static int safeLongToInt( long l )
	{
		if ( l < Integer.MIN_VALUE )
			return Integer.MIN_VALUE;
		if ( l > Integer.MAX_VALUE )
			return Integer.MAX_VALUE;
		return ( int ) l;
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

	private Objs()
	{

	}
}
