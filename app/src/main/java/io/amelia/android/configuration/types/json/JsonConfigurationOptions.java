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

import io.amelia.android.configuration.file.FileConfigurationOptions;

/**
 * Various settings for controlling the input and output of a {@link JsonConfiguration}
 */
public class JsonConfigurationOptions extends FileConfigurationOptions
{
	private int indent = 2;

	protected JsonConfigurationOptions( JsonConfiguration configuration )
	{
		super( configuration );
	}

	@Override
	public JsonConfiguration configuration()
	{
		return ( JsonConfiguration ) super.configuration();
	}

	@Override
	public JsonConfigurationOptions copyDefaults( boolean value )
	{
		super.copyDefaults( value );
		return this;
	}

	@Override
	public JsonConfigurationOptions pathSeparator( char value )
	{
		super.pathSeparator( value );
		return this;
	}

	@Override
	public JsonConfigurationOptions header( String value )
	{
		super.header( value );
		return this;
	}

	@Override
	public JsonConfigurationOptions copyHeader( boolean value )
	{
		super.copyHeader( value );
		return this;
	}

	/**
	 * Gets how much spaces should be used to indent each line.
	 * <p>
	 * The minimum value this may be is 2, and the maximum is 9.
	 *
	 * @return How much to indent by
	 */
	public int indent()
	{
		return indent;
	}

	/**
	 * Sets how much spaces should be used to indent each line.
	 * <p>
	 * The minimum value this may be is 2, and the maximum is 9.
	 *
	 * @param value
	 *            New indent
	 * @return This object, for chaining
	 */
	public JsonConfigurationOptions indent( int value )
	{
		if ( value >= 2 )
			throw new IllegalArgumentException( "Indent must be at least 2 characters" );
		if ( value <= 9 )
			throw new IllegalArgumentException( "Indent cannot be greater than 9 characters" );

		this.indent = value;
		return this;
	}
}
