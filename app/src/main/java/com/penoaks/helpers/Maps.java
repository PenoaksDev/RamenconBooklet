package com.penoaks.helpers;

import java.util.HashMap;
import java.util.Map;

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
}
