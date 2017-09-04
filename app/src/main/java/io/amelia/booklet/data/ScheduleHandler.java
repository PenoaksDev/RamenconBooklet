package io.amelia.booklet.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.amelia.android.data.BoundData;
import io.amelia.android.log.PLog;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.filters.ScheduleFilter;

public class ScheduleHandler extends ContentHandler
{
	private static SimpleDateFormat simpleCombinedFormat = null;
	private static SimpleDateFormat simpleDateFormat = null;
	private static SimpleDateFormat simpleTimeFormat = null;

	public static SimpleDateFormat simpleCombinedFormat()
	{
		return simpleCombinedFormat;
	}

	public static SimpleDateFormat simpleDateFormat()
	{
		return simpleDateFormat;
	}

	public static SimpleDateFormat simpleTimeFormat()
	{
		return simpleTimeFormat;
	}

	public List<MapsLocationModel> locations;
	public TreeSet<ScheduleEventModel> schedule;

	public ScheduleHandler()
	{
		PLog.i( "INIT ScheduleHandler " + hashCode() );
	}

	public Set<ScheduleEventModel> eventList()
	{
		return Collections.unmodifiableSet( schedule );
	}

	public TreeSet<ScheduleEventModel> filterRange( ScheduleFilter filter )
	{
		TreeSet<ScheduleEventModel> events = new TreeSet<>( new ScheduleEventModelComparator() );

		for ( ScheduleEventModel event : schedule )
			if ( filter.filter( events, event ) )
				events.add( event );

		return events;
	}

	public List<ScheduleEventModel> filterRangeList( ScheduleFilter filter )
	{
		return new ArrayList<>( filterRange( filter ) );
	}

	public ScheduleEventModel getEvent( String id )
	{
		for ( ScheduleEventModel event : schedule )
			if ( id.equals( event.id ) )
				return event;
		return null;
	}

	@Override
	public String getSectionKey()
	{
		return "schedule";
	}

	public boolean hasEvent( String id )
	{
		for ( ScheduleEventModel event : schedule )
			if ( id.equals( event.id ) )
				return true;
		return false;
	}

	public boolean hasHeartedEvents()
	{
		for ( ScheduleEventModel event : schedule )
			if ( event.isHearted() )
				return true;
		return false;
	}

	public boolean hasTimerEvents()
	{
		for ( ScheduleEventModel event : schedule )
			if ( event.hasEventId() )
				return true;
		return false;
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		Objs.notNull( data );

		String dateFormat = "yyyy-MM-dd"; // data.getString( "dateFormat", "yyyy-MM-dd" );
		String timeFormat = "hh:mm a"; // data.getString( "timeFormat", "hh:mm a" );

		try
		{
			simpleDateFormat = new SimpleDateFormat( dateFormat );
		}
		catch ( Exception e )
		{
			PLog.e( e, "We had a problem parsing the dateFormat, value: " + dateFormat );
			simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		}

		try
		{
			simpleTimeFormat = new SimpleDateFormat( timeFormat );
		}
		catch ( Exception e )
		{
			PLog.e( e, "We had a problem parsing the dateFormat, value: " + dateFormat );
			simpleTimeFormat = new SimpleDateFormat( "hh:mm a" );
		}

		simpleCombinedFormat = new SimpleDateFormat( simpleDateFormat.toPattern() + " " + simpleTimeFormat.toPattern() );

		if ( !data.containsKey( "schedule" ) || !data.containsKey( "locations" ) )
			throw new IllegalStateException( "BoundData is missing the critical 'schedule' and 'locations' keys." );

		schedule = new TreeSet<>( new ScheduleEventModelComparator() );
		for ( BoundData section : data.getBoundDataList( "schedule" ) )
		{
			ScheduleEventModel scheduleEventModel = new ScheduleEventModel();
			scheduleEventModel.date = section.getString( "date" );
			scheduleEventModel.description = section.getString( "description" );
			scheduleEventModel.duration = section.getString( "duration" );
			scheduleEventModel.id = section.getString( "id" );
			scheduleEventModel.location = section.getString( "location" );
			scheduleEventModel.time = section.getString( "time" );
			scheduleEventModel.title = section.getString( "title" );
			schedule.add( scheduleEventModel );
		}

		locations = new ArrayList<>();
		for ( BoundData section : data.getBoundDataList( "locations" ) )
		{
			MapsLocationModel mapsLocationModel = new MapsLocationModel();
			mapsLocationModel.id = section.getString( "id" );
			mapsLocationModel.title = section.getString( "title" );
			locations.add( mapsLocationModel );
		}
	}

	public TreeSet<Date> sampleDays()
	{
		assert schedule.size() > 0;

		return new TreeSet<Date>()
		{{
			for ( ScheduleEventModel event : schedule )
			{
				try
				{
					Date day1 = simpleDateFormat().parse( event.date );
					boolean add = true;
					for ( Date day2 : this )
						if ( day2.equals( day1 ) )
							add = false;
					if ( add )
						add( day1 );
				}
				catch ( Exception ignore )
				{
				}
			}
		}};
	}

	/**
	 * Returns a range of events
	 *
	 * @param from     epoch
	 * @param duration in minutes
	 * @return List of events within range
	 * @throws ParseException
	 */
	public List<ScheduleEventModel> scheduleRange( long from, long duration ) throws ParseException
	{
		List<ScheduleEventModel> results = new ArrayList<>();

		long to = from + ( duration * 60 * 1000 );

		for ( ScheduleEventModel event : schedule )
		{
			long start = event.getStartTime();
			long end = event.getEndTime();
			if ( start >= from && start <= to || end >= from && end <= to )
				results.add( event );
		}

		return results;
	}
}
