/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package com.penoaks.configuration;

/**
 * Exception thrown when attempting to load an invalid {@link Configuration}
 */
@SuppressWarnings( "serial" )
public class InvalidConfigurationException extends Exception
{
	/**
	 * Creates a new instance of InvalidConfigurationException without a message or cause.
	 */
	public InvalidConfigurationException()
	{
	}

	/**
	 * Constructs an instance of InvalidConfigurationException with the specified message.
	 *
	 * @param msg
	 *            The details of the exception.
	 */
	public InvalidConfigurationException( String msg )
	{
		super( msg );
	}

	/**
	 * Constructs an instance of InvalidConfigurationException with the specified cause.
	 *
	 * @param cause
	 *            The cause of the exception.
	 */
	public InvalidConfigurationException( Throwable cause )
	{
		super( cause );
	}

	/**
	 * Constructs an instance of InvalidConfigurationException with the specified message and cause.
	 *
	 * @param cause
	 *            The cause of the exception.
	 * @param msg
	 *            The details of the exception.
	 */
	public InvalidConfigurationException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
