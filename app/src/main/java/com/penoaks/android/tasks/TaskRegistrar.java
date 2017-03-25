/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.android.tasks;

public interface TaskRegistrar
{
	/**
	 * Returns a value indicating whether or not this creator is currently enabled
	 * 
	 * @return true if this creator is enabled, otherwise false
	 */
	boolean isEnabled();
	
	/**
	 * Returns the name of the creator.
	 * <p>
	 * This should return the bare name of the creator and should be used for comparison.
	 * 
	 * @return name of the creator
	 */
	String getName();
}
