package com.penoaks.android.data;

import com.penoaks.android.utils.UtilIO;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class DataDownloadBuilder
{
	public static DataDownloadBuilder fromUrl( String url )
	{
		return new DataDownloadBuilder( url );
	}

	private String url;

	private DataDownloadBuilder( String url )
	{
		this.url = url;
	}

	private HttpURLConnection create() throws IOException
	{
		URL obj = new URL( url );
		HttpURLConnection con = ( HttpURLConnection ) obj.openConnection();

		con.setRequestProperty( "Accept-Language", "en-US,en;q=0.5" );

		return con;
	}

	public DataResult post( Map<String, String> args ) throws IOException
	{
		DataResult result = new DataResult();

		try
		{
			HttpURLConnection con = create();

			con.setRequestMethod( "POST" );
			con.setDoOutput( true );

			DataOutputStream wr = new DataOutputStream( con.getOutputStream() );

			try
			{
				wr.writeBytes( args.toString() );
				wr.flush();
			}
			finally
			{
				UtilIO.closeQuietly( wr );
			}

			result.responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
			StringBuffer response = new StringBuffer();

			try
			{
				String inputLine;
				while ( ( inputLine = in.readLine() ) != null )
					response.append( inputLine );
				in.close();
			}
			finally
			{
				UtilIO.closeQuietly( in );
			}

			result.content = response.toString();
		}
		catch ( IOException e )
		{
			result.exception = e;
		}

		return result;
	}

	public class DataResult
	{
		public int responseCode = -1;
		public Exception exception = null;
		public String content = null;
	}
}
