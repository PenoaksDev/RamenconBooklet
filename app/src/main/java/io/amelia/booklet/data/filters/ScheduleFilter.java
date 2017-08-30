package io.amelia.booklet.data.filters;

import java.util.TreeSet;

import io.amelia.booklet.data.ScheduleEventModel;

public interface ScheduleFilter
{
	/**
	 * Filter an event
	 *
	 * @param events A list of previously included events
	 * @param event  The event being checked
	 * @return should this event be included in results
	 */
	boolean filter( TreeSet<ScheduleEventModel> events, ScheduleEventModel event );
}
