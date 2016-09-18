package com.ramencon.data.models;

import com.penoaks.helpers.Strings;

public class ModelGuest
{
	public String id;
	public String title;
	public String description;
	public String image;

	public String getTitle()
	{
		return Strings.fixQuotes(title);
	}

	public String getDescription()
	{
		return Strings.fixQuotes(description);
	}
}
