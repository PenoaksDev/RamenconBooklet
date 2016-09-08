/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * <p/>
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.penoaks.sepher;

import com.penoaks.helpers.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO Fix the number type casting issues

/**
 * A type of {@link ConfigurationSection} that is stored in memory.
 */
public class MemorySection implements ConfigurationSection
{
	/**
	 * Creates a full path to the given {@link ConfigurationSection} from its root {@link Configuration}.
	 * <p/>
	 * You may use this method for any given {@link ConfigurationSection}, not only {@link MemorySection}.
	 *
	 * @param section Section to create a path for.
	 * @param key     Name of the specified section.
	 * @return Full path of the section from its root.
	 */
	public static String createPath(ConfigurationSection section, String key)
	{
		return createPath(section, key, section == null ? null : section.getRoot());
	}

	/**
	 * Creates a relative path to the given {@link ConfigurationSection} from the given relative section.
	 * <p/>
	 * You may use this method for any given {@link ConfigurationSection}, not only {@link MemorySection}.
	 *
	 * @param section    Section to create a path for.
	 * @param key        Name of the specified section.
	 * @param relativeTo Section to create the path relative to.
	 * @return Full path of the section from its root.
	 */
	public static String createPath(ConfigurationSection section, String key, ConfigurationSection relativeTo)
	{
		if (section == null)
			throw new IllegalArgumentException("Cannot create path without a section");

		Configuration root = section.getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot create path without a root");
		char separator = root.options().pathSeparator();

		StringBuilder builder = new StringBuilder();
		if (section != null)
			for (ConfigurationSection parent = section; parent != null && parent != relativeTo; parent = parent.getParent())
			{
				if (builder.length() > 0)
					builder.insert(0, separator);

				builder.insert(0, parent.getName());
			}

		if (key != null && key.length() > 0)
		{
			if (builder.length() > 0)
				builder.append(separator);

			builder.append(key);
		}

		return builder.toString();
	}

	protected final Map<String, Object> map = new LinkedHashMap<>();
	protected final Set<String> changes = new HashSet<>();

	private final ConfigurationSection parent;
	private final Configuration root;
	private final String fullPath;
	private final String path;

	/**
	 * Creates an empty MemorySection for use as a root {@link Configuration} section.
	 * <p/>
	 * Note that calling this without being yourself a {@link Configuration} will throw an exception!
	 *
	 * @throws IllegalStateException Thrown if this is not a {@link Configuration} root.
	 */
	protected MemorySection()
	{
		if (!(this instanceof Configuration))
			throw new IllegalStateException("You must implement Configuration when using MemorySection as a root node");

		path = "";
		fullPath = "";
		parent = null;
		root = (Configuration) this;
	}

	/**
	 * Creates an empty MemorySection with the specified parent and path.
	 *
	 * @param parent Parent section that contains this own section.
	 * @param path   Path that you may access this section from via the root {@link Configuration}.
	 * @throws IllegalArgumentException Thrown is parent or path is null, or if parent contains no root Configuration.
	 */
	protected MemorySection(ConfigurationSection parent, String path)
	{
		if (parent == null)
			throw new IllegalArgumentException("Parent cannot be null");
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null");

		this.path = path;
		this.parent = parent;
		root = parent.getRoot();

		if (root == null)
			throw new IllegalArgumentException("Path cannot be orphaned");

		fullPath = createPath(parent, path);
	}

	@Override
	public void addDefault(String path, Object value)
	{
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null");

		Configuration root = getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot add default without root");
		if (root == this)
			throw new UnsupportedOperationException("Unsupported addDefault(String, Object) implementation");
		root.addDefault(createPath(this, path), value);
	}

	@Override
	public boolean contains(String path)
	{
		return get(path) != null;
	}

	@Override
	public ConfigurationSection createSection(String path)
	{
		if (path == null)
			throw new IllegalArgumentException("Cannot create section at empty path");

		Configuration root = getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot create section without a root");

		final char separator = root.options().pathSeparator();
		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		ConfigurationSection section = this;
		while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1)
		{
			String node = path.substring(i2, i1);
			ConfigurationSection subSection = section.getConfigurationSection(node);
			if (subSection == null)
				section = section.createSection(node);
			else
				section = subSection;
		}

