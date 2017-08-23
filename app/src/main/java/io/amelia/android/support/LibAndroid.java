package io.amelia.android.support;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.amelia.booklet.ContentManager;

public class LibAndroid
{
	public static boolean haveNetworkConnection()
	{
		ConnectivityManager connectivityManager = ( ConnectivityManager ) ContentManager.getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static Gson getGson()
	{
		return new GsonBuilder().registerTypeAdapterFactory( new BundleTypeAdapterFactory() ).create();
	}

	public static String writeBundleToJson( Bundle bundle )
	{
		if ( bundle == null )
			return null;

		return getGson().toJson( bundle );
	}

	public static void writeBundleToJsonFile( Bundle bundle, File file ) throws IOException
	{
		LibIO.writeStringToFile( file, writeBundleToJson( bundle ) );
	}

	public static Bundle[] readJsonToBundle( InputStream inputStream ) throws IOException
	{
		return readJsonToBundle( LibIO.inputStream2String( inputStream ) );
	}

	public static Bundle[] readJsonToBundle( File file ) throws IOException
	{
		return readJsonToBundle( LibIO.readFileToString( file ) );
	}

	public static Bundle[] readJsonToBundle( String json )
	{
		if ( json == null )
			return null;

		if ( json.trim().startsWith( "[" ) )
			return getGson().fromJson( json, Bundle[].class );
		else
			return new Bundle[] {getGson().fromJson( json, Bundle.class )};
	}
}
