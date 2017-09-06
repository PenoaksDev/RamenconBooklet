package io.amelia.android.files;

import android.support.annotation.NonNull;

import io.amelia.android.support.DateAndTime;

public class FileRequest implements Comparable<FileRequest>
{
	public boolean download;
	public boolean exists;
	public FileBuilder fileBuilder;
	public FileResult result = new FileResult();
	public FileRequestState state = FileRequestState.INIT;
	public long timestamp = DateAndTime.epoch();

	public FileRequest( FileBuilder fileBuilder )
	{
		this.fileBuilder = fileBuilder;
	}

	@Override
	public int compareTo( @NonNull FileRequest request )
	{
		return Long.compare( timestamp, request.timestamp );
	}
}
