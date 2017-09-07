package io.amelia.booklet.data;

import io.amelia.android.support.Objs;
import io.amelia.android.support.Strs;

public class VendorsModel
{
	public String description;
	public String id;
	public String image;
	public String title;
	public String urls;

	public String getDescription()
	{
		return Strs.fixQuotes( description ).replaceAll( "\r", "" ).replaceAll( "\n", "<br>" );
	}

	public String getFormattedUrls()
	{
		if ( Objs.isEmpty( urls ) )
			return "";

		StringBuilder sb = new StringBuilder();

		String[] lines = urls.split( "\n\r?" );
		for ( String line : lines )
		{
			line = line.trim();
			if ( !Objs.isEmpty( line ) )
				sb.append( "<a href=\"" + line + "\">" + line + "</a><br>" );
		}

		return sb.toString();
	}

	public String getTitle()
	{
		return Strs.fixQuotes( title );
	}
}
