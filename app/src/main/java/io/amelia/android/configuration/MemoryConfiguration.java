/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.android.configuration;

import java.util.Map;

/**
 * This is a {@link Configuration} implementation that does not save or load from any source, and stores all values in
 * memory only. This is useful for temporary Configurations for providing defaults.
 */
public class MemoryConfiguration extends MemorySection implements Configuration
{
	protected Configuration defaults;
	protected MemoryConfigurationOptions options;

	/**
	 * Creates an empty {@link MemoryConfiguration} with no default values.
	 */
	public MemoryConfiguration()
	{
	}

	/**
	 * Creates an empty {@link MemoryConfiguration} using the specified {@link Configuration} as a source for all default
	 * values.
	 *
	 * @param defaults
	 *             Default value provider
	 * @throws IllegalArgumentException
	 *              Thrown if defaults is null
	 */
	public MemoryConfiguration( Configuration defaults )
	{
		this.defaults = defaults;
	}

	@Override
	public void addDefault( String path, Object value )
	{
		if ( path == null )
			throw new IllegalArgumentException( "Path cannot be null" );

		if ( defaults == null )
			defaults = new MemoryConfiguration();

		defaults.set( path, value, false );
	}

	@Override
	public void addDefaults( Configuration defaults )
	{
		if ( defaults == null )
			throw new IllegalArgumentException( "Defaults cannot be null" );

		addDefaults( defaults.getValues( true ) );
	}

	@Override
	public void addDefaults( Map<String, Object> defaults )
	{
		if ( defaults == null )
			throw new IllegalArgumentException( "Defaults cannot be null" );

		for ( Map.Entry<String, Object> entry : defaults.entrySet() )
			addDefault( entry.getKey(), entry.getValue() );
	}

	@Override
	public Configuration getDefaults()
	{
		return defaults;
	}

	@Override
	public ConfigurationSection getParent()
	{
		return null;
	}

	@Override
	public MemoryConfigurationOptions options()
	{
		if ( options == null )
			options = new MemoryConfigurationOptions( this );

		return options;
	}

	@Override
	public void setDefaults( Configuration defaults )
	{
		if ( defaults == null )
			throw new IllegalArgumentException( "Defaults cannot be null" );

		this.defaults = defaults;
	}
}
