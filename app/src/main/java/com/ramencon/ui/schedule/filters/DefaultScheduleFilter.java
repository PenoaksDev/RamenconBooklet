package com.ramencon.ui.schedule.filters;

import com.ramencon.cache.skel.ModelEvent;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

public class DefaultScheduleFilter implements ScheduleFilter
{
	private long min = -1;
	private long max = -1;

	public static JsonAdapter<DefaultScheduleFilter> getAdapter()
	{
		Moshi moshi = new Moshi.Builder().build();
		return moshi.adapter(DefaultScheduleFilter.class);
	}

	public DefaultScheduleFilter()
	{
	}

	public long getMin()
	{
		return min;
	}

	public long getMax()
	{
		return max;
	}

	public void setMin(long min)
	{
		this.min = min;
	}

	public void setMax(long max)
	{
		this.max = max;
	}

	@Override
	public boolean filter(List<ModelEvent> events, long epoch, ModelEvent event)
	{
		if (min >= 0 && epoch < min)
			return false;

		if (max >= 0 && epoch > max)
			return false;

		return true;
	}
}
