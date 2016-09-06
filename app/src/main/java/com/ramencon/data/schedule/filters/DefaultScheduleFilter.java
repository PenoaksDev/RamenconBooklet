package com.ramencon.data.schedule.filters;

import com.ramencon.models.ModelEvent;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

public class DefaultScheduleFilter implements ScheduleFilter
{
	public enum TriStateList
	{
		UNSET, SHOW, HIDE
	}

	private long min = -1;
	private long max = -1;

	private TriStateList hearted = TriStateList.UNSET;

	public static JsonAdapter<DefaultScheduleFilter> getAdapter()
	{
		Moshi moshi = new Moshi.Builder().build();
		return moshi.adapter(DefaultScheduleFilter.class);
	}

	public DefaultScheduleFilter()
	{
	}

	@Override
	public boolean filter(List<ModelEvent> events, long epoch, ModelEvent event)
	{
		if (min >= 0 && epoch < min)
			return false;

		if (max >= 0 && epoch > max)
			return false;

		if (hearted == TriStateList.SHOW && !event.isHearted())
			return false;

		if (hearted == TriStateList.HIDE && event.isHearted())
			return false;

		return true;
	}

	public void reset()
	{
		min = -1;
		max = -1;
		hearted = TriStateList.UNSET;
	}

	public void setHearted(TriStateList hearted)
	{
		this.hearted = hearted;
	}

	public void setMin(long min)
	{
		this.min = min;
	}

	public void setMax(long max)
	{
		this.max = max;
	}
}
