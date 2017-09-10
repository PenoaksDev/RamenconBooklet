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

import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strs
{
	/**
	 * Returns true if either array contains elements from the other
	 */
	public static boolean comparable( Object[] array1, Object[] array2 )
	{
		if ( array1.length == 0 && array2.length == 0 )
			return true;
		for ( Object obj : array1 )
			for ( Object obj2 : array2 )
				if ( obj.equals( obj2 ) )
					return true;
		return false;
	}

	public static int compare( String left, String right )
	{
		if ( left == null && right == null )
			return 0;
		if ( left == null )
			return 1;
		if ( right == null )
			return -1;

		return left.compareTo( right );
	}

	public static boolean containsValidChars( String ref )
	{
		return ref.matches( "[a-z0-9_]*" );
	}

	/**
	 * Copies all elements from the iterable collection of originals to the collection provided.
	 *
	 * @param token      String to search for
	 * @param originals  An iterable collection of strings to filter.
	 * @param collection The collection to add matches to
	 * @return the collection provided that would have the elements copied into
	 * @throws UnsupportedOperationException if the collection is immutable and originals contains a string which starts with the specified search
	 *                                       string.
	 * @throws IllegalArgumentException      if any parameter is is null
	 * @throws IllegalArgumentException      if originals contains a null element. <b>Note: the collection may be modified before this is thrown</b>
	 */
	public static <T extends Collection<String>> T copyPartialMatches( final String token, final Iterable<String> originals, final T collection ) throws UnsupportedOperationException, IllegalArgumentException
	{
		Objs.notNull( token, "Search token cannot be null" );
		Objs.notNull( collection, "Collection cannot be null" );
		Objs.notNull( originals, "Originals cannot be null" );

		for ( String string : originals )
			if ( startsWithIgnoreCase( string, token ) )
				collection.add( string );

		return collection;
	}

	public static byte[] decodeDefault( @NonNull String str )
	{
		return str.getBytes( Charset.defaultCharset() );
	}

	public static byte[] decodeUtf8( @NonNull String str )
	{
		return str.getBytes( Charset.forName( "utf8" ) );
	}

	public static String encodeDefault( byte[] bytes )
	{
		return new String( bytes, Charset.defaultCharset() );
	}

	public static String encodeUtf8( byte[] bytes )
	{
		return new String( bytes, Charset.forName( "utf8" ) );
	}

	public static String fixQuotes( String var )
	{
		try
		{
			var = var.replaceAll( "\\\\\"", "\"" );
			var = var.replaceAll( "\\\\'", "'" );

			if ( var.startsWith( "\"" ) || var.startsWith( "'" ) )
				var = var.substring( 1 );
			if ( var.endsWith( "\"" ) || var.endsWith( "'" ) )
				var = var.substring( 0, var.length() - 1 );
		}
		catch ( Exception ignore )
		{

		}

		return var;
	}

	public static boolean isCamelCase( String var )
	{
		Objs.notNull( var );
		return var.matches( "[a-z0-9]+(?:[A-Z]{1,2}[a-z0-9]+)*" );
	}

	/**
	 * Determines if a string is all lowercase using the toLowerCase() method.
	 *
	 * @param str The string to check
	 * @return Is it all lowercase?
	 */
	public static boolean isLowercase( String str )
	{
		return str.toLowerCase().equals( str );
	}

	/**
	 * Determines if a string is all uppercase using the toUpperCase() method.
	 *
	 * @param str The string to check
	 * @return Is it all uppercase?
	 */
	public static boolean isUppercase( String str )
	{
		return str.toUpperCase().equals( str );
	}

	public static String join( String[] args )
	{
		return Arrays.toString( args );
	}

	/**
	 * See {@link Arrays#copyOfRange(Object[], int, int)}
	 */
	public static String join( String[] args, int start )
	{
		return join( Arrays.copyOfRange( args, start, args.length ) );
	}

	/**
	 * See {@link Arrays#copyOfRange(Object[], int, int)}
	 */
	public static String join( String[] args, int start, int end )
	{
		return join( Arrays.copyOfRange( args, start, end ) );
	}

	public static String limitLength( String text, int max )
	{
		if ( text.length() <= max )
			return text;
		return text.substring( 0, max ) + "...";
	}

	public static String randomChars( String seed, int length )
	{
		Objs.notEmpty( seed );

		StringBuilder sb = new StringBuilder();

		for ( int i = 0; i < length; i++ )
			sb.append( seed.toCharArray()[new Random().nextInt( seed.length() )] );

		return sb.toString();
	}

	public static String regexCapture( String var, String regex )
	{
		return regexCapture( var, regex, 1 );
	}

	public static String regexCapture( String var, String regex, int group )
	{
		Pattern p = Pattern.compile( regex );
		Matcher m = p.matcher( var );

		if ( !m.find() )
			return null;

		return m.group( group );
	}

	public static String removeInvalidChars( String ref )
	{
		return ref.replaceAll( "[^a-zA-Z0-9!#$%&'*+-/=?^_`{|}~@\\. ]", "" );
	}

	public static String removeLetters( String input )
	{
		return input.replaceAll( "[a-zA-Z]", "" );
	}

	public static String removeLettersLower( String input )
	{
		return input.replaceAll( "[a-z]", "" );
	}

	public static String removeLettersUpper( String input )
	{
		return input.replaceAll( "[A-Z]", "" );
	}

	public static String removeNumbers( String input )
	{
		return input.replaceAll( "\\d", "" );
	}

	public static String removeSpecial( String input )
	{
		return input.replaceAll( "\\W", "" );
	}

	public static String removeWhitespace( String input )
	{
		return input.replaceAll( "\\s", "" );
	}

	public static String repeat( String string, int count )
	{
		Objs.notNull( string );

		if ( count <= 1 )
			return count == 0 ? "" : string;

		final int len = string.length();
		final long longSize = ( long ) len * ( long ) count;
		final int size = ( int ) longSize;
		if ( size != longSize )
			throw new ArrayIndexOutOfBoundsException( "Required array size too large: " + longSize );

		final char[] array = new char[size];
		string.getChars( 0, len, array, 0 );
		int n;
		for ( n = len; n < size - n; n <<= 1 )
			System.arraycopy( array, 0, array, n, n );
		System.arraycopy( array, 0, array, n, size - n );
		return new String( array );
	}

	public static List<String> repeatToList( String chr, int length )
	{
		List<String> list = new ArrayList<>();
		for ( int i = 0; i < length; i++ )
			list.add( chr );
		return list;
	}

	public static String replaceAt( String par, int at, String rep )
	{
		StringBuilder sb = new StringBuilder( par );
		sb.setCharAt( at, rep.toCharArray()[0] );
		return sb.toString();
	}

	/**
	 * This method uses a substring to check case-insensitive equality. This means the internal array does not need to be
	 * copied like a toLowerCase() call would.
	 *
	 * @param string String to check
	 * @param prefix Prefix of string to compare
	 * @return true if provided string starts with, ignoring case, the prefix provided
	 * @throws NullPointerException     if prefix is null
	 * @throws IllegalArgumentException if string is null
	 */
	public static boolean startsWithIgnoreCase( final String string, final String prefix ) throws IllegalArgumentException, NullPointerException
	{
		Objs.notNull( string, "Cannot check a null string for a match" );
		if ( string.length() < prefix.length() )
			return false;
		return string.substring( 0, prefix.length() ).equalsIgnoreCase( prefix );
	}

	public static byte[] stringToBytesASCII( String str )
	{
		byte[] b = new byte[str.length()];
		for ( int i = 0; i < b.length; i++ )
			b[i] = ( byte ) str.charAt( i );
		return b;
	}

	public static byte[] stringToBytesUTF( String str )
	{
		byte[] b = new byte[str.length() << 1];
		for ( int i = 0; i < str.length(); i++ )
		{
			char strChar = str.charAt( i );
			int bytePos = i << 1;
			b[bytePos] = ( byte ) ( ( strChar & 0xFF00 ) >> 8 );
			b[bytePos + 1] = ( byte ) ( strChar & 0x00FF );
		}
		return b;
	}

	/**
	 * Scans a string list for entries that are not lower case.
	 *
	 * @param strings The original list to check.
	 * @return Lowercase string array.
	 */
	public static String[] toLowerCase( Collection<String> strings )
	{
		List<String> result = new ArrayList<>();
		for ( String string : strings )
			if ( string != null )
				result.add( string.toLowerCase() );
		return result.toArray( new String[0] );
	}

	/**
	 * Scans a string array for entries that are not lower case.
	 *
	 * @param array The original array to check.
	 * @return The corrected string array.
	 */
	public static String[] toLowerCase( String... array )
	{
		return toLowerCase( Arrays.asList( array ) );
	}

	public static Collection<String> toLowerCaseList( Collection<String> strings )
	{
		List<String> result = new ArrayList<>();

		for ( String string : strings )
			result.add( string.toLowerCase() );

		return result;
	}

	/**
	 * Scans a string array for entries that are not lower case.
	 *
	 * @param array The original array to check.
	 * @return The corrected string array.
	 */
	public static Collection<String> toLowerCaseList( String... array )
	{
		return toLowerCaseList( Arrays.asList( array ) );
	}

	/**
	 * Trim specified character from both ends of a String
	 *
	 * @param text      Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimAll( String text, char character )
	{
		String normalizedText = trimFront( text, character );

		return trimEnd( normalizedText, character );
	}

	/**
	 * Trim specified character from end of string
	 *
	 * @param text      Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimEnd( String text, char character )
	{
		String normalizedText;
		int index;

		if ( text == null || text.isEmpty() )
			return text;

		normalizedText = text.trim();
		index = normalizedText.length() - 1;

		while ( normalizedText.charAt( index ) == character )
			if ( --index < 0 )
				return "";
		return normalizedText.substring( 0, index + 1 ).trim();
	}

	/**
	 * Trim specified character from front of string
	 *
	 * @param text      Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimFront( String text, char character )
	{
		String normalizedText;
		int index;

		if ( text == null || text.isEmpty() )
			return text;

		normalizedText = text.trim();
		index = -1;

		do
			index++;
		while ( index < normalizedText.length() && normalizedText.charAt( index ) == character );

		return normalizedText.substring( index ).trim();
	}

	public static Map<String, String> wrap( Map<String, String> map )
	{
		return wrap( map, '`', '\'' );
	}

	public static Collection<String> wrap( Collection<String> col )
	{
		return wrap( col, '`' );
	}

	public static Collection<String> wrap( final Collection<String> col, char wrap )
	{
		synchronized ( col )
		{
			String[] strings = col.toArray( new String[0] );
			col.clear();
			for ( int i = 0; i < strings.length; i++ )
				col.add( wrap( strings[i], wrap ) );
		}

		return col;
	}

	public static List<String> wrap( List<String> list )
	{
		return wrap( list, '`' );
	}

	public static List<String> wrap( final List<String> list, char wrap )
	{
		List<String> newList;
		if ( list instanceof ArrayList )
			newList = new ArrayList<>();
		else if ( list instanceof CopyOnWriteArrayList )
			newList = new CopyOnWriteArrayList<>();
		else if ( list instanceof LinkedList )
			newList = new LinkedList<>();
		else
		{
			Log.w( "Util", "Could not find List type for class " + list.getClass() );
			newList = new LinkedList<>();
		}

		for ( String str : list )
			newList.add( wrap( str, wrap ) );

		return newList;
	}

	public static Map<String, String> wrap( final Map<String, String> map, char keyWrap, char valueWrap )
	{
		Map<String, String> newMap;
		if ( map instanceof HashMap )
			newMap = new HashMap<>();
		if ( map instanceof ConcurrentMap )
			newMap = new ConcurrentHashMap<>();
		if ( map instanceof IdentityHashMap )
			newMap = new IdentityHashMap<>();
		if ( map instanceof LinkedHashMap )
			newMap = new LinkedHashMap<>();
		if ( map instanceof TreeMap )
			newMap = new TreeMap<>();
		else
		{
			Log.w( "Util", "Could not find Map type for class " + map.getClass() );
			newMap = new LinkedHashMap<>();
		}

		for ( Map.Entry<String, String> e : map.entrySet() )
			if ( e.getKey() != null && !e.getKey().isEmpty() )
				newMap.put( keyWrap + e.getKey() + keyWrap, valueWrap + ( e.getValue() == null ? "" : e.getValue() ) + valueWrap );

		return newMap;
	}

	public static Set<String> wrap( Set<String> set )
	{
		return wrap( set, '`' );
	}

	public static Set<String> wrap( final Set<String> set, char wrap )
	{
		Set<String> newSet;
		if ( set instanceof HashSet )
			newSet = new HashSet<>();
		else if ( set instanceof TreeSet )
			newSet = new TreeSet<>();
		else if ( set instanceof LinkedHashSet )
			newSet = new LinkedHashSet<>();
		else if ( set instanceof CopyOnWriteArraySet )
			newSet = new CopyOnWriteArraySet<>();
		else if ( set.getClass() == new HashMap<>().keySet().getClass() ) // Really nasty way of comparing it to a private class
			newSet = new LinkedHashSet<>();
		else
		{
			Log.w( "Util", "Could not find Set type for class " + set.getClass() );
			newSet = new LinkedHashSet<>();
		}

		for ( String str : set )
			newSet.add( wrap( str, wrap ) );

		return newSet;
	}

	public static String wrap( String str, char wrap )
	{
		return String.format( "%s%s%s", wrap, str, wrap );
	}

	private Strs()
	{

	}
}
