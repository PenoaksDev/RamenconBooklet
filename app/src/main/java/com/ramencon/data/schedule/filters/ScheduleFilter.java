package com.ramencon.data.schedule.filters;

import com.ramencon.data.models.ModelEvent;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface ScheduleFilter
{
	/**
	 * Filter an event
	 *
	 * @param events A list of previously included events
	 * @param event  The event being checked
	 * @return should this event be included in results
	 */
	boolean filter(TreeSet<ModelEvent> events, ModelEvent event);
}
