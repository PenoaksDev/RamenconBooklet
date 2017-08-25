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

import io.amelia.android.data.BoundData;
import io.amelia.android.data.BoundDataTypeAdapterFactory;
import io.amelia.android.data.BundleTypeAdapterFactory;
import io.amelia.booklet.ContentManager;

public class LibAndroid
{
	public static Gson getGson()
	{
		return new GsonBuilder().registerTypeAdapterFactory( new BundleTypeAdapterFactory() ).registerTypeAdapterFactory( new BoundDataTypeAdapterFactory() ).create();
	}

	public static boolean haveNetworkConnection()
	{
		ConnectivityManager connectivityManager = ( ConnectivityManager ) ContentManager.getApplicationContext().getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static BoundData[] readJsonToBoundData( InputStream inputStream ) throws IOException
	{
		return readJsonToBoundData( LibIO.inputStream2String( inputStream ) );
	}

	public static BoundData[] readJsonToBoundData( File file ) throws IOException
	{
		return readJsonToBoundData( LibIO.readFileToString( file ) );
	}

	public static BoundData[] readJsonToBoundData( String json )
	{
		if ( json == null )
			return null;

		if ( json.trim().startsWith( "[" ) )
			return getGson().fromJson( json, BoundData[].class );
		else
			return new BoundData[] {getGson().fromJson( json, BoundData.class )};
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

	public static String writeBoundDataToJson( BoundData bundle )
	{
		if ( bundle == null )
			return null;

		return getGson().toJson( bundle );
	}

	public static void writeBoundDataToJsonFile( BoundData bundle, File file ) throws IOException
	{
		LibIO.writeStringToFile( file, writeBoundDataToJson( bundle ) );
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
}
