package io.amelia.booklet.data;

public enum BookletState
{
	/**
	 * The Booklet is available for download
	 */
	AVAILABLE,
	/**
	 * The Booklet is downloaded but is outdated
	 */
	OUTDATED,
	/**
	 * The Booklet is currently busy, either updating or something else
	 */
	BUSY,
	/**
	 * The Booklet had a failure, exception should be available
	 */
	FAILURE,
	/**
	 * The Booklet is ready for viewing
	 */
	READY
}
