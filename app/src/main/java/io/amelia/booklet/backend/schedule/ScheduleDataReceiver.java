package io.amelia.booklet.data.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.amelia.android.data.BoundData;
import io.amelia.android.data.DataReceiver;
import io.amelia.android.log.PLog;
import io.amelia.booklet.data.maps.ModelLocation;
import io.amelia.booklet.data.schedule.filters.ScheduleFilter;

public class ScheduleDataReceiver extends DataReceiver
{
	private static ScheduleDataReceiver instance = null;
	private static SimpleDateFormat simpleCombinedFormat = null;
	private static SimpleDateFormat simpleDateFormat = null;
	private static SimpleDateFormat simpleTimeFormat = null;

	public static ScheduleDataReceiver getInstance()
	{
		if ( instance == null )
			instance = new ScheduleDataReceiver();
		return instance;
	}

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

	public List<ModelLocation> locations;
	public TreeSet<ModelEvent> schedule;

	private ScheduleDataReceiver()
	{
		super( true );
	}

	public Set<ModelEvent> eventList()
	{
		return Collections.unmodifiableSet( schedule );
	}

	public TreeSet<ModelEvent> filterRange( ScheduleFilter filter )
	{
		TreeSet<ModelEvent> events = new TreeSet<>( new ModelEventComparator() );

		for ( ModelEvent event : schedule )
			if ( filter.filter( events, event ) )
				events.add( event );

		return events;
	}

	public List<ModelEvent> filterRangeList( ScheduleFilter filter )
	{
		return new ArrayList<>( filterRange( filter ) );
	}

	public ModelEvent getEvent( String id )
	{
		for ( ModelEvent event : schedule )
			if ( id.equals( event.id ) )
				return event;
		return null;
	}

	@Override
	public String getReferenceUri()
	{
		return "booklet-data";
	}

	public boolean hasEvent( String id )
	{
		for ( ModelEvent event : schedule )
			if ( id.equals( event.id ) )
				return true;
		return false;
	}

	public boolean hasHeartedEvents()
	{
		for ( ModelEvent event : schedule )
			if ( event.isHearted() )
				return true;
		return false;
	}

	public boolean hasTimerEvents()
	{
		for ( ModelEvent event : schedule )
			if ( event.hasTimer() )
				return true;
		return false;
	}

	@Override
	public void onDataArrived( BoundData data, boolean isRefresh )
	{
		PLog.i( "Schedule data " + getClass().getSimpleName() + " // " + data.keySet() );

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

		locations = new ArrayList<>();
		if ( data.hasBoundData( "locations" ) )
			for ( BoundData section : data.getBoundDataList( "locations" ) )
			{
				ModelLocation modelLocation = new ModelLocation();
				modelLocation.id = section.getString( "id" );
				modelLocation.title = section.getString( "title" );
				locations.add( modelLocation );
			}

		schedule = new TreeSet<>( new ModelEventComparator() );
		if ( data.hasBoundData( "schedule" ) )
			for ( BoundData section : data.getBoundDataList( "schedule" ) )
			{
				ModelEvent modelEvent = new ModelEvent();
				modelEvent.date = section.getString( "date" );
				modelEvent.description = section.getString( "description" );
				modelEvent.duration = section.getString( "duration" );
				modelEvent.id = section.getString( "id" );
				modelEvent.location = section.getString( "location" );
				modelEvent.time = section.getString( "time" );
				modelEvent.title = section.getString( "title" );
				schedule.add( modelEvent );
			}

	}

	@Override
	protected void onDataUpdate( BoundData data )
	{

	}

	public TreeSet<Date> sampleDays()
	{
		assert schedule.size() > 0;

		return new TreeSet<Date>()
		{{
			for ( ModelEvent event : schedule )
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
	public List<ModelEvent> scheduleRange( long from, long duration ) throws ParseException
	{
		List<ModelEvent> results = new ArrayList<>();

		long to = from + ( duration * 60 * 1000 );

		for ( ModelEvent event : schedule )
		{
			long start = event.getStartTime();
			long end = event.getEndTime();
			if ( start >= from && start <= to || end >= from && end <= to )
				results.add( event );
		}

		return results;
	}
}
