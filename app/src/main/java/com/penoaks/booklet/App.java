package com.penoaks.booklet;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ReportsCrashes( formUri = "https://collector.tracepot.com/63ad0e5d" )
public final class App extends Application
{
	public static final String REMOTE_CATALOG_URL = "http://booklet.dev.penoaks.com/catalog/ramencon";
	public static final String REMOTE_DATA_URL = "http://booklet.dev.penoaks.com/data/";

	private static final ExecutorService threadPool = Executors.newCachedThreadPool();
	public static File cacheDir;

	public static ExecutorService getExecutorThreadPool()
	{
		return threadPool;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		// FacebookSdk.sdkInitialize(getApplicationContext());
		// AppEventsLogger.activateApp(this);

		cacheDir = getCacheDir();
	}

	@Override
	protected void attachBaseContext( Context base )
	{
		super.attachBaseContext( base );

		ACRA.init( this );
	}
}
