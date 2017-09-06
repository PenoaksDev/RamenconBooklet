package io.amelia.booklet;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import io.amelia.booklet.data.ContentManager;
import io.fabric.sdk.android.Fabric;

/* @ReportsCrashes( formUri = "https://collector.tracepot.com/63ad0e5d" ) */
public final class App extends Application
{
	@Override
	protected void attachBaseContext( Context base )
	{
		super.attachBaseContext( base );

		// ACRA.init( this );
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		ContentManager.setupContentManager( getCacheDir(), getApplicationContext() );

		Iconify.with( new FontAwesomeModule() );

		Fabric.with( this, new Crashlytics() );

		// FacebookSdk.sdkInitialize(getApplicationContext());
		// AppEventsLogger.activateApp(this);
	}
}
