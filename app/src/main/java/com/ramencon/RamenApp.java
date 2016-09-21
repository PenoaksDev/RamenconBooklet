package com.ramencon;

import android.app.Application;
import android.content.Context;

import com.penoaks.log.PLog;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.data.Persistence;
import com.ramencon.data.UriChecker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ReportsCrashes(
	formUri = "https://collector.tracepot.com/63ad0e5d")
public class RamenApp extends Application
{
	private final static List<MonitorInstance> monitorInst = new ArrayList<>();

	public static void start(MonitorInstance instance)
	{
		monitorInst.add(instance);

		Persistence.getInstance();

		PLog.i("Starting MonitorInstance: " + instance.getClass().getSimpleName());
	}

	public static void stop(MonitorInstance instance)
	{
		PLog.i("Stopping MonitorInstance: " + instance.getClass().getSimpleName());

		monitorInst.remove(instance);

		if (monitorInst.size() == 0 && Persistence.isInstigated())
			Persistence.getInstance().destroy();
	}

	public static File cacheDir;

	@Override
	public void onCreate()
	{
		super.onCreate();

		// FacebookSdk.sdkInitialize(getApplicationContext());
		// AppEventsLogger.activateApp(this);

		cacheDir = getCacheDir();

		Persistence.keepPersistent("booklet-data", new UriChecker()
		{
			@Override
			public boolean isLoaded(String uri, ConfigurationSection root)
			{
				ConfigurationSection uSection = root.getConfigurationSection(uri);

				if (uSection == null)
					return false;

				// PLog.i("Uri Checker for booklet-data: " + uSection.getKeys());

				if (uSection.getConfigurationSection("guests") == null)
					return false;

				if (uSection.getConfigurationSection("locations") == null)
					return false;

				if (uSection.getConfigurationSection("maps") == null)
					return false;

				if (uSection.getConfigurationSection("schedule") == null)
					return false;

				if (uSection.get("dateFormat") == null)
					return false;

				if (uSection.get("timeFormat") == null)
					return false;

				return true;
			}
		});
	}

	@Override
	protected void attachBaseContext(Context base)
	{
		super.attachBaseContext(base);

		ACRA.init(this);
	}
}
