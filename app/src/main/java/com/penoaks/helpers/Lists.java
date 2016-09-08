package com.penoaks.helpers;

import com.penoaks.log.PLog;

import java.util.Set;
import java.util.TreeSet;

public class Lists
{
	private Lists()
	{
	}

	public static boolean incremented(Set<String> values)
	{
		Set<Integer> numbers = new TreeSet<>();

		int lowest = -1;

		for (String s : values)
			try
			{
				int n = Integer.parseInt(s);
				if (lowest < 0 || n < lowest)
					lowest = n;
				numbers.add(n);
			}
			catch (NumberFormatException e)
			{
				return false; // String is not a number, auto disqualified
			}

		for (int i = lowest; i < numbers.size(); i++)
		{
			if (!numbers.contains(i))
			{
				return false;
			}
		}

		return true;
	}
}
