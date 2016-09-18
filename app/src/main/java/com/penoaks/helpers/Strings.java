package com.penoaks.helpers;

public class Strings
{
	private Strings()
	{

	}

	public static String fixQuotes(String var)
	{
		try
		{
			var = var.replaceAll("\\\\\"", "\"");
			var = var.replaceAll("\\\\'", "'");

			if (var.startsWith("\"") || var.startsWith("'"))
				var = var.substring(1);
			if (var.endsWith("\"") || var.endsWith("'"))
				var = var.substring(0, var.length() - 1);
		}
		catch (Exception ignore)
		{

		}

		return var;
	}
}
