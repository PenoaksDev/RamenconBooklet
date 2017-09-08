package io.amelia.android.files;

interface FileFutureConsumer<FutureType extends FileFuture>
{
	/**
	 * Called once the FileRequestHandler has finished all downloads.
	 *
	 * @param request
	 * @param updateType
	 * @throws Exception
	 */
	void accept( FutureType request, FileFutureUpdateType updateType ) throws Exception;

	/**
	 * Called if there was problem
	 *
	 * @param request   The FileRequest
	 * @param exception The exception
	 */
	default void exception( FutureType request, Throwable exception )
	{
		// Do Nothing
	}

	/**
	 * Called if file exists or after successful download
	 *
	 * @param request
	 * @throws Exception
	 */
	default void process( FutureType request ) throws Exception
	{
		// Do Nothing
	}

	/**
	 * Called before FileBuilder.apply() to very the FileBuilder is all in order.
	 *
	 * @param fileBuilder The FileBuilder to very
	 * @throws Exception Throw at will for all and any verification problems
	 */
	default void verify( FileBuilder fileBuilder ) throws Exception
	{
		// Do Nothing
	}
}
