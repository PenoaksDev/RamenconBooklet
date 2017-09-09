package io.amelia.booklet.data.filters;

import java.util.Date;
import java.util.TreeSet;

import io.amelia.android.data.BoundData;
import io.amelia.booklet.data.ScheduleEventModel;

public class DefaultScheduleFilter implements ScheduleFilter
{
	private TriStateList hearted = TriStateList.UNSET;
	// private long max = -1;
	// private long min = -1;
	private Date max = null;
	private Date min = null;
	// private TriStateList timed = TriStateList.UNSET;

	public DefaultScheduleFilter()
	{

	}

	public DefaultScheduleFilter( BoundData boundData )
	{
		hearted = DefaultScheduleFilter.TriStateList.valueOf( boundData.getString( "hearted" ) );
		Long minLong = boundData.getLong( "min", null );
		min = minLong == null || minLong < 0 ? null : new Date( minLong );
		Long maxLong = boundData.getLong( "max", null );
		max = maxLong == null || maxLong < 0 ? null : new Date( maxLong );
	}

	@Override
	public BoundData encode()
	{
		BoundData boundData = new BoundData();
		boundData.put( "class", getClass().getName() );
		boundData.put( "hearted", getHearted().name() );
		boundData.put( "min", getMinLong() );
		boundData.put( "max", getMaxLong() );
		return boundData;
	}

	@Override
	public boolean filter( TreeSet<ScheduleEventModel> events, ScheduleEventModel event )
	{
		Date startTime = event.getStartDate();
		// long epoch = event.getStartTime();

		if ( min != null && startTime.before( min ) )
			return false;

		if ( max != null && startTime.after( max ) )
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

	public DefaultScheduleFilter setHearted( TriStateList hearted )
	{
		this.hearted = hearted;
		return this;
	}

	/* public DefaultScheduleFilter setHasTimer( TriStateList timed )
	{
		this.timed = timed;
		return this;
	} */

	public Date getMax()
	{
		return max;
	}

	public DefaultScheduleFilter setMax( Date max )
	{
		this.max = max;
		return this;
	}

	public long getMaxLong()
	{
		return max == null ? -1 : max.getTime();
	}

	public DefaultScheduleFilter setMaxLong( long max )
	{
		this.max = max < 0 ? null : new Date( max );
		return this;
	}

	public Date getMin()
	{
		return min;
	}

	public DefaultScheduleFilter setMin( Date min )
	{
		this.min = min;
		return this;
	}

	public long getMinLong()
	{
		return min == null ? -1 : min.getTime();
	}

	public DefaultScheduleFilter setMinLong( long min )
	{
		this.min = min < 0 ? null : new Date( min );
		return this;
	}

	public void reset()
	{
		min = null;
		max = null;
		hearted = TriStateList.UNSET;
	}

	public enum TriStateList
	{
		UNSET,
		SHOW,
		HIDE
	}
}
