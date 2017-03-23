package com.ramencon;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.penoaks.android.log.PLog;
import com.penoaks.booklet.AppTicks;
import com.penoaks.booklet.DataPersistence;
import com.penoaks.booklet.DataSource;
import com.penoaks.configuration.ConfigurationSection;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ReportsCrashes( formUri = "https://collector.tracepot.com/63ad0e5d" )
public class RamenApp extends MultiDexApplication
{
	private final static List<ContextReference> references = new ArrayList<>();

	public static void start( ContextReference instance )
	{
		PLog.i( "Starting ContextReference: " + instance.getClass().getSimpleName() );
		references.add( instance );

		AppTicks.getInstance();
		DataPersistence.getInstance();

		DataSource.DataIntegrityChecker checker = new DataSource.DataIntegrityChecker()
		{
			@Override
			public boolean checkIntegrity( String uri, ConfigurationSection section )
			{
				// PLog.i( "Data Debug (" + uri + ") (" + ( section.getChildren().size() > 0 ) + ") " + section.getValues( false ) );
				return section.getChildren().size() > 0;
			}
		};

		DataPersistence.getInstance().registerDataSource( "guests", checker );
		DataPersistence.getInstance().registerDataSource( "locations", checker );
		DataPersistence.getInstance().registerDataSource( "maps", checker );
		DataPersistence.getInstance().registerDataSource( "schedule", checker );
		// Persistence.registerDataSource( "settings" );
	}

	public static void stop( ContextReference instance )
	{
		PLog.i( "Stopping ContextReference: " + instance.getClass().getSimpleName() );
		references.remove( instance );

		// When no more ContextReferences exist, we shutdown the data framework
		if ( references.size() == 0 )
		{
			DataPersistence.getInstance().destroy();
			AppTicks.forceStop();
		}
	}

	public static File cacheDir;

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
