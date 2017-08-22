/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.android.configuration.types.json;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.amelia.android.configuration.Configuration;
import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.configuration.InvalidConfigurationException;
import io.amelia.android.configuration.file.FileConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml. Note that this implementation is not
 * synchronized.
 */
public class JsonConfiguration extends FileConfiguration
{
	protected static final String COMMENT_PREFIX = "# ";
	protected static final String BLANK_CONFIG = "{}\n";

	/**
	 * Creates a new {@link JsonConfiguration}, loading from the given file.
	 * <p/>
	 * Any errors loading the Configuration will be logged and then ignored. If the specified input is not a valid config, a blank config will be returned.
	 *
	 * @param file Input file
	 * @return Resulting configuration
	 * @throws IllegalArgumentException Thrown if file is null
	 */
	public static JsonConfiguration loadConfiguration( File file )
	{
		if ( file == null )
			throw new IllegalArgumentException( "File cannot be null" );

		JsonConfiguration config = new JsonConfiguration();

		try
		{
			config.load( file );
		}
		catch ( FileNotFoundException ex )
		{
			// Ignore
		}
		catch ( IOException ex )
		{
			Log.e( "Config", String.format( "Cannot load %s", file ), ex );
		}
		catch ( InvalidConfigurationException ex )
		{
			Log.e( "Config", String.format( "Cannot load %s", file ), ex );
		}

		return config;
	}

	/**
	 * Creates a new {@link JsonConfiguration}, loading from the given stream.
	 * <p/>
	 * Any errors loading the Configuration will be logged and then ignored. If the specified input is not a valid config, a blank config will be returned.
	 *
	 * @param stream Input stream
	 * @return Resulting configuration
	 * @throws IllegalArgumentException Thrown if stream is null
	 */
	public static JsonConfiguration loadConfiguration( InputStream stream )
	{
		if ( stream == null )
			throw new IllegalArgumentException( "Stream cannot be null" );

		JsonConfiguration config = new JsonConfiguration();

		try
		{
			config.load( stream );
		}
		catch ( IOException ex )
		{
			Log.e( "Config", "Cannot load configuration from stream", ex );
		}
		catch ( InvalidConfigurationException ex )
		{
			Log.e( "Config", "Cannot load configuration from stream", ex );
		}

		return config;
	}

	public static JsonConfiguration loadConfiguration( String contents )
	{
		try
		{
			return loadConfigurationWithException( contents );
		}
		catch ( InvalidConfigurationException ex )
		{
			Log.e( "Config", "Cannot load configuration from stream", ex );
		}
		return new JsonConfiguration();
	}

	public static JsonConfiguration loadConfigurationWithException( String contents ) throws InvalidConfigurationException
	{
		if ( contents == null )
			throw new IllegalArgumentException( "Contents cannot be null" );

		JsonConfiguration config = new JsonConfiguration();
		config.loadFromString( contents );
		return config;
	}

	@Override
	public String buildHeader()
	{
		String header = options().header();

		if ( options().copyHeader() )
		{
			Configuration def = getDefaults();

			if ( def != null && def instanceof JsonConfiguration )
			{
				JsonConfiguration filedefaults = ( JsonConfiguration ) def;
				String defaultsHeader = filedefaults.buildHeader();

				if ( defaultsHeader != null && defaultsHeader.length() > 0 )
					return defaultsHeader;
			}
		}

		if ( header == null )
			return "";

		StringBuilder builder = new StringBuilder();
		String[] lines = header.split( "\r?\n", -1 );
		boolean startedHeader = false;

		for ( int i = lines.length - 1; i >= 0; i-- )
		{
			builder.insert( 0, "\n" );

			if ( startedHeader || lines[i].length() != 0 )
			{
				builder.insert( 0, lines[i] );
				builder.insert( 0, COMMENT_PREFIX );
				startedHeader = true;
			}
		}

		return builder.toString();
	}

	public void copy( String fromPath, String toPath )
	{
		ConfigurationSection oldSection = getConfigurationSection( fromPath );
		if ( oldSection == null )
			return;
		ConfigurationSection newSection = getConfigurationSection( toPath, true );
		for ( String key : oldSection.getKeys( true ) )
			newSection.set( key, oldSection.get( key ), false );
	}

	@Override
	public void loadFromString( String contents ) throws InvalidConfigurationException
	{
		Log.w( "Config", "Loaded JSON Data: " + contents );

		if ( contents == null )
			throw new IllegalArgumentException( "Contents cannot be null" );

		try
		{
			String header = parseHeader( contents );
			if ( header.length() > 0 )
				options().header( header );

			if ( contents.trim().startsWith( "[" ) )
			{
				JSONArray input = new JSONArray( contents );

				if ( input != null )
					convertJsonToSections( input, this );
			}
			else
			{
				JSONObject input = new JSONObject( contents );

				if ( input != null )
					convertJsonToSections( input, this );
			}
		}
		catch ( JSONException e )
		{
			throw new InvalidConfigurationException( e );
		}
	}

	protected void convertJsonToSections( String name, Object input, ConfigurationSection section ) throws JSONException
	{
		if ( input instanceof JSONArray )
			convertJsonToSections( ( JSONArray ) input, section.createSection( name ) );
		else if ( input instanceof JSONObject )
			convertJsonToSections( ( JSONObject ) input, section.createSection( name ) );
		else
			section.set( name, input, false );
	}

	protected void convertJsonToSections( JSONArray input, ConfigurationSection section ) throws JSONException
	{
		for ( int i = 0; i < input.length(); i++ )
		{
			String name = Integer.toString( i );
			Object obj = input.get( i );

			if ( obj instanceof JSONArray )
				convertJsonToSections( ( JSONArray ) obj, section.createSection( name ) );
			else if ( obj instanceof JSONObject )
				convertJsonToSections( ( JSONObject ) obj, section.createSection( name ) );
			else
				section.set( name, obj, false );
		}
	}

	protected void convertJsonToSections( JSONObject input, ConfigurationSection section ) throws JSONException
	{
		Iterator<String> keys = input.keys();
		while ( keys.hasNext() )
		{
			String name = keys.next();
			convertJsonToSections( name, input.get( name ), section );
		}
	}

	public void move( String fromPath, String toPath )
	{
		copy( fromPath, toPath );
		set( fromPath, null, false );
	}

	@Override
	public JsonConfigurationOptions options()
	{
		if ( options == null )
			options = new JsonConfigurationOptions( this );

		return ( JsonConfigurationOptions ) options;
	}

	protected String parseHeader( String input )
	{
		String[] lines = input.split( "\r?\n", -1 );
		StringBuilder result = new StringBuilder();
		boolean readingHeader = true;
		boolean foundHeader = false;

		for ( int i = 0; i < lines.length && readingHeader; i++ )
		{
			String line = lines[i];

			if ( line.startsWith( COMMENT_PREFIX ) )
			{
				if ( i > 0 )
					result.append( "\n" );

				if ( line.length() > COMMENT_PREFIX.length() )
					result.append( line.substring( COMMENT_PREFIX.length() ) );

				foundHeader = true;
			}
			else if ( foundHeader && line.length() == 0 )
				result.append( "\n" );
			else if ( foundHeader )
				readingHeader = false;
		}

		return result.toString();
	}

	@Override
	public String saveToString()
	{
		Gson gson = new GsonBuilder().create();
		String dump = gson.toJson( getValues( false ) );

		String header = buildHeader();

		if ( dump.equals( BLANK_CONFIG ) )
			dump = "";

		return header + dump;
	}
}
