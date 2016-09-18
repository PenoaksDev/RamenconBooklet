/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * <p/>
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.penoaks.sepher.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.penoaks.sepher.Configuration;
import com.penoaks.sepher.InvalidConfigurationException;
import com.penoaks.sepher.MemoryConfiguration;

/**
 * This is a base class for all File based implementations of {@link Configuration}
 */
public abstract class FileConfiguration extends MemoryConfiguration
{
	private String loadedFrom = null;
	protected boolean hasChanges = false;

	/**
	 * Creates an empty {@link FileConfiguration} with no default values.
	 */
	public FileConfiguration()
	{
		super();
	}

	/**
	 * Creates an empty {@link FileConfiguration} using the specified {@link Configuration} as a source for all default
	 * values.
	 *
	 * @param defaults Default value provider
	 */
	public FileConfiguration(Configuration defaults)
	{
		super(defaults);
	}

	/**
	 * Compiles the header for this {@link FileConfiguration} and returns the result.
	 * <p/>
	 * This will use the header from {@link #options()} -> {@link FileConfigurationOptions#header()}, respecting the rules of {@link FileConfigurationOptions#copyHeader()} if set.
	 *
	 * @return Compiled header
	 */
	public abstract String buildHeader();

	/**
	 * Loads this {@link FileConfiguration} from the specified location.
	 * <p/>
	 * All the values contained within this configuration will be removed, leaving only settings and defaults, and the new values will be loaded from the given file.
	 * <p/>
	 * If the file cannot be loaded for any reason, an exception will be thrown.
	 *
	 * @param file File to load from.
	 * @throws FileNotFoundException         Thrown when the given file cannot be opened.
	 * @throws IOException                   Thrown when the given file cannot be read.
	 * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
	 * @throws IllegalArgumentException      Thrown when file is null.
	 */
	public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		if (file == null)
			throw new IllegalArgumentException("File cannot be null");

		load(new FileInputStream(file));
		loadedFrom = file.getAbsolutePath();
	}

	/**
	 * Loads this {@link FileConfiguration} from the specified stream.
	 * <p/>
	 * All the values contained within this configuration will be removed, leaving only settings and defaults, and the new values will be loaded from the given stream.
	 *
	 * @param stream Stream to load from
	 * @throws IOException                   Thrown when the given file cannot be read.
	 * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
	 * @throws IllegalArgumentException      Thrown when stream is null.
	 */
	public void load(InputStream stream) throws IOException, InvalidConfigurationException
	{
		if (stream == null)
			throw new IllegalArgumentException("Stream cannot be null");

		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder builder = new StringBuilder();
		BufferedReader input = new BufferedReader(reader);

		try
		{
			String line;

			while ((line = input.readLine()) != null)
			{
				builder.append(line);
				builder.append('\n');
			}
		}
		finally
		{
			input.close();
		}

		loadFromString(builder.toString());
		loadedFrom = null;
		hasChanges = true;
	}

	/**
	 * Loads this {@link FileConfiguration} from the specified location.
	 * <p/>
	 * All the values contained within this configuration will be removed, leaving only settings and defaults, and the new values will be loaded from the given file.
	 * <p/>
	 * If the file cannot be loaded for any reason, an exception will be thrown.
	 *
	 * @param file File to load from.
	 * @throws FileNotFoundException         Thrown when the given file cannot be opened.
	 * @throws IOException                   Thrown when the given file cannot be read.
	 * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
	 * @throws IllegalArgumentException      Thrown when file is null.
	 */
	public void load(String file) throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		if (file == null)
			throw new IllegalArgumentException("File cannot be null");

		load(new File(file));
	}

	public void loadedFrom(String path)
	{
		loadedFrom = path;
	}

	public String loadedFrom()
	{
		return loadedFrom;
	}

	/**
	 * Loads this {@link FileConfiguration} from the specified string, as opposed to from file.
	 * <p/>
	 * All the values contained within this configuration will be removed, leaving only settings and defaults, and the new values will be loaded from the given string.
	 * <p/>
	 * If the string is invalid in any way, an exception will be thrown.
	 *
	 * @param contents Contents of a Configuration to load.
	 * @throws InvalidConfigurationException Thrown if the specified string is invalid.
	 * @throws IllegalArgumentException      Thrown if contents is null.
	 */
	public abstract void loadFromString(String contents) throws InvalidConfigurationException;

	@Override
	public FileConfigurationOptions options()
	{
		if (options == null)
			options = new FileConfigurationOptions(this);

		return (FileConfigurationOptions) options;
	}

	/**
	 * Saves this {@link FileConfiguration} to the specified location.
	 * <p/>
	 * If the file does not exist, it will be created. If already exists, it will be overwritten. If it cannot be overwritten or created, an exception will be thrown.
	 *
	 * @param file File to save to.
	 * @throws IOException              Thrown when the given file cannot be written to for any reason.
	 * @throws IllegalArgumentException Thrown when file is null.
	 */
	public void save(File file) throws IOException
	{
		if (file == null)
			throw new IllegalArgumentException("File cannot be null");

		file.getParentFile().mkdirs();

		String data = saveToString();
		hasChanges = false;

		FileWriter writer = new FileWriter(file);

		try
		{
			writer.write(data);
		}
		finally
		{
			writer.close();
		}
	}

	/**
	 * Saves this {@link FileConfiguration} to the specified location.
	 * <p/>
	 * If the file does not exist, it will be created. If already exists, it will be overwritten. If it cannot be overwritten or created, an exception will be thrown.
	 *
	 * @param file File to save to.
	 * @throws IOException              Thrown when the given file cannot be written to for any reason.
	 * @throws IllegalArgumentException Thrown when file is null.
	 */
	public void save(String file) throws IOException
	{
		if (file == null)
			throw new IllegalArgumentException("File cannot be null");

		save(new File(file));
	}

	/**
	 * Saves this {@link FileConfiguration} to a string, and returns it.
	 *
	 * @return String containing this configuration.
	 */
	public abstract String saveToString();
}