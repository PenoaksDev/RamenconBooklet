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

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Provides Network Utilities
 */
public class LibHttp
{
	public static final String REGEX_IPV4 = "^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$";
	public static final String REGEX_IPV6 = "^((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$";

	private static final Pattern DNS_WILDCARD_PATTERN = Pattern.compile( "^\\*\\..*" );

	private LibHttp()
	{

	}

	public static boolean matches( String strTemplate, String str )
	{
		if ( DNS_WILDCARD_PATTERN.matcher( strTemplate ).matches() )
			return strTemplate.substring( 2 ).equals( str ) || str.endsWith( strTemplate.substring( 1 ) );
		else
			return strTemplate.equals( str );
	}

	public static boolean needsNormalization( String str )
	{
		final int length = str.length();
		for ( int i = 0; i < length; i++ )
		{
			int c = str.charAt( i );
			if ( c > 0x7F )
				return true;
		}
		return false;
	}

	public static String normalize( String str )
	{
		if ( str == null )
			return null;
		if ( needsNormalization( str ) )
			str = IDN.toASCII( str, IDN.ALLOW_UNASSIGNED );
		return str.toLowerCase( Locale.US );
	}

	public static boolean downloadFile( String url, File dest ) throws IOException
	{
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		try
		{
			URL conn = new URL( url );
			rbc = Channels.newChannel( conn.openStream() );
			fos = new FileOutputStream( dest );

			fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
		}
		finally
		{
			if ( rbc != null )
				rbc.close();
			if ( fos != null )
				fos.close();
		}

		return true;
	}

	/* public static Date getNTPDate()
	{
		String[] hosts = new String[] {"ntp02.oal.ul.pt", "ntp04.oal.ul.pt", "ntp.xs4all.nl"};

		NTPUDPClient client = new NTPUDPClient();
		// We want to timeout if a response takes longer than 5 seconds
		client.setDefaultTimeout( 5000 );

		for ( String host : hosts )
			try
			{
				InetAddress hostAddress = InetAddress.getByName( host );
				// System.out.println( "> " + hostAddress.getHostName() + "/" + hostAddress.getHostAddress() );
				TimeInfo info = client.getTime( hostAddress );
				Date date = new Date( info.getReturnTime() );
				return date;

			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

		client.close();

		return null;
	} */

	public static boolean isValidIPv4( String ip )
	{
		if ( ip == null )
			return false;

		return ip.matches( REGEX_IPV4 );
	}

	public static boolean isValidIPv6( String ip )
	{
		if ( ip == null )
			return false;

		return ip.matches( REGEX_IPV6 );
	}

	/**
	 * Establishes an HttpURLConnection from a URL, with the correct configuration to receive content from the given URL.
	 *
	 * @param url The URL to set up and receive content from
	 * @return A valid HttpURLConnection
	 * @throws IOException The openConnection() method throws an IOException and the calling method is responsible for handling it.
	 */
	public static HttpURLConnection openHttpConnection( URL url ) throws IOException
	{
		HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
		conn.setDoInput( true );
		conn.setDoOutput( false );
		HttpURLConnection.setFollowRedirects( true );
		conn.setUseCaches( false );
		conn.setInstanceFollowRedirects( true );
		return conn;
	}

	/**
	 * Opens an HTTP connection to a web URL and tests that the response is a valid 200-level code
	 * and we can successfully open a stream to the content.
	 *
	 * @param url The HTTP URL indicating the location of the content.
	 * @return True if the content can be accessed successfully, false otherwise.
	 */
	public static boolean pingHttpURL( String url )
	{
		InputStream stream = null;
		try
		{
			final HttpURLConnection conn = openHttpConnection( new URL( url ) );
			conn.setConnectTimeout( 10000 );

			int responseCode = conn.getResponseCode();
			int responseFamily = responseCode / 100;

			if ( responseFamily == 2 )
			{
				stream = conn.getInputStream();
				LibIO.closeQuietly( stream );
				return true;
			}
			else
				return false;
		}
		catch ( IOException e )
		{
			return false;
		}
		finally
		{
			LibIO.closeQuietly( stream );
		}
	}

	public static byte[] readUrl( String url )
	{
		try
		{
			return readUrlWithException( url );
		}
		catch ( IOException e )
		{
			// Log.config().severe( "Reading URL \"" + url + "\" failed!" );
			return null;
		}
	}

	public static byte[] readUrl( String url, boolean trustAll )
	{
		try
		{
			return readUrlWithException( url, trustAll );
		}
		catch ( IOException e )
		{
			// Log.config().severe( "Reading URL \"" + url + "\" failed!" );
			return null;
		}
	}

	public static byte[] readUrl( String url, String user, String pass )
	{
		try
		{
			return readUrlWithException( url, user, pass, false );
		}
		catch ( IOException e )
		{
			// Log.config().severe( "Reading URL \"" + url + "\" failed!" );
			return null;
		}
	}

	public static byte[] readUrlWithException( String url ) throws IOException
	{
		return readUrlWithException( url, null, null, false );
	}

	public static byte[] readUrlWithException( String url, boolean trustAll ) throws IOException
	{
		return readUrlWithException( url, null, null, trustAll );
	}

	public static byte[] readUrlWithException( String surl, String user, String pass, boolean trustAll ) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		URL url = new URL( surl );
		URLConnection uc = url.openConnection();

		if ( user != null || pass != null )
		{
			String userpass = user + ":" + pass;
			String basicAuth = "Basic " + Base64.encodeToString( userpass.getBytes(), 0 );
			uc.setRequestProperty( "Authorization", basicAuth );
		}

		if ( uc instanceof HttpsURLConnection && trustAll )
			try
			{
				SSLContext ctx = SSLContext.getInstance( "SSL" );
				ctx.init( null, TrustManagerFactory.getTrustManagers(), null );
				( ( HttpsURLConnection ) uc ).setSSLSocketFactory( ctx.getSocketFactory() );
			}
			catch ( KeyManagementException | NoSuchAlgorithmException e )
			{
				Log.e( "Util", "Failed to set the SSL Factory, so all certificates are accepted.", e );
			}

		InputStream is = uc.getInputStream();

		byte[] byteChunk = new byte[4096];
		int n;

		while ( ( n = is.read( byteChunk ) ) > 0 )
			out.write( byteChunk, 0, n );

		is.close();

		return out.toByteArray();
	}

	/**
	 * TODO This was lagging the server! WHY???
	 * Maybe we should change our metrics system
	 */
	public static boolean sendTracking( String category, String action, String label )
	{
		try
		{
			String url = "http://www.google-analytics.com/collect";

			URL urlObj = new URL( url );
			HttpURLConnection con = ( HttpURLConnection ) urlObj.openConnection();
			con.setRequestMethod( "POST" );

			String urlParameters = "v=1&tid=UA-60405654-1&cid=Unknown&t=event&ec=" + category + "&ea=" + action + "&el=" + label;

			con.setDoOutput( true );
			DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
			wr.writeBytes( urlParameters );
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			Log.i( "Util", "Analytics Response [" + category + "]: " + responseCode );

			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ( ( inputLine = in.readLine() ) != null )
				response.append( inputLine );
			in.close();

			return true;
		}
		catch ( IOException e )
		{
			return false;
		}
	}
}
