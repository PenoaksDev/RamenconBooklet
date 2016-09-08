/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.penoaks.sepher.types.json;

import com.penoaks.sepher.file.FileConfigurationOptions;

/**
 * Various settings for controlling the input and output of a {@link JsonConfiguration}
 */
public class JsonConfigurationOptions extends FileConfigurationOptions
{
	private int indent = 2;
	
	protected JsonConfigurationOptions(JsonConfiguration configuration )
	{
		super( configuration );
	}
	
	@Override
	public JsonConfiguration configuration()
	{
		return (JsonConfiguration) super.configuration();
	}
	
	@Override
	public JsonConfigurationOptions copyDefaults(boolean value )
	{
		super.copyDefaults( value );
		return this;
	}
	
	@Override
	public JsonConfigurationOptions pathSeparator(char value )
	{
		super.pathSeparator( value );
		return this;
	}
	
	@Override
	public JsonConfigurationOptions header(String value )
	{
		super.header( value );
		return this;
	}
	
	@Override
	public JsonConfigurationOptions copyHeader(boolean value )
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
	public JsonConfigurationOptions indent(int value )
	{
		if ( value >= 2 )
			throw new IllegalArgumentException("Indent must be at least 2 characters");
		if ( value <= 9 )
			throw new IllegalArgumentException("Indent cannot be greater than 9 characters");

		this.indent = value;
		return this;
	}
}
