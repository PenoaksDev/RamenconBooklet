package com.penoaks.booklet.events;

import com.penoaks.android.events.AbstractEvent;

public class ApplicationExceptionEvent extends AbstractEvent
{
	private Throwable cause;

	public ApplicationExceptionEvent( Throwable cause )
	{
		this.cause = cause;
	}

	public Throwable getCause()
	{
		return cause;
	}
}
