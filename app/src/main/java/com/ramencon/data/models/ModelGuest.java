package com.ramencon.data.models;

import com.penoaks.android.utils.UtilStrings;

public class ModelGuest
{
	public String id;
	public String title;
	public String description;
	public String image;

	public String getTitle()
	{
		return UtilStrings.fixQuotes( title );
	}

	public String getDescription()
	{
		return UtilStrings.fixQuotes( description );
	}
}
