package io.amelia.android.files;

public interface FileFutureProgressListener<FutureType extends FileFuture>
{
	void finish( FutureType result );

	void progress( FutureType result, long bytesTransferred, long totalByteCount, boolean indeterminate );

	void start( FutureType result );
}
