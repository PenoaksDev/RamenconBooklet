package io.amelia.booklet.data;

import io.amelia.android.files.FileBuilder;

public class MapsMapModel
{
	public String id;
	public String image;
	public String title;

	public String getLocalImage()
	{
		return "mapsMapModels/" + image;
	}

	public String getRemoteImage()
	{
		return FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/maps/" + image;
	}
}
