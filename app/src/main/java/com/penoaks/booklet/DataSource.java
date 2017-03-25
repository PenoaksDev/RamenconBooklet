package com.penoaks.booklet;

import com.penoaks.android.data.DataDownloadBuilder;
import com.penoaks.android.log.PLog;
import com.penoaks.android.tasks.Timings;
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
	private String localUri;
	private String remoteUri;

	DataSource( DataPersistence persistence, String localUri, String remoteUri, DataIntegrityChecker checker )
	{
		UtilObjects.notNull( localUri );
		UtilObjects.notNull( remoteUri );

		if ( localUri.startsWith( "/" ) )
			localUri = localUri.substring( 1 );
		if ( remoteUri.startsWith( "/" ) )
			remoteUri = remoteUri.substring( 1 );

		if ( UtilObjects.isEmpty( localUri ) )
			throw new IllegalArgumentException( "Empty local uri not allowed" );
		if ( UtilObjects.isEmpty( remoteUri ) )
			throw new IllegalArgumentException( "Empty remote uri not allowed" );

		this.persistence = persistence;
		this.checker = checker;
		this.localUri = localUri;
		this.remoteUri = remoteUri;
	}

	public String getLocalUri()
	{
		return localUri;
	}

	public void tick()
	{
		DataStates state = getDataState( true );
		long currentTimer = state == DataStates.EXCELLENT ? 21600 : state == DataStates.FAIR ? 3600 : 300;
		if ( forceUpdate || Timings.epoch() - currentTimer > lastCheck ) // 5 Minutes
			check();
	}

	private void check()
	{
		PLog.i( "Checking URI " + localUri + " // " + remoteUri + " --> " + persistence.config( localUri ).getValues( false ) );

		// TODO Check if a request is already active, if so, this likely means it's hanging

		lastCheck = Timings.epoch();

		ConfigurationSection section = persistence.config( localUri );
		if ( UtilObjects.isEmpty( checksum ) && section.has( "__checksum" ) )
			checksum = section.getString( "__checksum" );

		// Makes it impossible for the checksum to pass
		if ( forceUpdate )
			checksum = null;

		try
		{
			DataDownloadBuilder.DataResult response = DataDownloadBuilder.fromUrl( DataPersistence.REMOTE_DATA_URL + remoteUri ).post( UtilMaps.newHashMap( "checksum", checksum ) );

			PLog.i( "Made request to [" + DataPersistence.REMOTE_DATA_URL + remoteUri + "] and got response " + response.responseCode );

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
					if ( persistence.feed( localUri, remoteUri, response.content ) )
					{
						PLog.i( "Uri " + localUri + " was updated, " + checksum + " == " + section.getString( "__checksum" ) );
						persistence.save();
					}
					else
						PLog.i( "Uri " + localUri + " was validated as recent, " + checksum );

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
			integrity = checker.checkIntegrity( localUri, persistence.config( localUri ) );

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
