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


class AsyncTaskDebugger
{
	private AsyncTaskDebugger next = null;
	private final int expiry;
	private final TaskRegistrar creator;
	private final Class<? extends Runnable> clazz;
	
	AsyncTaskDebugger( final int expiry, final TaskRegistrar creator, final Class<? extends Runnable> clazz )
	{
		this.expiry = expiry;
		this.creator = creator;
		this.clazz = clazz;
		
	}
	
	final AsyncTaskDebugger getNextHead( final int time )
	{
		AsyncTaskDebugger next, current = this;
		while ( time > current.expiry && ( next = current.next ) != null )
		{
			current = next;
		}
		return current;
	}
	
	final AsyncTaskDebugger setNext( final AsyncTaskDebugger next )
	{
		return this.next = next;
	}
	
	StringBuilder debugTo( final StringBuilder string )
	{
		for ( AsyncTaskDebugger next = this; next != null; next = next.next )
		{
			string.append( next.creator.getName() ).append( ':' ).append( next.clazz.getName() ).append( '@' ).append( next.expiry ).append( ',' );
		}
		return string;
	}
}
