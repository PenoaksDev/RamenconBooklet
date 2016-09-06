package com.ramencon;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
	formUri = "https://collector.tracepot.com/63ad0e5d")
public class RamenApp extends Application
{
	@Override
	protected void attachBaseContext(Context base)
	{
		super.attachBaseContext(base);

		ACRA.init(this);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		FacebookSdk.sdkInitialize(getApplicationContext());
		AppEventsLogger.activateApp(this);
	}
}
