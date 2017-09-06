package io.amelia.booklet.data;

import io.amelia.android.files.FileBuilder;

public class GuidePageModel
{
	public String image;
	public String pageNo;
	public String title;

	public String getLocalImage()
	{
		return "guide/" + image;
	}

	public String getRemoteImage()
	{
		return FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/guide/" + image;
	}
}
