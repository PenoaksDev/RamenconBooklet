package io.amelia.android.files;

public class FileRequest
{
	public boolean download;
	public boolean exists;
	public FileBuilder fileBuilder;
	public FileResult result = new FileResult();
	public FileRequestState state = FileRequestState.INIT;
	int progressAck = 0;

	public FileRequest( FileBuilder fileBuilder )
	{
		this.fileBuilder = fileBuilder;
	}
}
