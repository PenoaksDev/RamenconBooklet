package com.penoaks.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Formatting
{
	public static String date()
	{
		return date("");
	}

	public static String date(Date date)
	{
		return date("", date, null);
	}

	public static String date(String format)
	{
		return date(format, "");
	}

	public static String date(String format, Date date)
	{
		return date(format, date, null);
	}

	public static String date(String format, Date date, String def)
	{
		return date(format, String.valueOf(date.getTime() / 1000), def);
	}

	public static String date(String format, Object data)
	{
		return date(format, data, null);
	}

	public static String date(String format, Object data, String def)
	{
		return date(format, data.toString(), def);
	}

	public static String date(String format, String data)
	{
		return date(format, data, null);
	}

	public static String date(String format, String data, String def)
	{
		Date date = new Date();

		if (data != null && !data.isEmpty())
		{
			data = data.trim();

			if (data.length() > 10)
				data = data.substring(0, 10);

			try
			{
				date = new Date(Long.parseLong(data) * 1000);
			}
			catch (NumberFormatException e)
			{
				if (def != null)
					return def;
			}
		}

		if (format == null || format.isEmpty())
			format = "MMM dx YYYY";

		if (format.contains("U"))
			format = format.replaceAll("U", date.getTime() / 1000 + "");

		if (format.contains("x"))
		{
			Calendar var1 = Calendar.getInstance();
			var1.setTime(date);
			int day = var1.get(Calendar.DAY_OF_MONTH);
			String suffix = "";

			if (day >= 11 && day <= 13)
				suffix = "'th'";
			else
				switch (day % 10)
				{
					case 1:
						suffix = "'st'";
						break;
					case 2:
						suffix = "'nd'";
						break;
					case 3:
						suffix = "'rd'";
						break;
					default:
						suffix = "'th'";
						break;
				}

			format = format.replaceAll("x", suffix);
		}

		return new SimpleDateFormat(format).format(date);
	}
}
