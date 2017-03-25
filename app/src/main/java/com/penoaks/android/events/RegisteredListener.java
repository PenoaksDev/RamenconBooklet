/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package com.penoaks.android.events;

public class RegisteredListener
{
	private final EventListener listener;
	private final EventPriority priority;
	private final EventRegistrar registrar;
	private final EventExecutor executor;
	private final boolean ignoreCancelled;

	public RegisteredListener( final EventListener listener, final EventExecutor executor, final EventPriority priority, final EventRegistrar registrar, final boolean ignoreCancelled )
	{
		this.listener = listener;
		this.priority = priority;
		this.registrar = registrar;
		this.executor = executor;
		this.ignoreCancelled = ignoreCancelled;
	}

	/**
	 * Calls the event executor
	 *
	 * @param event The event
	 * @throws EventException If an event handler throws an exception.
	 */
	public void callEvent( final AbstractEvent event ) throws EventException
	{
		if ( event instanceof Cancellable )
			if ( ( ( Cancellable ) event ).isCancelled() && isIgnoringCancelled() )
				return;

		if ( event instanceof Conditional )
			if ( priority != EventPriority.MONITOR && !( ( Conditional ) event ).conditional( this ) )
				return;

		executor.execute( listener, event );
	}

	/**
	 * Gets the plugin for this registration
	 *
	 * @return Registered Plugin
	 */
	public EventRegistrar getRegistrar()
	{
		return registrar;
	}

	/**
	 * Gets the listener for this registration
	 *
	 * @return Registered Listener
	 */
	public EventListener getListener()
	{
		return listener;
	}

	/**
	 * Gets the priority for this registration
	 *
	 * @return Registered Priority
	 */
	public EventPriority getPriority()
	{
		return priority;
	}

	/**
	 * Whether this listener accepts cancelled events
	 *
	 * @return True when ignoring cancelled events
	 */
	public boolean isIgnoringCancelled()
	{
		return ignoreCancelled;
	}
}
