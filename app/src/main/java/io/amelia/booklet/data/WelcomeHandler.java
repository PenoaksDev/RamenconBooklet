package io.amelia.booklet.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.amelia.android.data.BoundData;

public class WelcomeHandler extends ContentHandler
{
	public String description;
	public String id;
	public String title;
	public String whenFrom;
	public Date whenFromDate;
	public String whenTo;
	public Date whenToDate;
	public String where;

	@Override
	public String getSectionKey()
	{
		return "";
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh ) throws Exception
	{
		title = data.getString( Booklet.KEY_TITLE );
		whenFrom = data.getString( Booklet.KEY_WHEN_FROM );
		whenTo = data.getString( Booklet.KEY_WHEN_TO );
		where = data.getString( Booklet.KEY_WHERE );
		description = data.getString( Booklet.KEY_DESCRIPTION );
		id = data.getString( Booklet.KEY_ID );

		SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
		whenFromDate = format.parse( whenFrom );
		whenToDate = format.parse( whenTo );
	}
}
