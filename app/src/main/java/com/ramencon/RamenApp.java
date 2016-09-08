package com.ramencon;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.penoaks.data.Persistence;

public class RamenApp extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();

		FacebookSdk.sdkInitialize(getApplicationContext());
		AppEventsLogger.activateApp(this);
	}
}
