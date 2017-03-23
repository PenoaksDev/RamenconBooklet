package com.penoaks.booklet;

import com.penoaks.android.log.PLog;
import com.penoaks.android.utils.UtilObjects;
import com.penoaks.booklet.DataSource.DataIntegrityChecker;
import com.penoaks.configuration.ConfigurationSection;
import com.penoaks.configuration.InvalidConfigurationException;
import com.penoaks.configuration.OnConfigurationListener;
import com.penoaks.configuration.types.json.JsonConfiguration;
import com.ramencon.RamenApp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import groovy.json.JsonSlurper;
import groovy.transform.CompileStatic;

@CompileStatic
public class DataPersistence implements OnConfigurationListener, AppTicks.EventReceiver
{
	public static final String REMOTE_DATA_URL = "http://booklet.dev.penoaks.com/data/ramencon/";

	private static DataPersistence instance = null;

	public static DataPersistence getInstance()
	{
		if ( instance == null )
			instance = new DataPersistence();
		return instance;
	}

	private final List<DataSource> registered = new CopyOnWriteArrayList<>();
	private final JsonConfiguration root = new JsonConfiguration();
	private JsonSlurper jsonSlurper = new JsonSlurper();
	private final File persistenceFile;

	private DataPersistence()
	{
		persistenceFile = new File( RamenApp.cacheDir, "PersistenceData.json" );
		root.options().pathSeparator( "/".charAt( 0 ) );

		try
		{
			if ( persistenceFile.exists() )
			{
				root.load( persistenceFile );
				PLog.i( "Persistence file was loaded!" );
			}
			else
				PLog.i( "Persistence file was missing!" );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		root.addListener( this );

		AppTicks.getInstance().registerReceiver( "persistence", this );
	}

	public void registerDataSource( String uri, DataIntegrityChecker checker )
	{
		for ( DataSource ds : registered )
		{
			if ( ds.getUri() == uri || ds.getUri().startsWith( uri ) )
				return;
			if ( uri.startsWith( ds.getUri() ) )
				registered.remove( ds );
		}

		registered.add( new DataSource( this, uri, checker ) );
	}

	public String getFirstError()
	{
		for ( DataSource ds : registered )
			if ( ds.getLastError() != null )
				return ds.getLastError();
		return null;
	}

	public Exception getFirstException()
	{
		for ( DataSource ds : registered )
			if ( ds.getLastException() != null )
				return ds.getLastException();
		return null;
	}

	public AppStates eventState()
	{
		for ( DataSource ds : registered )
		{
			switch ( ds.getDataState() )
			{
				case BAD:
					if ( ds.getLastException() != null )
						return AppStates.PERSISTENCE_EXCEPTION;
					if ( ds.getLastError() != null )
						return AppStates.PERSISTENCE_ERRORED;
					break;
				case PENDING:
					return AppStates.PENDING;
			}
		}

		return AppStates.SUCCESS;
	}

	@Override
	public void onTick( long tick )
	{
		for ( DataSource ds : registered )
			ds.tick();
	}

	public boolean feed( String uri, String json ) throws InvalidConfigurationException
	{
		JsonConfiguration result = JsonConfiguration.loadConfiguration( json );

		ConfigurationSection details = result.getConfigurationSection( "details" );

		// Code 1: Indicates changes were received
		if ( details.getInt( "code" ) == 1 && result.isConfigurationSection( "data" ) )
		{
			ConfigurationSection section = config( uri );

			section.merge( result.getConfigurationSection( "data" ) );
			section.set( "__checksum", details.getString( "checksum" ) );

			return true;
		}

		return false;
	}

	public ConfigurationSection config()
	{
		return config( null );
	}

	public ConfigurationSection config( String path )
	{
		if ( UtilObjects.isEmpty( path ) )
			return root;

		ConfigurationSection section = root.getConfigurationSection( path, true );

		if ( section == null )
			section = root.createSection( path );

		assert section != null;

		return section;
	}

	public void forceUpdate( String uri )
	{
		UtilObjects.notNull( uri );

		if ( uri.startsWith( "/" ) )
			uri = uri.substring( 1 );

		if ( UtilObjects.isEmpty( uri ) )
			throw new IllegalArgumentException( "Root data source update not allowed" );

		for ( DataSource ds : registered )
			if ( uri.startsWith( ds.getUri() ) )
				ds.forceUpdate();
	}

	public void forceUpdate()
	{
		for ( DataSource ds : registered )
			ds.forceUpdate();
	}

	public void factoryReset()
	{
		root.clear();
		persistenceFile.delete();
	}

	public void save()
	{
		synchronized ( root )
		{
			try
			{
				PLog.i( "Saved JSON Data: " + root.saveToString() );

				root.save( persistenceFile );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onSectionAdd( ConfigurationSection section )
	{

	}

	@Override
	public void onSectionRemove( ConfigurationSection parent, ConfigurationSection orphanedChild )
	{
		// if ( loading )
		// return

		// database.getReference( orphanedChild.getCurrentPath() ).removeValue();

		// save();
	}

	@Override
	public void onSectionChange( ConfigurationSection parent, String affectedKey )
	{
		/* if ( loading )
			return

		database.getReference( parent.getCurrentPath() + "/" + affectedKey ).setValue( parent.config( affectedKey ) );

		save(); */
	}

	@Override
	public void onException( Throwable cause )
	{

	}

	@Override
	public void onShutdown()
	{

	}

	public void destroy()
	{
		save();
		AppTicks.getInstance().unregisterReceiver( this );
		instance = null;
	}
}
