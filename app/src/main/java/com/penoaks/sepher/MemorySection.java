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
import com.penoaks.helpers.Maps;
import com.penoaks.helpers.Objects;
import com.penoaks.log.PLog;
import com.penoaks.sepher.file.FileConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

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

	private final ConfigurationSection parent;
	private final Configuration root;
	private final String fullPath;
	private final String path;

	protected final Map<String, Object> map = new ConcurrentHashMap<>();
	protected final Set<String> changes = new HashSet<>();

	protected final List<OnConfigurationListener> listeners = new ArrayList<>();
	protected final OnConfigurationListener forwardingListener = new OnConfigurationListener()
	{
		@Override
		public void onSectionAdd(ConfigurationSection section)
		{
			for (OnConfigurationListener listener : listeners)
				listener.onSectionAdd(section);
			if (parent != null)
				parent.getForwardingListener().onSectionAdd(section);
		}

		@Override
		public void onSectionRemove(ConfigurationSection section, ConfigurationSection orphanedChild)
		{
			for (OnConfigurationListener listener : listeners)
				listener.onSectionRemove(section, orphanedChild);
			if (parent != null)
				parent.getForwardingListener().onSectionRemove(section, orphanedChild);
		}

		@Override
		public void onSectionChange(ConfigurationSection section, String affectedKey)
		{
			for (OnConfigurationListener listener : listeners)
				listener.onSectionChange(section, affectedKey);
			if (parent != null)
				parent.getForwardingListener().onSectionChange(section, affectedKey);
		}
	};

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
			ConfigurationSection subSection = section.getConfigurationSection(node, true);
			if (subSection == null)
				section = section.createSection(node);
			else
				section = subSection;
		}

		String key = path.substring(i2);
		if (section == this)
		{
			ConfigurationSection result = new MemorySection(this, key);
			synchronized (map)
			{
				// PLog.i("Creating ConfigurationSection as " + getCurrentPath() + "/" + key);
				map.put(key, result);
				changes.add(key);
				forwardingListener.onSectionAdd(result);
			}
			return result;
		}
		return section.createSection(key);
	}

	@Override
	public ConfigurationSection createSection(String path, final Collection<?> list)
	{
		return createSection(path, new HashMap<Object, Object>()
		{
			{
				Object[] a = list.toArray();
				for (int i = 0; i < a.length; i++)
					put(Integer.toString(i), a[i]);
			}
		});
	}

	@Override
	public ConfigurationSection createSection(String path, Map<?, ?> map)
	{
		ConfigurationSection section = createSection(path);

		for (Map.Entry<?, ?> entry : map.entrySet())
			if (entry.getValue() instanceof Map)
				section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
			else if (entry.getValue() instanceof Collection)
				section.createSection(entry.getKey().toString(), (Collection<?>) entry.getValue());
			else
				section.set(entry.getKey().toString(), entry.getValue());

		return section;
	}

	@Override
	public <T> List<T> getObjectList(String path, final Class<T> cls)
	{
		Object obj = get(path, null);

		if (obj == null || !(obj instanceof ConfigurationSection) && !(obj instanceof Map))
			return null;

		final ConfigurationSection values;
		if (obj instanceof Map)
		{
			values = new MemorySection(this, path);
			values.set((Map<String, Object>) obj);
		}
		else
			values = (ConfigurationSection) obj;

		return (List<T>) values.asObjectList(cls);
	}

	@Override
	public <T> T getObject(String path, final Class<T> cls)
	{
		Object obj = get(path, null);

		if (obj == null || !(obj instanceof ConfigurationSection) && !(obj instanceof Map))
			return null;

		final ConfigurationSection values;
		if (obj instanceof Map)
		{
			values = new MemorySection(this, path);
			values.set((Map<String, Object>) obj);
		}
		else
			values = (ConfigurationSection) obj;

		return values.asObject(cls);
	}

	@Override
	public <T> List<T> asObjectList(final Class<T> cls)
	{
		return new ArrayList<T>()
		{{
			if (Lists.incremented(map.keySet()))
			{
				for (Map.Entry<Integer, Object> section : Maps.asNumericallySortedMap(map).entrySet())
					if (section.getValue() instanceof ConfigurationSection)
						add(((ConfigurationSection) section.getValue()).asObject(cls));
			}
			else
				for (Map.Entry<String, Object> section : map.entrySet())
					if (section.getValue() instanceof ConfigurationSection)
						add(((ConfigurationSection) section.getValue()).asObject(cls));
		}};
	}

	@Override
	public <T> T asObject(final Class<T> cls)
	{
		try
		{
			Constructor<?> constructor = cls.getConstructor(ConfigurationSection.class);
			return (T) constructor.newInstance(this);
		}
		catch (Exception ignore)
		{
		}

		try
		{
			Constructor<?> constructor = cls.getConstructor(Map.class);
			return (T) constructor.newInstance(map);
		}
		catch (Exception ignore)
		{
		}

		try
		{
			Constructor<?> constructor = cls.getConstructor();
			Object obj = constructor.newInstance();

			final Map<String, Field> fields = new HashMap<String, Field>()
			{{
				for (Field field : cls.getFields())
					put(field.getName(), field);
			}};

			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				if (!fields.containsKey(entry.getKey()))
					PLog.w("The key " + entry.getKey() + " does not exist in class " + cls);
				else
				{
					Object value = entry.getValue();
					Field field = fields.remove(entry.getKey());
					field.setAccessible(true);

					if (value.getClass().isAssignableFrom(field.getType()))
					{
						field.set(obj, value);
					}
					else if (value instanceof ConfigurationSection)
					{
						Object newValue = Lists.incremented(((ConfigurationSection) value).getKeys()) ? ((ConfigurationSection) value).asObjectList(field.getType()) : ((ConfigurationSection) value).asObject(field.getType());
						if (newValue == null)
							PLog.e("Failed to cast ConfigurationSection " + entry.getKey() + " to object " + field.getType() + ". [" + value.getClass() + " // " + field.getType() + "]");
						else
							field.set(obj, newValue);
					}
					else if (String.class.isAssignableFrom(field.getType()))
						field.set(obj, Objects.castToString(value));
					else if (Double.class.isAssignableFrom(field.getType()))
						field.set(obj, Objects.castToDouble(value));
					else if (Integer.class.isAssignableFrom(field.getType()))
						field.set(obj, Objects.castToInt(value));
					else if (Long.class.isAssignableFrom(field.getType()))
						field.set(obj, Objects.castToLong(value));
					else if (Boolean.class.isAssignableFrom(field.getType()))
						field.set(obj, Objects.castToBool(value));
					else if (field.get(obj) == null)
						PLog.e("Failed to find a casting for field \"" + entry.getKey() + "\" with type \"" + field.getType() + "\" in object \"" + value.getClass() + "\".");
				}
			}

			for (Field field : fields.values())
				if (field.get(obj) == null && !field.isSynthetic())
					PLog.w("The field " + field.getName() + " is unassigned for object " + cls);

			return (T) obj;
		}
		catch (Exception ignore)
		{
		}

		return null;
	}

	@Override
	public Map<String, Object> getChildren()
	{
		return Collections.unmodifiableMap(map);
	}

	@Override
	public Object get(String path)
	{
		return get(path, getDefault(path));
	}

	@Override
	public Object get(String path, Object def)
	{
		return get(path, false, def);
	}

	public Object get(String path, boolean makeParents, Object def)
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
			section = section.getConfigurationSection(path.substring(i2, i1), makeParents);
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

		// PLog.i("Section Debug: " + path + " // " + (val == null ? "unknown-type" : val.getClass()) + " // " + val + " // " + create);

		if (val != null)
		{
			if (val instanceof ConfigurationSection)
				return (ConfigurationSection) val;
			else if (val instanceof Map)
			{
				ConfigurationSection section = new MemorySection(this, path);
				section.set((Map) val);
				return section;
			}
			else if (create)
				return createSection(path);
			else
			{
				PLog.w("Was asked for ConfigurationSection [" + path + "] but got " + val.getClass() + " [" + val + "]");
				return null;
			}
		}

		val = getDefault(path);
		if (val != null)
		{
			if (val instanceof ConfigurationSection)
				return (ConfigurationSection) val;
			else if (val instanceof Map)
			{
				ConfigurationSection section = new MemorySection(this, path);
				section.set((Map) val);
				return section;
			}
			else
				return null;
		}
		else if (create)
			return createSection(path);
		else
			return null;
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
		Set<String> result = new TreeSet<>();

		Configuration root = getRoot();
		if (root != null && root.options().copyDefaults())
		{
			ConfigurationSection defaults = getDefaultSection();

			if (defaults != null)
				result.addAll(defaults.getKeys(deep));
		}

		// mapChildrenKeys(result, this, deep);

		if (deep)
			result.addAll(getValues(true).keySet());
		else
			result.addAll(map.keySet());

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

		// mapChildrenValues(result, this, deep);

		for (String key : new ArrayList<>(map.keySet()))
		{
			Object obj = map.get(key);

			if (obj instanceof ConfigurationSection)
			{
				ConfigurationSection section = (ConfigurationSection) obj;
				Map<String, Object> values = section.getValues(false);

				if (Lists.incremented(values.keySet()))
					result.put(key, values.values());
				else
					result.put(key, values);
			}
			else
				result.put(key, obj);
		}

		if (deep)
			result.putAll(Maps.flattenMap(result));

		return result;
	}

	/*protected void mapChildrenValues(Map<String, Object> output, ConfigurationSection section, boolean deep)
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
	}*/

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

	/* protected void mapChildrenKeys(Set<String> output, ConfigurationSection section, boolean deep)
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
	}*/

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
			{
				Object removed = map.remove(key);
				if (removed instanceof ConfigurationSection)
					forwardingListener.onSectionRemove(this, (ConfigurationSection) removed);
			}
			else if (value instanceof Map)
				createSection(key, (Map<?, ?>) value);
			else if (value instanceof List)
				createSection(key, (List<?>) value);
			else if (map.containsKey(key) && map.get(key) instanceof ConfigurationSection && value instanceof ConfigurationSection)
				((ConfigurationSection) map.get(key)).set((ConfigurationSection) value);
			else
				map.put(key, value);

			forwardingListener.onSectionChange(this, key);
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
	public void set(ConfigurationSection section)
	{
		set(section.getValues(false));
	}

	@Override
	public void set(Map<String, Object> values)
	{
		for (String key : new ArrayList<>(values.keySet()))
			set(key, values.get(key));
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
	public List<String> getChanges()
	{
		return getChanges(true);
	}

	@Override
	public List<String> getChanges(boolean deep)
	{
		return new ArrayList<String>()
		{{
			addAll(changes);
			// TODO DEEP
		}};
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
			synchronized (map)
			{
				for (Object obj : map.values())
					if (obj instanceof ConfigurationSection && ((ConfigurationSection) obj).hasChanges(true))
						changes = true;
			}

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
			synchronized (map)
			{
				for (Object obj : map.values())
					if (obj instanceof ConfigurationSection)
						((ConfigurationSection) obj).resolveChanges(true);
			}
	}

	@Override
	public void addListener(OnConfigurationListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(OnConfigurationListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	@Override
	public OnConfigurationListener getForwardingListener()
	{
		return forwardingListener;
	}

	@Override
	public List<ConfigurationSection> getConfigurationSections()
	{
		return new ArrayList<ConfigurationSection>()
		{{
			for (Object obj : map.values())
				if (obj instanceof ConfigurationSection)
					add((ConfigurationSection) obj);
		}};
	}

	@Override
	public String toString()
	{
		Configuration root = getRoot();
		return new StringBuilder().append(getClass().getSimpleName()).append("[path='").append(getCurrentPath()).append("', root='").append(root == null ? null : root.getClass().getSimpleName()).append("']").toString();
	}
}
