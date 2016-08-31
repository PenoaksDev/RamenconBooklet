package com.ramencon.cache;

public class DownloadResult
{
	public boolean errored;
	public String message;
	public Exception exception;

	public DownloadResult(Exception exception)
	{
		this.exception = exception;
	}

	public DownloadResult(boolean errored, String message)
	{
		this.errored = errored;
		this.message = message;
	}
}
