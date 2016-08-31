package com.penoaks;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlWalker
{
	private XmlPullParser parser;
	private File file;

	public XmlWalker(File file)
	{
		this.file = file;
	}

	private Map<String, XmlMapping> mappings = new HashMap<>();

	public void mapTag(String parent, String child, String... values)
	{
		mappings.put(parent, new XmlMapping(parent, child, values));
	}

	public void execute() throws IOException, XmlPullParserException
	{
		parser = Xml.newPullParser();
		InputStream is = new FileInputStream(file);
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(is, null);

		int eventType = parser.getEventType();
		XmlMapping currentMapping = null;
		Map<String, String> values;

		for (XmlMapping mapping : mappings.values())
			mapping.results = new ArrayList<>();

		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (currentMapping != null)
				{
					if (currentMapping.child.equals(parser.getName()))
					{
						eventType = parser.next();
						values = new HashMap<>();
						while (eventType != XmlPullParser.END_DOCUMENT)
						{
							if (eventType == XmlPullParser.START_TAG)
							{
								String tag = parser.getName();
								String value = parser.nextText();
								values.put(tag, value);
							}
							else if (eventType == XmlPullParser.END_TAG && parser.getName().equals(currentMapping.child))
							{
								for (String tag : currentMapping.values)
									if (!values.containsKey(tag))
										throw new RuntimeException("Malformed xml on line " + parser.getLineNumber() + ", missing child tag: " + tag);
								currentMapping.results.add(values);
								break;
							}

							eventType = parser.next();
						}
					}
				}
				else if (mappings.containsKey(parser.getName()))
					currentMapping = mappings.get(parser.getName());
			}
			else if (eventType == XmlPullParser.END_TAG)
			{
				if (currentMapping != null && currentMapping.parent.equals(parser.getName()))
					currentMapping = null;
			}

			eventType = parser.next();
		}

		/* for (XmlMapping mapping : mappings.values())
		{
			Log.i("APP", "----------> Results for " + mapping.parent);
			for (Map<String, String> result : mapping.results)
			{
				for (Map.Entry<String, String> entry : result.entrySet())
					Log.i("APP", entry.getKey() + " :: " + entry.getValue());
			}
		} */
	}

	public List<Map<String, String>> getResults(String section)
	{
		return mappings.containsKey(section) ? mappings.get(section).results : null;
	}

	private class XmlMapping
	{
		String parent;
		String child;
		String[] values;
		List<Map<String, String>> results = new ArrayList<>();

		XmlMapping(String parent, String child, String... values)
		{
			this.parent = parent;
			this.child = child;
			this.values = values;
		}
	}
}
