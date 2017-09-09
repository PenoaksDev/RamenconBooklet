package io.amelia.booklet.data.filters;

import java.lang.reflect.Constructor;
import java.util.TreeSet;

import io.amelia.android.data.BoundData;
import io.amelia.booklet.data.ScheduleEventModel;

public interface ScheduleFilter
{
	static <T extends ScheduleFilter> T decode( BoundData boundData ) throws Exception
	{
		Class<T> cls = ( Class<T> ) Class.forName( boundData.getString( "class" ) );
		Constructor<T> constructor = cls.getConstructor( BoundData.class );
		return constructor.newInstance( boundData );
	}

	BoundData encode();

	/**
	 * Filter an event
	 *
	 * @param events A list of previously included events
	 * @param event  The event being checked
	 * @return should this event be included in results
	 */
	boolean filter( TreeSet<ScheduleEventModel> events, ScheduleEventModel event );

	void reset();
}
