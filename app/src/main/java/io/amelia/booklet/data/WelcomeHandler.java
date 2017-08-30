package io.amelia.booklet.data;

import io.amelia.android.data.BoundData;

public class WelcomeHandler extends ContentHandler
{
	public String description;
	public String id;
	public String image;
	public String title;
	public String whenFrom;
	public String whenTo;
	public String where;

	@Override
	public String getSectionKey()
	{
		return "";
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		image = data.getString( Booklet.KEY_IMAGE );
		title = data.getString( Booklet.KEY_TITLE );
		whenFrom = data.getString( Booklet.KEY_WHEN_FROM );
		whenTo = data.getString( Booklet.KEY_WHEN_TO );
		where = data.getString( Booklet.KEY_WHERE );
		description = data.getString( Booklet.KEY_DESCRIPTION );
		id = data.getString( Booklet.KEY_ID );
	}
}
