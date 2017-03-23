package com.penoaks.booklet;

import com.penoaks.android.data.DataDownloadBuilder;
import com.penoaks.android.log.PLog;
import com.penoaks.android.utils.UtilMaps;
import com.penoaks.android.utils.UtilObjects;
import com.penoaks.configuration.ConfigurationSection;
import com.penoaks.configuration.InvalidConfigurationException;

import java.io.IOException;

public class DataSource
{
	private Exception lastException = null;
	private DataIntegrityChecker checker;
	private DataPersistence persistence;
	private boolean forceUpdate = false;
	private Boolean integrity = null;
	private String lastError = null;
	private String checksum = null;
	private long lastCheck = 0;
	private String uri;

	DataSource( DataPersistence persistence, String uri, DataIntegrityChecker checker )
	{
		UtilObjects.notNull( uri );

		if ( uri.startsWith( "/" ) )
			uri = uri.substring( 1 );

		if ( UtilObjects.isEmpty( uri ) )
			throw new IllegalArgumentException( "Root data source uri not allowed" );

		this.persistence = persistence;
		this.checker = checker;
		this.uri = uri;
	}

	public String getUri()
	{
		return uri;
	}

	public void tick()
	{
		DataStates state = getDataState( true );
		long currentTimer = state == DataStates.EXCELLENT ? 21600 : state == DataStates.FAIR ? 3600 : 300;
		if ( forceUpdate || AppTicks.epoch() - currentTimer > lastCheck ) // 5 Minutes
			check();
	}

	private void check()
	{
		// TODO Check if a request is already active, if so, this likely means it's hanging

		lastCheck = AppTicks.epoch();

		ConfigurationSection section = persistence.config( uri );
		if ( UtilObjects.isEmpty( checksum ) && section.has( "__checksum" ) )
			checksum = section.getString( "__checksum" );

		// Makes it impossible for the checksum to pass
		if ( forceUpdate )
			checksum = null;

		try
		{
			DataDownloadBuilder.DataResult response = DataDownloadBuilder.fromUrl( DataPersistence.REMOTE_DATA_URL + uri ).post( UtilMaps.newHashMap( "checksum", checksum ) );

			if ( response.responseCode == -1 && response.exception != null )
			{
				response.exception.printStackTrace();
				lastException = response.exception;
			}

			if ( response.responseCode != 200 )
				lastError = "We received an invalid response from the server: " + response.responseCode;
			else
			{
				try
				{
					PLog.i( response.content );
					if ( persistence.feed( uri, response.content ) )
						PLog.i( "Uri " + uri + " was updated, " + checksum + " == " + section.getString( "__checksum" ) );
					else
						PLog.i( "Uri " + uri + " was validated as recent, " + checksum );

					lastException = null;
					lastError = null;
				}
				catch ( InvalidConfigurationException e )
				{
					e.printStackTrace();
					lastException = e;
					lastError = null;
				}
			}
		}
		catch ( IOException e )
		{
			lastException = e;
			e.printStackTrace();
		}
	}

	public void forceUpdate()
	{
		forceUpdate = true;
	}

	public String getLastError()
	{
		return lastError;
	}

	public Exception getLastException()
	{
		return lastException;
	}

	public DataStates getDataState()
	{
		return getDataState( false );
	}

	private DataStates getDataState( boolean recheckIntegrity )
	{
		if ( integrity == null || recheckIntegrity )
			integrity = checker.checkIntegrity( uri, persistence.config( uri ) );

		// 5 Minutes -- Bad Integrity with Errors
		if ( !integrity && ( lastException != null || lastError != null ) )
			return DataStates.BAD;

		// 1 Hour -- Good Integrity with Errors
		if ( integrity && ( lastException != null || lastError != null ) )
			return DataStates.FAIR;

		// 6 Hours -- Good Integrity with No Errors
		if ( integrity && lastException == null && lastError == null )
			return DataStates.EXCELLENT;

		// 5 Minutes -- Bad Integrity with No Errors
		return DataStates.PENDING;
	}

	public interface DataIntegrityChecker
	{
		/**
		 * Used to simply check if data integrity is intact,
		 * meaning the proper nodes for the application to function exist.
		 *
		 * @param uri     The data source uri
		 * @param section The current data
		 * @return True if proper nodes exist, False otherwise
		 */
		boolean checkIntegrity( String uri, ConfigurationSection section );
	}
}
