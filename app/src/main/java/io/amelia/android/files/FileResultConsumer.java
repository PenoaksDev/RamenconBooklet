package io.amelia.android.files;

interface FileResultConsumer
{
	/**
	 * Called if there was problem
	 *
	 * @param request   The FileRequest
	 * @param exception The exception
	 */
	default void exception( FileRequest request, Exception exception )
	{
		// Do Nothing
	}

	/**
	 * Called once the FileRequestHandler has finished all downloads.
	 *
	 * @param request
	 * @throws Exception
	 */
	void accept( FileRequest request ) throws Exception;

	/**
	 * Called if file exists or after successful download
	 *
	 * @param request
	 * @throws Exception
	 */
	void process( FileRequest request ) throws Exception;

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
