package io.amelia.booklet.data.schedule.filters;

import java.util.TreeSet;

import io.amelia.booklet.data.models.ModelEvent;

public interface ScheduleFilter
{
	/**
	 * Filter an event
	 *
	 * @param events A list of previously included events
	 * @param event  The event being checked
	 * @return should this event be included in results
	 */
	boolean filter( TreeSet<ModelEvent> events, ModelEvent event );
}