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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateAndTime
{
	public static String now()
	{
		return now( "" );
	}

	public static String now( Date date )
	{
		return now( "", date, null );
	}

	public static String now( String format )
	{
		return now( format, "" );
	}

	public static String now( String format, Date date )
	{
		return now( format, date, null );
	}

	public static String now( String format, Date date, String def )
	{
		return now( format, date == null ? "" : String.valueOf( date.getTime() / 1000 ), def );
	}

	public static String now( String format, Object data )
	{
		return now( format, data, null );
	}

	public static String now( String format, Object data, String def )
	{
		return now( format, data.toString(), def );
	}

	public static String now( String format, String data )
	{
		return now( format, data, null );
	}

	public static String now( String format, String data, String def )
	{
		Date date = new Date();

		if ( data != null && !data.isEmpty() )
		{
			data = data.trim();

			if ( data.length() > 10 )
				data = data.substring( 0, 10 );

			try
			{
				date = new Date( Long.parseLong( data ) * 1000 );
			}
			catch ( NumberFormatException e )
			{
				if ( def != null )
					return def;
			}
		}

		if ( format == null || format.isEmpty() )
			format = "MMM dx yyyy";

		if ( format.contains( "U" ) )
			format = format.replaceAll( "U", date.getTime() / 1000 + "" );

		if ( format.contains( "x" ) )
		{
			Calendar var1 = Calendar.getInstance();
			var1.setTime( date );
			int day = var1.get( Calendar.DAY_OF_MONTH );
			String suffix = "";

			if ( day >= 11 && day <= 13 )
				suffix = "'th'";
			else
				switch ( day % 10 )
				{
					case 1:
						suffix = "'st'";
						break;
					case 2:
						suffix = "'nd'";
						break;
					case 3:
						suffix = "'rd'";
						break;
					default:
						suffix = "'th'";
						break;
				}

			format = format.replaceAll( "x", suffix );
		}

		return new SimpleDateFormat( format ).format( date );
	}

	public static long epoch()
	{
		return System.currentTimeMillis() / 1000;
	}

	public static String formatDateRange( Date from, Date to )
	{
		// September 23-25, 2016

		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime( from );
		Calendar calTo = Calendar.getInstance();
		calTo.setTime( from );

		if ( calFrom.get( Calendar.YEAR ) == calTo.get( Calendar.YEAR ) )
		{
			StringBuffer op = new StringBuffer();

			if ( calFrom.get( Calendar.MONTH ) == calTo.get( Calendar.MONTH ) )
			{
				SimpleDateFormat formatFrom = new SimpleDateFormat( "MMMM dd" );
				SimpleDateFormat formatTo = new SimpleDateFormat( "dd" );
				op.append( formatFrom.format( from ) ).append( "-" ).append( formatTo.format( to ) );
			}
			else
			{
				SimpleDateFormat formatFrom = new SimpleDateFormat( "MM dd" );
				SimpleDateFormat formatTo = new SimpleDateFormat( "MM dd" );
				op.append( formatFrom.format( from ) ).append( " - " ).append( formatTo.format( to ) );
			}

			SimpleDateFormat year = new SimpleDateFormat( "yyyy" );
			op.append( ", " ).append( year.format( to ) );
			return op.toString();
		}
		else
		{
			SimpleDateFormat format = new SimpleDateFormat( "MM dd, yyyy" );
			return format.format( from ) + " - " + format.format( to );
		}
	}

	private DateAndTime()
	{

	}
}
