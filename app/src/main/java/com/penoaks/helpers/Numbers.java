package com.penoaks.helpers;

public class Numbers
{
	private Numbers()
	{

	}

	public static boolean isNumber(String value)
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false; // String is not a number, auto disqualified
		}
	}
}
