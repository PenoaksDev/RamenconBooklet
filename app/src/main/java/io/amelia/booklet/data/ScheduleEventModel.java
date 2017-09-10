package io.amelia.booklet.data;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import io.amelia.android.data.BoundData;
import io.amelia.android.support.Strs;
import io.amelia.booklet.ui.fragment.ScheduleFragment;

public class ScheduleEventModel implements Comparable<ScheduleEventModel>
{
	public String date;
	public String description;
	public String duration;
	public String id;
	public String location;
	public String time;
	public String title;

	@Override
	public int compareTo( @NonNull ScheduleEventModel scheduleEventModel )
	{
		int comp = Long.compare( getStartTime(), scheduleEventModel.getStartTime() );
		comp = comp == 0 ? Strs.compare( location, scheduleEventModel.location ) : comp;
		return comp == 0 ? Long.compare( getEndTime(), scheduleEventModel.getEndTime() ) : comp;
	}

	public String getDescription()
	{
		return Strs.fixQuotes( description );
	}

	public int getDuration()
	{
		return Integer.parseInt( duration );
	}

	public Date getEndDate()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( getStartDate() );
		calendar.add( Calendar.MILLISECOND, getDuration() * 60 * 1000 );
		return calendar.getTime();
	}

	public long getEndTime()
	{
		return getStartTime() + ( getDuration() * 60 * 1000 );
	}

	public long getEventId()
	{
		BoundData userData = ContentManager.getUserData();
		String key = "booklet-" + ContentManager.getActiveBooklet().getId();

		if ( !userData.hasBoundData( key ) )
			return -1;

		return userData.getBoundData( key ).getLong( id + "-event", -1L );
	}

	public void setEventId( long eventId )
	{
		BoundData userData = ContentManager.getUserData();
		String key = "booklet-" + ContentManager.getActiveBooklet().getId();

		if ( !userData.hasBoundData( key ) )
			userData.put( key, new BoundData() );

		if ( eventId == -1 )
			userData.getBoundData( key ).remove( id + "-event" );
		else
			userData.getBoundData( key ).put( id + "-event", eventId );
	}

	public MapsLocationModel getLocation()
	{
		return ScheduleFragment.instance().getLocation( location );
	}

	public Date getStartDate()
	{
		try
		{
			return ScheduleHandler.simpleCombinedFormat().parse( date + " " + time );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e.getMessage() + " Pattern: " + ScheduleHandler.simpleCombinedFormat().toPattern(), e );
		}
	}

	public long getStartTime()
	{
		return getStartDate().getTime();
	}

	public String getTitle()
	{
		return Strs.fixQuotes( title );
	}

	public boolean hasEventId()
	{
		long eventId = getEventId();
		if ( eventId > -1 )
		{
			Uri queryUri = ContentUris.withAppendedId( CalendarContract.Events.CONTENT_URI, eventId );
			Cursor cr = ContentManager.getActivity().getContentResolver().query( queryUri, null, null, null, null );
			return cr.getCount() > 0;
		}
		return false;
	}

	public boolean hasEventPassed()
	{
		return getStartTime() - new Date().getTime() < 0;
	}

	public boolean isHearted()
	{
		BoundData userData = ContentManager.getUserData();
		String key = "booklet-" + ContentManager.getActiveBooklet().getId();

		if ( !userData.hasBoundData( key ) )
			return false;

		return userData.getBoundData( key ).getBoolean( id + "-hearted", false );
	}

	public void setHearted( boolean hearted )
	{
		BoundData userData = ContentManager.getUserData();
		String key = "booklet-" + ContentManager.getActiveBooklet().getId();

		if ( !userData.hasBoundData( key ) )
			userData.put( key, new BoundData() );

		userData.getBoundData( key ).put( id + "-hearted", hearted );
	}
}
