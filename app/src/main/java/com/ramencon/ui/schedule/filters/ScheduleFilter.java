package com.ramencon.ui.schedule.filters;

import com.ramencon.cache.skel.ModelEvent;

import java.util.List;

public interface ScheduleFilter
{
	/**
	 * Filter an event
	 *
	 * @param events    A list of previously included events
	 * @param event     The event being checked
	 *
	 * @return should this event be included in results
	 */
	boolean filter(List<ModelEvent> events, long epoch, ModelEvent event);
}
