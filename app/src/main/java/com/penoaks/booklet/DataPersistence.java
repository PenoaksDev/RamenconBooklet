package com.penoaks.booklet;

import com.penoaks.android.log.PLog;
import com.penoaks.android.utils.UtilObjects;
import com.penoaks.booklet.DataSource.DataIntegrityChecker;
import com.penoaks.configuration.ConfigurationSection;
import com.penoaks.configuration.InvalidConfigurationException;
import com.penoaks.configuration.OnConfigurationListener;
import com.penoaks.configuration.types.json.JsonConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataPersistence implements OnConfigurationListener
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
	private final JsonConfiguration dataRoot = new JsonConfiguration();
	private final File persistenceFile;

	private DataPersistence()
	{
		UtilObjects.notNull( App.cacheDir );

		persistenceFile = new File( App.cacheDir, "PersistenceData.json" );
		dataRoot.options().pathSeparator( '/' );

		try
		{
			if ( persistenceFile.exists() )
			{
				dataRoot.load( persistenceFile );
				PLog.i( "Persistence file was loaded!" );
			}
			else
				PLog.i( "Persistence file was missing!" );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		dataRoot.addListener( this );
	}

	public void registerDataSource( String localUri, String remoteUri, DataIntegrityChecker checker )
	{
		for ( DataSource ds : registered )
		{
			if ( ds.getLocalUri() == localUri || ds.getLocalUri().startsWith( localUri ) )
				return;
			if ( localUri.startsWith( ds.getLocalUri() ) )
				registered.remove( ds );
		}

		registered.add( new DataSource( this, localUri, remoteUri, checker ) );
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

	public void heartbeat( int currentTick )
	{
		for ( DataSource ds : registered )
			ds.tick();
	}

	public boolean feed( String localUri, String remoteUri, String json ) throws InvalidConfigurationException
	{
		JsonConfiguration result = JsonConfiguration.loadConfiguration( json );
		result.options().pathSeparator( '/' );

		ConfigurationSection details = result.getConfigurationSection( "details" );

		// Code 1: Indicates changes were received
		if ( details.getInt( "code" ) == 1 && result.isConfigurationSection( "data" ) )
		{
			ConfigurationSection section = config( localUri );

			section.merge( result.getConfigurationSection( "data" ).getConfigurationSection( remoteUri ) );
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
			return dataRoot;

		ConfigurationSection section = dataRoot.getConfigurationSection( path, true );

		if ( section == null )
			section = dataRoot.createSection( path );

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
			if ( uri.startsWith( ds.getLocalUri() ) )
				ds.forceUpdate();
	}

	public void forceUpdate()
	{
		for ( DataSource ds : registered )
			ds.forceUpdate();
	}

	public void factoryReset()
	{
		dataRoot.clear();
		persistenceFile.delete();
	}

	public void save()
	{
		synchronized ( dataRoot )
		{
			try
			{
				PLog.i( "Saved JSON Data: " + dataRoot.saveToString() );

				dataRoot.save( persistenceFile );
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

	public void destroy()
	{
		save();
		// App.getInstance().getEventManager().unregisterEvents( this );
		instance = null;
	}
}
