package com.ramencon;

import android.app.Application;
import android.content.Context;

import com.ramencon.data.Persistence;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.io.File;

@ReportsCrashes(
	formUri = "https://collector.tracepot.com/63ad0e5d")
public class RamenApp extends Application
{
	public static File cacheDir;

	@Override
	public void onCreate()
	{
		super.onCreate();

		// FacebookSdk.sdkInitialize(getApplicationContext());
		// AppEventsLogger.activateApp(this);

		cacheDir = getCacheDir();

		Persistence.keepPersistent("booklet-data", false);
	}

	@Override
	protected void attachBaseContext(Context base)
	{
		super.attachBaseContext(base);

		ACRA.init(this);
	}
}
