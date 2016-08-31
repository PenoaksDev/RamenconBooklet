package com.ramencon.cache;

import java.io.File;

public class DownloadReference
{
	public int progress = 0;
	public String sourceUrl;
	public File dest;
	public String displayName;

	public DownloadReference(String sourceUrl, File dest, String displayName )
	{
		this.sourceUrl = sourceUrl;
		this.dest = dest;
		this.displayName = displayName;
	}
}
