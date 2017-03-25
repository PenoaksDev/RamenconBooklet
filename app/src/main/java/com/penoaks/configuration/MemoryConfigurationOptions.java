/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.configuration;

/**
 * Various settings for controlling the input and output of a {@link MemoryConfiguration}
 */
public class MemoryConfigurationOptions extends ConfigurationOptions
{
	protected MemoryConfigurationOptions(MemoryConfiguration configuration)
	{
		super(configuration);
	}

	@Override
	public MemoryConfiguration configuration()
	{
		return (MemoryConfiguration) super.configuration();
	}

	@Override
	public MemoryConfigurationOptions copyDefaults(boolean value)
	{
		super.copyDefaults(value);
		return this;
	}

	@Override
	public MemoryConfigurationOptions pathSeparator(char value)
	{
		super.pathSeparator(value);
		return this;
	}
}
