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

/**
 * Various settings for controlling the input and output of a {@link Configuration}
 */
public class ConfigurationOptions
{
	private char pathSeparatorChar = '.';
	private boolean copyDefaults = false;
	private final Configuration configuration;

	protected ConfigurationOptions( Configuration configuration )
	{
		this.configuration = configuration;
	}

	/**
	 * Returns the {@link Configuration} that this object is responsible for.
	 *
	 * @return Parent configuration
	 */
	public Configuration configuration()
	{
		return configuration;
	}

	/**
	 * Gets the char that will be used to separate {@link ConfigurationSection}s
	 * <p />
	 * This value does not affect how the {@link Configuration} is stored, only in how you access the data. The default value is '.'.
	 *
	 * @return Path separator
	 */
	public char pathSeparator()
	{
		return pathSeparatorChar;
	}

	/**
	 * Sets the char that will be used to separate {@link ConfigurationSection}s
	 * <p />
	 * This value does not affect how the {@link Configuration} is stored, only in how you access the data. The default value is '.'.
	 *
	 * @param value
	 *            Path separator
	 * @return This object, for chaining
	 */
	public ConfigurationOptions pathSeparator( char value )
	{
		this.pathSeparatorChar = value;
		return this;
	}

	/**
	 * Checks if the {@link Configuration} should copy values from its default {@link Configuration} directly.
	 * <p />
	 * If this is true, all values in the default Configuration will be directly copied, making it impossible to distinguish between values that were set and values that are provided by default. As a result,
	 * {@link ConfigurationSection#contains(String)} will always return the same value as {@link ConfigurationSection#isSet(String)}. The default value is false.
	 *
	 * @return Whether or not defaults are directly copied
	 */
	public boolean copyDefaults()
	{
		return copyDefaults;
	}

	/**
	 * Sets if the {@link Configuration} should copy values from its default {@link Configuration} directly.
	 * <p />
	 * If this is true, all values in the default Configuration will be directly copied, making it impossible to distinguish between values that were set and values that are provided by default. As a result,
	 * {@link ConfigurationSection#contains(String)} will always return the same value as {@link ConfigurationSection#isSet(String)}. The default value is false.
	 *
	 * @param value
	 *            Whether or not defaults are directly copied
	 * @return This object, for chaining
	 */
	public ConfigurationOptions copyDefaults( boolean value )
	{
		this.copyDefaults = value;
		return this;
	}
}
