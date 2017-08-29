package io.amelia.booklet.data.guests;

import io.amelia.android.support.Strs;

public class ModelGuest
{
	public String id;
	public String title;
	public String description;
	public String image;

	public String getTitle()
	{
		return Strs.fixQuotes( title );
	}

	public String getDescription()
	{
		return Strs.fixQuotes( description );
	}
}
