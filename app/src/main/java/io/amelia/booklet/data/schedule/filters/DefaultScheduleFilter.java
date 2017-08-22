package io.amelia.booklet.data.schedule.filters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.TreeSet;

import io.amelia.booklet.data.models.ModelEvent;

public class DefaultScheduleFilter implements ScheduleFilter
{
	public static JsonAdapter<DefaultScheduleFilter> getAdapter()
	{
		Moshi moshi = new Moshi.Builder().build();
		return moshi.adapter( DefaultScheduleFilter.class );
	}
	private long min = -1;
	private long max = -1;

	private TriStateList timed = TriStateList.UNSET;
	private TriStateList hearted = TriStateList.UNSET;

	public DefaultScheduleFilter()
	{
	}

	@Override
	public boolean filter( TreeSet<ModelEvent> events, ModelEvent event )
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

		if ( timed == TriStateList.SHOW && !event.hasTimer() )
			return false;

		if ( timed == TriStateList.HIDE && event.hasTimer() )
			return false;

		return true;
	}

	public DefaultScheduleFilter reset()
	{
		min = -1;
		max = -1;
		hearted = TriStateList.UNSET;
		timed = TriStateList.UNSET;

		return this;
	}

	public DefaultScheduleFilter setHasTimer( TriStateList timed )
	{
		this.timed = timed;
		return this;
	}

	public DefaultScheduleFilter setHearted( TriStateList hearted )
	{
		this.hearted = hearted;
		return this;
	}

	public DefaultScheduleFilter setMin( long min )
	{
		this.min = min;
		return this;
	}

	public DefaultScheduleFilter setMax( long max )
	{
		this.max = max;
		return this;
	}

	public enum TriStateList
	{
		UNSET, SHOW, HIDE
	}
}
