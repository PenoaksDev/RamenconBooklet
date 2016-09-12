package com.penoaks.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Maps
{
	private Maps()
	{
	}

	public static Map<String, Object> flattenMap(Map<String, Object> map)
	{
		Map<String, Object> result = new HashMap<>();
		flattenMap(result, "", map);
		return result;
	}

	private static void flattenMap(Map<String, Object> result, String path, Map<String, Object> map)
	{
		for (Map.Entry<String, Object> entry : map.entrySet())
		{
			String key = path.isEmpty() ? entry.getKey() : path + "/" + entry.getKey();

			if (entry.getValue() instanceof Map)
				flattenMap(result, key, (Map<String, Object>) entry.getValue());
			else
				result.put(key, entry.getValue());
		}
	}

	/**
	 * Checks and converts the string key to an integer. Non-numeric keys are removed from the treemap.
	 *
	 * @param map The map to sort
	 * @param <T> The value type
	 * @return The sorted map as a TreeMap
	 */
	public static <T> Map<Integer, T> asNumericallySortedMap(final Map<String, T> map)
	{
		return new TreeMap<Integer, T>()
		{{
			for (Entry<String, T> entry : map.entrySet())
			{
				if (Numbers.isNumber(entry.getKey()))
					put(Integer.parseInt(entry.getKey()), entry.getValue());
			}
		}};
	}
}
