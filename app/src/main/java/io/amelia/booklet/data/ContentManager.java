package io.amelia.booklet.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.amelia.android.data.BoundData;
import io.amelia.android.support.DateAndTime;
import io.amelia.android.support.LibAndroid;
import io.amelia.booklet.ui.activity.BaseActivity;
import io.amelia.booklet.ui.activity.BootActivity;

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
	private static BoundData userData;

	static void checkActiveBooklet()
	{
		if ( activeBooklet == null )
		{
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
			String activeBookletId = pref.getString( "activeBookletId", null );
			activeBooklet = Booklet.getBooklet( activeBookletId );
		}
	}

	public static void clearImageCache()
	{
		for ( Booklet booklet : Booklet.booklets )
			booklet.clearImageCache();
	}

	public static void factoryAppReset()
	{
		for ( Booklet booklet : Booklet.booklets )
		{
			booklet.getDataFile().delete();
			booklet.delete();
		}

		getUserDataFile().delete();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
		sharedPreferences.edit().clear().commit();

		setActiveBooklet( null );

		Booklet.setup();

		Booklet.addBooklet( "ramencon2016" );
		Booklet.addBooklet( "ramencon2017" );

		getActivity().startActivity( new Intent( context, BootActivity.class ) );
	}

	public static Booklet getActiveBooklet()
	{
		checkActiveBooklet();
		if ( activeBooklet == null )
			throw new IllegalStateException( "Active Booklet is unset!" );
		return activeBooklet;
	}

	public static void setActiveBooklet( String bookletId )
	{
		activeBooklet = null;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
		pref.edit().putString( "activeBookletId", bookletId ).commit();
	}

	public static Booklet getActiveBookletSafe()
	{
		checkActiveBooklet();
		return activeBooklet;
	}

	public static BaseActivity getActivity()
	{
		if ( activity == null )
			throw new IllegalStateException( "The active activity is null." );
		return activity;
	}

	public static BaseActivity getActivitySafe()
	{
		return activity;
	}

	public static Context getApplicationContext()
	{
		return context;
	}

	public static boolean getAutoUpdate()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ContentManager.getApplicationContext() );
		return prefs.getBoolean( "pref_auto_update", false );
	}

	public static File getCacheDirectory()
	{
		return cacheDir;
	}

	public static String getCalendarAccount()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ContentManager.getApplicationContext() );
		return prefs.getString( "pref_calendar_account", null );
	}

	public static void setCalendarAccount( String calendarAccount )
	{
		if ( calendarAccount == null )
			return;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
		pref.edit().putString( "pref_calendar_account", calendarAccount ).commit();
		Crashlytics.setUserEmail( calendarAccount );
	}

	public static int getEventReminderDelay()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ContentManager.getApplicationContext() );
		return Integer.parseInt( prefs.getString( "pref_reminder_delays", "10" ) );
	}

	public static ExecutorService getExecutorThreadPool()
	{
		return threadPool;
	}

	public static int getImageCacheTimeout()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ContentManager.getApplicationContext() );
		return Integer.parseInt( prefs.getString( "pref_image_cache", "360" ) );
	}

	public static Booklet getLatestBooklet()
	{
		SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );
		long now = DateAndTime.epoch();
		Booklet last = null;
		long lastDiff = 9999999999L;
		for ( Booklet booklet : Booklet.booklets )
		{
			try
			{
				Date whenFromDate = date.parse( booklet.getData().getString( Booklet.KEY_WHEN_FROM ) );
				long diff = now - whenFromDate.getTime();
				if ( diff < lastDiff )
				{
					lastDiff = diff;
					last = booklet;
				}
			}
			catch ( ParseException e )
			{
				e.printStackTrace();
			}
		}
		return last;
	}

	public static BoundData getUserData()
	{
		if ( userData == null )
			try
			{
				userData = LibAndroid.readJsonToBoundData( getUserDataFile() )[0];
			}
			catch ( IOException e )
			{
				userData = new BoundData();
			}
		return userData;
	}

	public static File getUserDataFile()
	{
		return new File( cacheDir, "user.json" );
	}

	public static boolean hasActiveBooklet()
	{
		checkActiveBooklet();
		return activeBooklet != null;
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

	public static void saveUserData()
	{
		if ( userData != null )
			try
			{
				LibAndroid.writeBoundDataToJsonFile( userData, getUserDataFile() );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
	}

	public static void setupContentManager( File cacheDir, Context context )
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
