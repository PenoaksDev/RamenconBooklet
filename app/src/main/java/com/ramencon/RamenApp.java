package com.ramencon;

import android.app.Application;
import android.content.Context;

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
}
