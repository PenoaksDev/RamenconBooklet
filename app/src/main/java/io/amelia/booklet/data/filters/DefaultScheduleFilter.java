package io.amelia.booklet.data.filters;

import java.util.TreeSet;

import io.amelia.booklet.data.ScheduleEventModel;

public class DefaultScheduleFilter implements ScheduleFilter
{
	private TriStateList hearted = TriStateList.UNSET;
	private long max = -1;
	private long min = -1;
	// private TriStateList timed = TriStateList.UNSET;

	public DefaultScheduleFilter()
	{

	}

	public DefaultScheduleFilter( TriStateList hearted, long min, long max )
	{
		this.hearted = hearted;
		this.max = max;
		this.min = min;
		// this.timed = timed;
	}

	@Override
	public boolean filter( TreeSet<ScheduleEventModel> events, ScheduleEventModel event )
	{
		long epoch = event.getStartTime();

		if ( min >= 0 && epoch < min )
			return false;

		if ( max >= 0 && epoch > max )
			return false;

		if ( hearted == TriStateList.SHOW && !event.isHearted() )
			return false;

		if ( hearted == TriStateList.HIDE && event.isHearted() )
			return false;

		/*if ( timed == TriStateList.SHOW && !event.hasTimer() )
			return false;

		if ( timed == TriStateList.HIDE && event.hasTimer() )
			return false;*/

		return true;
	}

	public TriStateList getHearted()
	{
		return hearted;
	}

	public long getMax()
	{
		return max;
	}

	public long getMin()
	{
		return min;
	}

	public DefaultScheduleFilter reset()
	{
		min = -1;
		max = -1;
		hearted = TriStateList.UNSET;
		// timed = TriStateList.UNSET;

		return this;
	}

	/* public DefaultScheduleFilter setHasTimer( TriStateList timed )
	{
		this.timed = timed;
		return this;
	} */

	public DefaultScheduleFilter setHearted( TriStateList hearted )
	{
		this.hearted = hearted;
		return this;
	}

	public DefaultScheduleFilter setMax( long max )
	{
		this.max = max;
		return this;
	}

	public DefaultScheduleFilter setMin( long min )
	{
		this.min = min;
		return this;
	}

	public enum TriStateList
	{
		UNSET,
		SHOW,
		HIDE
	}
}