		String key = path.substring(i2);
		if (section == this)
		{
			ConfigurationSection result = new MemorySection(this, key);
			map.put(key, result);
			changes.add(key);
			return result;
		}
		return section.createSection(key);
	}

	@Override
	public ConfigurationSection createSection(String path, Map<?, ?> map)
	{
		ConfigurationSection section = createSection(path);

		for (Map.Entry<?, ?> entry : map.entrySet())
			if (entry.getValue() instanceof Map)
				section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
			else
				section.set(entry.getKey().toString(), entry.getValue());

		return section;
	}

	@Override
	public Object get(String path)
	{
		return get(path, getDefault(path));
	}

	@Override
	public Object get(String path, Object def)
	{
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null");

		if (path.length() == 0)
			return this;

		Configuration root = getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot access section without a root");

		final char separator = root.options().pathSeparator();
		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		ConfigurationSection section = this;
		while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1)
		{
			section = section.getConfigurationSection(path.substring(i2, i1));
			if (section == null)
				return def;
		}

		String key = path.substring(i2);
		if (section == this)
		{
			Object result = map.get(key);
			return result == null ? def : result;
		}
		return section.get(key, def);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getAsList(String path)
	{
		List<T> def;
		try
		{
			def = (List<T>) getDefault(path);
		}
		catch (ClassCastException e)
		{
			def = null;
		}
		return getAsList(path, def);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getAsList(String path, List<T> def)
	{
		final Object val = get(path, def);
		try
		{
			if (val == null)
				return def;
			else if (!(val instanceof List))
				return new ArrayList<T>()
				{
					{
						add((T) val);
					}
				};
			else
				return (List<T>) val;
		}
		catch (ClassCastException e)
		{
			return def;
		}
	}

	@Override
	public boolean getBoolean(String path)
	{
		Object def = getDefault(path);
		return getBoolean(path, def instanceof Boolean ? (Boolean) def : false);
	}

	@Override
	public boolean getBoolean(String path, boolean def)
	{
		Object val = get(path, def);
		return val instanceof Boolean ? (Boolean) val : def;
	}

	@Override
	public List<Boolean> getBooleanList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Boolean>(0);

		List<Boolean> result = new ArrayList<Boolean>();

		for (Object object : list)
			if (object instanceof Boolean)
				result.add((Boolean) object);
			else if (object instanceof String)
				if (Boolean.TRUE.toString().equals(object))
					result.add(true);
				else if (Boolean.FALSE.toString().equals(object))
					result.add(false);

		return result;
	}

	@Override
	public List<Byte> getByteList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Byte>(0);

		List<Byte> result = new ArrayList<Byte>();

		for (Object object : list)
			if (object instanceof Byte)
				result.add((Byte) object);
			else if (object instanceof String)
				try
				{
					result.add(Byte.valueOf((String) object));
				}
				catch (Exception ex)
				{

				}
			else if (object instanceof Character)
				result.add((byte) ((Character) object).charValue());
			else if (object instanceof Number)
				result.add(((Number) object).byteValue());

		return result;
	}

	public List<Character> getCharacterList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Character>(0);

		List<Character> result = new ArrayList<Character>();

		for (Object object : list)
			if (object instanceof Character)
				result.add((Character) object);
			else if (object instanceof String)
			{
				String str = (String) object;

				if (str.length() == 1)
					result.add(str.charAt(0));
			}
			else if (object instanceof Number)
				result.add((char) ((Number) object).intValue());

		return result;
	}

	/*@Override
	public Color getColor(String path)
	{
		Object def = getDefault(path);
		return getColor(path, def instanceof Color ? (Color) def : null);
	}

	@Override
	public Color getColor(String path, Color def)
	{
		Object val = get(path, def);
		return val instanceof Color ? (Color) val : def;
	}*/

	@Override
	public ConfigurationSection getConfigurationSection(String path)
	{
		return getConfigurationSection(path, false);
	}

	@Override
	public ConfigurationSection getConfigurationSection(String path, boolean create)
	{
		Object val = get(path, null);
		if (val != null)
			return val instanceof ConfigurationSection ? (ConfigurationSection) val : null;

		val = get(path, getDefault(path));
		return val instanceof ConfigurationSection || create ? createSection(path) : null;
	}

	@Override
	public String getCurrentPath()
	{
		return fullPath;
	}

	protected Object getDefault(String path)
	{
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null");

		Configuration root = getRoot();
		Configuration defaults = root == null ? null : root.getDefaults();
		return defaults == null ? null : defaults.get(createPath(this, path));
	}

	@Override
	public ConfigurationSection getDefaultSection()
	{
		Configuration root = getRoot();
		Configuration defaults = root == null ? null : root.getDefaults();

		if (defaults != null)
			if (defaults.isConfigurationSection(getCurrentPath()))
				return defaults.getConfigurationSection(getCurrentPath());

		return null;
	}

	@Override
	public double getDouble(String path)
	{
		Object def = getDefault(path);
		return getDouble(path, def instanceof Number ? toDouble(def) : 0);
	}

	@Override
	public double getDouble(String path, double def)
	{
		Object val = get(path, def);
		return val instanceof Number || val instanceof Integer ? toDouble(val) : def;
	}

	@Override
	public List<Double> getDoubleList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Double>(0);

		List<Double> result = new ArrayList<Double>();

		for (Object object : list)
			if (object instanceof Double)
				result.add((Double) object);
			else if (object instanceof String)
				try
				{
					result.add(Double.valueOf((String) object));
				}
				catch (Exception ex)
				{

				}
			else if (object instanceof Character)
				result.add((double) ((Character) object).charValue());
			else if (object instanceof Number)
				result.add(((Number) object).doubleValue());

		return result;
	}

	@Override
	public List<Float> getFloatList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Float>(0);

		List<Float> result = new ArrayList<Float>();

		for (Object object : list)
			if (object instanceof Float)
				result.add((Float) object);
			else if (object instanceof String)
				try
				{
					result.add(Float.valueOf((String) object));
				}
				catch (Exception ex)
				{

				}
			else if (object instanceof Character)
				result.add((float) ((Character) object).charValue());
			else if (object instanceof Number)
				result.add(((Number) object).floatValue());

		return result;
	}

	@Override
	public int getInt(String path)
	{
		Object def = getDefault(path);
		return getInt(path, def instanceof Number ? toInt(def) : 0);
	}

	@Override
	public int getInt(String path, int def)
	{
		Object val = get(path, def);
		return val instanceof Integer ? toInt(val) : def;
	}

	@Override
	public List<Integer> getIntegerList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Integer>(0);

		List<Integer> result = new ArrayList<Integer>();

		for (Object object : list)
			if (object instanceof Integer)
				result.add((Integer) object);
			else if (object instanceof String)
				try
				{
					result.add(Integer.valueOf((String) object));
				}
				catch (Exception ex)
				{

				}
			else if (object instanceof Character)
				result.add((int) ((Character) object).charValue());
			else if (object instanceof Number)
				result.add(((Number) object).intValue());

		return result;
	}

	@Override
	public Set<String> getKeys()
	{
		return getKeys(false);
	}

	@Override
	public Set<String> getKeys(boolean deep)
	{
		Set<String> result = new LinkedHashSet<String>();

		Configuration root = getRoot();
		if (root != null && root.options().copyDefaults())
		{
			ConfigurationSection defaults = getDefaultSection();

			if (defaults != null)
				result.addAll(defaults.getKeys(deep));
		}

		mapChildrenKeys(result, this, deep);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getList(String path)
	{
		List<T> def;
		try
		{
			def = (List<T>) getDefault(path);
		}
		catch (ClassCastException e)
		{
			def = null;
		}
		return getList(path, def);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getList(String path, List<T> def)
	{
		Object val = get(path, def);
		try
		{
			return val == null ? def : (List<T>) val;
		}
		catch (ClassCastException e)
		{
			return def;
		}
	}

	@Override
	public long getLong(String path)
	{
		Object def = getDefault(path);
		return getLong(path, def instanceof Number || def instanceof Integer ? toLong(def) : 0);
	}

	@Override
	public long getLong(String path, long def)
	{
		Object val = get(path, def);
		return val instanceof Number || val instanceof Integer ? toLong(val) : def;
	}

	@Override
	public List<Long> getLongList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Long>(0);

		List<Long> result = new ArrayList<Long>();

		for (Object object : list)
			if (object instanceof Long)
				result.add((Long) object);
			else if (object instanceof String)
				try
				{
					result.add(Long.valueOf((String) object));
				}
				catch (Exception ex)
				{

				}
			else if (object instanceof Character)
				result.add((long) ((Character) object).charValue());
			else if (object instanceof Number)
				result.add(((Number) object).longValue());

		return result;
	}

	@Override
	public List<Map<?, ?>> getMapList(String path)
	{
		List<?> list = getList(path);
		List<Map<?, ?>> result = new ArrayList<Map<?, ?>>();

		if (list == null)
			return result;

		for (Object object : list)
			if (object instanceof Map)
				result.add((Map<?, ?>) object);

		return result;
	}

	@Override
	public String getName()
	{
		return path;
	}

	@Override
	public ConfigurationSection getParent()
	{
		return parent;
	}

	@Override
	public Configuration getRoot()
	{
		return root;
	}

	@Override
	public List<Short> getShortList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<Short>(0);

		List<Short> result = new ArrayList<Short>();

		for (Object object : list)
			if (object instanceof Short)
				result.add((Short) object);
			else if (object instanceof String)
				try
				{
					result.add(Short.valueOf((String) object));
				}
				catch (Exception ex)
				{

				}
			else if (object instanceof Character)
				result.add((short) ((Character) object).charValue());
			else if (object instanceof Number)
				result.add(((Number) object).shortValue());

		return result;
	}

	// Primitives
	@Override
	public String getString(String path)
	{
		Object def = getDefault(path);
		return getString(path, def != null ? def.toString() : null);
	}

	@Override
	public String getString(String path, String def)
	{
		Object val = get(path, def);
		return val != null ? val.toString() : def;
	}

	@Override
	public List<String> getStringList(String path)
	{
		List<?> list = getList(path);

		if (list == null)
			return new ArrayList<String>(0);

		List<String> result = new ArrayList<String>();

		for (Object object : list)
			if (object instanceof String || isPrimitiveWrapper(object))
				result.add(String.valueOf(object));

		return result;
	}

	@Override
	public List<String> getStringList(String path, List<String> def)
	{
		List<?> list = getList(path);

		if (list == null)
			return def;

		List<String> result = new ArrayList<String>();

		for (Object object : list)
			if (object instanceof String || isPrimitiveWrapper(object))
				result.add(String.valueOf(object));

		return result;
	}

	@Override
	public Map<String, Object> getValues(boolean deep)
	{
		Map<String, Object> result = new LinkedHashMap<>();

		Configuration root = getRoot();
		if (root != null && root.options().copyDefaults())
		{
			ConfigurationSection defaults = getDefaultSection();

			if (defaults != null)
				result.putAll(defaults.getValues(deep));
		}

		mapChildrenValues(result, this, deep);

		return result;
	}

	@Override
	public boolean has(String path)
	{
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null");

		if (path.length() == 0)
			return true;

		Configuration root = getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot access section without a root");

		final char separator = root.options().pathSeparator();
		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		ConfigurationSection section = this;
		while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1)
		{
			section = section.getConfigurationSection(path.substring(i2, i1));
			if (section == null)
				return false;
		}

		String key = path.substring(i2);
		if (section == this)
		{
			Object result = map.get(key);
			return result != null;
		}
		return section.has(key);
	}

	@Override
	public boolean isBoolean(String path)
	{
		Object val = get(path);
		return val instanceof Boolean;
	}

	/* @Override
	public boolean isColor(String path)
	{
		Object val = get(path);
		return val instanceof Color;
	}*/

	@Override
	public boolean isConfigurationSection(String path)
	{
		Object val = get(path);
		return val instanceof ConfigurationSection;
	}

	@Override
	public boolean isDouble(String path)
	{
		Object val = get(path);
		return val instanceof Double;
	}

	@Override
	public boolean isInt(String path)
	{
		Object val = get(path);
		return val instanceof Integer;
	}

	@Override
	public boolean isList(String path)
	{
		Object val = get(path);
		return val instanceof List;
	}

	@Override
	public boolean isLong(String path)
	{
		Object val = get(path);
		return val instanceof Long;
	}

	protected boolean isPrimitiveWrapper(Object input)
	{
		return input instanceof Integer || input instanceof Boolean || input instanceof Character || input instanceof Byte || input instanceof Short || input instanceof Double || input instanceof Long || input instanceof Float;
	}

	@Override
	public boolean isSet(String path)
	{
		Configuration root = getRoot();
		if (root == null)
			return false;
		if (root.options().copyDefaults())
			return contains(path);
		return get(path, null) != null;
	}

	@Override
	public boolean isString(String path)
	{
		Object val = get(path);
		return val instanceof String;
	}

	protected void mapChildrenKeys(Set<String> output, ConfigurationSection section, boolean deep)
	{
		if (section instanceof MemorySection)
		{
			MemorySection sec = (MemorySection) section;

			for (Map.Entry<String, Object> entry : sec.map.entrySet())
			{
				output.add(createPath(section, entry.getKey(), this));

				if (deep && entry.getValue() instanceof ConfigurationSection)
				{
					ConfigurationSection subsection = (ConfigurationSection) entry.getValue();
					mapChildrenKeys(output, subsection, deep);
				}
			}
		}
		else
		{
			Set<String> keys = section.getKeys(deep);

			for (String key : keys)
				output.add(createPath(section, key, this));
		}
	}

	protected void mapChildrenValues(Map<String, Object> output, ConfigurationSection section, boolean deep)
	{
		if (section instanceof MemorySection)
		{
			MemorySection sec = (MemorySection) section;

			for (Map.Entry<String, Object> entry : sec.map.entrySet())
			{
				if (entry.getValue() instanceof ConfigurationSection)
				{
					Map<String, Object> result = ((ConfigurationSection) entry.getValue()).getValues(false);
					if (Lists.incremented(result.keySet()))
						output.put(createPath(section, entry.getKey(), this), result.values());
					else
						output.put(createPath(section, entry.getKey(), this), result);
				}
				else
					output.put(createPath(section, entry.getKey(), this), entry.getValue());

				if (entry.getValue() instanceof ConfigurationSection && deep)
					mapChildrenValues(output, (ConfigurationSection) entry.getValue(), deep);
			}
		}
		else
		{
			Map<String, Object> values = section.getValues(deep);

			for (Map.Entry<String, Object> entry : values.entrySet())
				output.put(createPath(section, entry.getKey(), this), entry.getValue());
		}
	}

	@Override
	public void set(String path, Object value)
	{
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null");

		Configuration root = getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot use section without a root");

		final char separator = root.options().pathSeparator();
		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		ConfigurationSection section = this;
		while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1)
		{
			String node = path.substring(i2, i1);
			ConfigurationSection subSection = section.getConfigurationSection(node);
			if (subSection == null)
				section = section.createSection(node);
			else
				section = subSection;
		}

		String key = path.substring(i2);
		if (section == this)
		{
			if (value == null)
				map.remove(key);
			else if (map.containsKey(key) && map.get(key) instanceof ConfigurationSection && value instanceof ConfigurationSection)
				((ConfigurationSection) map.get(key)).overwrite((ConfigurationSection) value);
			else
				map.put(key, value);

			changes.add(key);
		}
		else
			section.set(key, value);
	}

	@Override
	public void merge(ConfigurationSection section)
	{
		for (Map.Entry<String, Object> entry : (section instanceof MemorySection ? ((MemorySection) section).map : section.getValues(false)).entrySet())
			if (!has(entry.getKey()))
				set(entry.getKey(), entry.getValue());
	}

	@Override
	public void overwrite(ConfigurationSection section)
	{
		for (Map.Entry<String, Object> entry : (section instanceof MemorySection ? ((MemorySection) section).map : section.getValues(false)).entrySet())
			set(entry.getKey(), entry.getValue());
	}

	private double toDouble(Object def)
	{
		if (def instanceof Double)
			return (double) def;

		try
		{
			return Double.parseDouble((String) def);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	private int toInt(Object val)
	{
		if (val instanceof Integer)
			return (int) val;

		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	private long toLong(Object def)
	{
		try
		{
			return Long.parseLong("" + def);
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	@Override
	public boolean hasChanges()
	{
		return hasChanges(true);
	}

	@Override
	public boolean hasChanges(boolean deep)
	{
		if (changes.size() > 0)
			return true;

		boolean changes = false;

		if (deep)
			for (Object obj : map.values())
				if (obj instanceof ConfigurationSection && ((ConfigurationSection) obj).hasChanges(true))
					changes = true;

		return changes;
	}

	@Override
	public void resolveChanges()
	{
		resolveChanges(true);
	}

	@Override
	public void resolveChanges(boolean deep)
	{
		changes.clear();
		if (deep)
			for (Object obj : map.values())
				if (obj instanceof ConfigurationSection)
					((ConfigurationSection) obj).resolveChanges(true);
	}

	@Override
	public String toString()
	{
		Configuration root = getRoot();
		return new StringBuilder().append(getClass().getSimpleName()).append("[path='").append(getCurrentPath()).append("', root='").append(root == null ? null : root.getClass().getSimpleName()).append("']").toString();
	}
}
