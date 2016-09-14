package com.ramencon;

import android.app.Application;

import com.ramencon.data.Persistence;

import java.io.File;

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
}
