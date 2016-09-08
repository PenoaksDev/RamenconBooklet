/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * <p/>
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.penoaks.sepher.types.yaml;

import com.penoaks.log.PLog;
import com.penoaks.sepher.Configuration;
import com.penoaks.sepher.ConfigurationSection;
import com.penoaks.sepher.InvalidConfigurationException;
import com.penoaks.sepher.file.FileConfiguration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml. Note that this implementation is not
 * synchronized.
 */
public class YamlConfiguration extends FileConfiguration
{
	protected static final String COMMENT_PREFIX = "# ";
	protected static final String BLANK_CONFIG = "{}\n";

	/**
	 * Creates a new {@link YamlConfiguration}, loading from the given file.
	 * <p/>
	 * Any errors loading the Configuration will be logged and then ignored. If the specified input is not a valid config, a blank config will be returned.
	 *
	 * @param file Input file
	 * @return Resulting configuration
	 * @throws IllegalArgumentException Thrown if file is null
	 */
	public static YamlConfiguration loadConfiguration(File file)
	{
		if (file == null)
			throw new IllegalArgumentException("File cannot be null");

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(file);
		}
		catch (FileNotFoundException ex)
		{

		}
		catch (IOException ex)
		{
			PLog.e(ex, "Cannot load %s", file);
		}
		catch (InvalidConfigurationException ex)
		{
			PLog.e(ex, "Cannot load %s", file);
		}

		return config;
	}

	/**
	 * Creates a new {@link YamlConfiguration}, loading from the given stream.
	 * <p/>
	 * Any errors loading the Configuration will be logged and then ignored. If the specified input is not a valid config, a blank config will be returned.
	 *
	 * @param stream Input stream
	 * @return Resulting configuration
	 * @throws IllegalArgumentException Thrown if stream is null
	 */
	public static YamlConfiguration loadConfiguration(InputStream stream)
	{
		if (stream == null)
			throw new IllegalArgumentException("Stream cannot be null");

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(stream);
		}
		catch (IOException ex)
		{
			PLog.e(ex, "Cannot load configuration from stream");
		}
		catch (InvalidConfigurationException ex)
		{
			PLog.e(ex, "Cannot load configuration from stream");
		}

		return config;
	}

	public static YamlConfiguration loadConfiguration(String contents)
	{
		if (contents == null)
			throw new IllegalArgumentException("Contents cannot be null");

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.loadFromString(contents);
		}
		catch (InvalidConfigurationException ex)
		{
			PLog.e(ex, "Cannot load configuration from stream");
		}

		return config;
	}

	private final DumperOptions yamlOptions = new DumperOptions();

	private final Representer yamlRepresenter = new YamlRepresenter();

	private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

	@Override
	public String buildHeader()
	{
		String header = options().header();

		if (options().copyHeader())
		{
			Configuration def = getDefaults();

			if (def != null && def instanceof FileConfiguration)
			{
				FileConfiguration filedefaults = (FileConfiguration) def;
				String defaultsHeader = filedefaults.buildHeader();

				if (defaultsHeader != null && defaultsHeader.length() > 0)
					return defaultsHeader;
			}
		}

		if (header == null)
			return "";

		StringBuilder builder = new StringBuilder();
		String[] lines = header.split("\r?\n", -1);
		boolean startedHeader = false;

		for (int i = lines.length - 1; i >= 0; i--)
		{
			builder.insert(0, "\n");

			if (startedHeader || lines[i].length() != 0)
			{
				builder.insert(0, lines[i]);
				builder.insert(0, COMMENT_PREFIX);
				startedHeader = true;
			}
		}

		return builder.toString();
	}

	protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section)
	{
		for (Map.Entry<?, ?> entry : input.entrySet())
		{
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if (value instanceof Map)
				convertMapsToSections((Map<?, ?>) value, section.createSection(key));
			else
				section.set(key, value);
		}
	}

	public void copy(String fromPath, String toPath)
	{
		ConfigurationSection oldSection = getConfigurationSection(fromPath);
		if (oldSection == null)
			return;
		ConfigurationSection newSection = getConfigurationSection(toPath, true);
		for (String key : oldSection.getKeys(true))
			newSection.set(key, oldSection.get(key));
	}

	@Override
	public void loadFromString(String contents) throws InvalidConfigurationException
	{
		if (contents == null)
			throw new IllegalArgumentException("Contents cannot be null");

		Map<?, ?> input;
		try
		{
			input = (Map<?, ?>) yaml.load(contents);
		}
		catch (YAMLException e)
		{
			throw new InvalidConfigurationException(e);
		}
		catch (ClassCastException e)
		{
			throw new InvalidConfigurationException("Top level is not a Map.");
		}

		String header = parseHeader(contents);
		if (header.length() > 0)
			options().header(header);

		if (input != null)
			convertMapsToSections(input, this);
	}

	public void move(String fromPath, String toPath)
	{
		copy(fromPath, toPath);
		set(fromPath, null);
	}

	@Override
	public YamlConfigurationOptions options()
	{
		if (options == null)
			options = new YamlConfigurationOptions(this);

		return (YamlConfigurationOptions) options;
	}

	protected String parseHeader(String input)
	{
		String[] lines = input.split("\r?\n", -1);
		StringBuilder result = new StringBuilder();
		boolean readingHeader = true;
		boolean foundHeader = false;

		for (int i = 0; i < lines.length && readingHeader; i++)
		{
			String line = lines[i];

			if (line.startsWith(COMMENT_PREFIX))
			{
				if (i > 0)
					result.append("\n");

				if (line.length() > COMMENT_PREFIX.length())
					result.append(line.substring(COMMENT_PREFIX.length()));

				foundHeader = true;
			}
			else if (foundHeader && line.length() == 0)
				result.append("\n");
			else if (foundHeader)
				readingHeader = false;
		}

		return result.toString();
	}

	@Override
	public String saveToString()
	{
		yamlOptions.setIndent(options().indent());
		yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		String header = buildHeader();
		String dump = yaml.dump(getValues(false));

		if (dump.equals(BLANK_CONFIG))
			dump = "";

		return header + dump;
	}
}
