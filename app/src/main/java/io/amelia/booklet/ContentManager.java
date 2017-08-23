package io.amelia.booklet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.amelia.android.support.LibAndroid;
import io.amelia.booklet.ui.activity.DownloadActivity;

public class ContentManager
{
	public static final String REMOTE_CATALOG_URL = "http://booklet.dev.penoaks.com/catalog/";
	// public static final String REMOTE_CATALOG_URL = "http://booklet.dev.penoaks.com/catalog/search?startsWith=ramencon";
	public static final String REMOTE_DATA_URL = "http://booklet.dev.penoaks.com/data/";

	static final ExecutorService threadPool = Executors.newCachedThreadPool();
	private static Booklet activeBooklet;
	private static File cacheDir;
	private static boolean contentManagerReady = false;
	private static Context context;
	private static DownloadActivity downloadActivity;

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

	public static Context getApplicationContext()
	{
		return context;
	}

	public static File getCacheDir()
	{
		return cacheDir;
	}

	public static DownloadActivity getDownloadActivity()
	{
		return downloadActivity;
	}

	public static void setDownloadActivity( DownloadActivity downloadActivity )
	{
		ContentManager.downloadActivity = downloadActivity;

		refreshBooklets();
	}

	public static ExecutorService getExecutorThreadPool()
	{
		return threadPool;
	}

	public static boolean isContentManagerReady()
	{
		return contentManagerReady;
	}

	public static void refreshBooklets()
	{
		if ( !LibAndroid.haveNetworkConnection() )
		{
			downloadActivity.showErrorDialog( "We could not find an active internet connection. Please turn on data or connect to WiFi." );
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
