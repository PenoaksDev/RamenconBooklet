package io.amelia.android.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.amelia.android.log.PLog;
import io.amelia.android.support.LibIO;
import io.amelia.booklet.data.ContentManager;
import okhttp3.Call;

public class FileResult
{
	public byte[] bytes;
	public long bytesLength;
	public long bytesReceived;
	public Call call;
	public int code;
	public boolean isDone;
	public Exception lastException;

	public byte[] resultBytes() throws IOException
	{
		return bytes;
	}

	public InputStream resultInputStream() throws IOException
	{
		return new ByteArrayInputStream( bytes );
	}

	public String resultString() throws IOException
	{
		return new String( bytes, Charset.defaultCharset() );
	}
}
