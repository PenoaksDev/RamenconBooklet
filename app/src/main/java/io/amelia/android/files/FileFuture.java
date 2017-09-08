package io.amelia.android.files;

public abstract class FileFuture
{
	public long bytesLength;
	public long bytesReceived;
	public FileBuilder<FileFuture> fileBuilder;
	public boolean indeterminate = false;
	protected boolean finished = false;
	private Throwable lastException;

	public Throwable getException()
	{
		return lastException;
	}

	public void setException( Throwable exception )
	{
		this.lastException = exception;
	}

	public abstract void handle( FileHandlerTask task ) throws Exception;

	public final boolean isFinished()
	{
		return finished;
	}
}
