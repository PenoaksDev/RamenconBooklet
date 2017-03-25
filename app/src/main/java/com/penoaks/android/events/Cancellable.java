/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.android.events;

public interface Cancellable
{
	/**
	 * Gets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
	 * pass to other plugins
	 * 
	 * @return true if this event is cancelled
	 */
	boolean isCancelled();
	
	/**
	 * Sets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
	 * pass to other plugins.
	 * 
	 * @param cancel
	 *            true if you wish to cancel this event
	 */
	void setCancelled( boolean cancel );
}
