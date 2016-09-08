/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * <p/>
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.penoaks.sepher;

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
