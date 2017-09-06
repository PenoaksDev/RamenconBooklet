package io.amelia.android.data;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class OkHttpProgress implements Interceptor
{
	ProgressListener progressListener;

	public OkHttpProgress( ProgressListener progressListener )
	{
		this.progressListener = progressListener;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();
		Response originalResponse = chain.proceed( request );
		return originalResponse.newBuilder().body( new ProgressResponseBody( originalResponse.body(), progressListener, request.tag() ) ).build();
	}

	public interface ProgressListener
	{
		void update( Object tag, long bytesRead, long contentLength, boolean intermittent );
	}

	private static class ProgressResponseBody extends ResponseBody
	{
		private final ProgressListener progressListener;
		private final ResponseBody responseBody;
		private final Object tag;
		private BufferedSource bufferedSource;

		public ProgressResponseBody( ResponseBody responseBody, ProgressListener progressListener, Object tag )
		{
			this.responseBody = responseBody;
			this.progressListener = progressListener;
			this.tag = tag;
		}

		@Override
		public long contentLength()
		{
			return responseBody.contentLength();
		}

		@Override
		public MediaType contentType()
		{
			return responseBody.contentType();
		}

		@Override
		public BufferedSource source()
		{
			if ( bufferedSource == null )
				bufferedSource = Okio.buffer( source( responseBody.source() ) );
			return bufferedSource;
		}

		private Source source( Source source )
		{
			return new ForwardingSource( source )
			{
				long totalBytesRead = 0L;

				@Override
				public long read( Buffer sink, long byteCount ) throws IOException
				{
					long bytesRead = super.read( sink, byteCount );
					// read() returns the number of bytes read, or -1 if this source is exhausted.
					totalBytesRead += bytesRead != -1 ? bytesRead : 0;
					progressListener.update( tag, totalBytesRead, responseBody.contentLength(), responseBody.contentLength() == -1 ? true : bytesRead == -1 );
					return bytesRead;
				}
			};
		}
	}
}
