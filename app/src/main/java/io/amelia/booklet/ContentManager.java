package io.amelia.booklet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.amelia.android.support.LibAndroid;
import io.amelia.booklet.ui.activity.BaseActivity;
import io.amelia.booklet.ui.activity.ContentActivity;

public class ContentManager
{
	public static final String REMOTE_CATALOG_URL = "http://booklet.dev.penoaks.com/catalog/";
	// public static final String REMOTE_CATALOG_URL = "http://booklet.dev.penoaks.com/catalog/search?startsWith=ramencon";
	public static final String REMOTE_DATA_URL = "http://booklet.dev.penoaks.com/data/";

	static final ExecutorService threadPool = Executors.newCachedThreadPool();
	private static Booklet activeBooklet;
	private static BaseActivity activity;
	private static File cacheDir;
	private static boolean contentManagerReady = false;
	private static Context context;

	public static void factoryReset()
	{
		for ( File file : getCacheDir().listFiles() )
			file.delete();

		activeBooklet = null;

		Booklet.setup();

		Booklet.addBooklet( "ramencon2016" );
		Booklet.addBooklet( "ramencon2017" );
	}

	public static Booklet getActiveBooklet()
	{
		if ( activeBooklet == null )
		{
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
			String activeBookletId = pref.getString( "activeBookletId", null );
			activeBooklet = Booklet.getBooklet( activeBookletId );
		}

		return activeBooklet;
	}

	public static void setActiveBooklet( String bookletId )
	{
		activeBooklet = null;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
		pref.edit().putString( "activeBookletId", bookletId ).commit();
	}

	public static BaseActivity getActivity()
	{
		if ( activity == null )
			throw new IllegalStateException( "The active activity is null." );
		return activity;
	}

	public static Context getApplicationContext()
	{
		return context;
	}

	public static File getCacheDir()
	{
		return cacheDir;
	}

	public static ExecutorService getExecutorThreadPool()
	{
		return threadPool;
	}

	public static int getImageCacheTimeout()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ContentManager.getApplicationContext() );
		return prefs.getInt( "pref_image_cache", 360 );
	}

	public static boolean isBookletsRefreshing()
	{
		for ( Booklet booklet : Booklet.getBooklets() )
			if ( booklet.isInUse() )
				return true;
		return false;
	}

	public static boolean isContentManagerReady()
	{
		return contentManagerReady;
	}

	public static void onBookletClick( Booklet booklet, View view )
	{
		BookletState state = booklet.getState();

		if ( state == BookletState.AVAILABLE || state == BookletState.OUTDATED )
			booklet.goDownload( view );
		else
		{
			setActiveBooklet( booklet.getId() );
			getActivity().startActivity( new Intent( getActivity(), ContentActivity.class ) );
		}
	}

	public static void onStartActivity( BaseActivity activity )
	{
		ContentManager.activity = activity;
	}

	public static void refreshBooklets()
	{
		if ( !LibAndroid.haveNetworkConnection() )
		{
			if ( activity == null )
				throw new IllegalStateException( "We could not find an active internet connection. Please turn on data or connect to WiFi." );
			activity.uiShowErrorDialog( "We could not find an active internet connection. Please turn on data or connect to WiFi." );
			return;
		}

		Booklet.refreshBooklets();

		Booklet.addBooklet( "ramencon2016" );
		Booklet.addBooklet( "ramencon2017" );

		/*
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url( REMOTE_CATALOG_URL ).build();
		client.newCall( request ).enqueue( new Callback()
		{
			@Override
			public void onFailure( Call call, IOException e )
			{
				e.printStackTrace();
				ACRAHelper.handleExceptionOnce( "content-manager-refresh-failure", e );
				downloadActivity.showErrorDialog( "We had a problem getting the available booklets. Are you connected to the internet?" );
			}

			@Override
			public void onResponse( Call call, Response response ) throws IOException
			{
				try
				{
					JsonConfiguration data = JsonConfiguration.loadConfiguration( response.body().string() );

					for ( ConfigurationSection section : data.getConfigurationSections() )
					{
						Booklet booklet = Booklet.getBooklet( section.getString( "bookletId" ) );

						if ( booklet == null )
						{
							booklet = new Booklet( section.getString( Booklet.KEY_ID ) );
							Booklet.booklets.add( booklet );
						}

						booklet.processData( section );
					}
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
			}
		} );
		*/
	}

	static void setupContentManager( File cacheDir, Context context )
	{
		// Existing context, reload ContentManager
		if ( ContentManager.context != null )
			activeBooklet = null;

		ContentManager.cacheDir = cacheDir;
		ContentManager.context = context;

		Booklet.setup();

		contentManagerReady = true;
	}

	private ContentManager()
	{
		// Static Class
	}
}
