package com.ramencon.data.schedule;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.penoaks.helpers.DataReceiver;
import com.ramencon.data.schedule.filters.ScheduleFilter;
import com.ramencon.data.models.ModelEvent;
import com.ramencon.data.models.ModelLocation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScheduleDataReceiver implements DataReceiver
{
	public SimpleDateFormat simpleDateFormat = null;
	public SimpleDateFormat simpleTimeFormat = null;
	public List<ModelLocation> locations;
	public Map<Long, ModelEvent> schedule;

	public ScheduleDataReceiver()
	{

	}

	@Override
	public String getReferenceUri()
	{
		return "booklet-data";
	}

	@Override
	public void onDataError(DatabaseError databaseError)
	{
		Log.i("APP", "Exception Details: (" + databaseError.getCode() + ") " + databaseError.getDetails());

		throw databaseError.toException();
	}

	@Override
	public void onDataReceived(DataSnapshot data)
	{
		String dateFormat = data.child("dateFormat").getValue(String.class);
		String timeFormat = data.child("timeFormat").getValue(String.class);

		simpleDateFormat = new SimpleDateFormat(dateFormat);
		simpleTimeFormat = new SimpleDateFormat(timeFormat);

		GenericTypeIndicator<List<ModelLocation>> tLocations = new GenericTypeIndicator<List<ModelLocation>>()
		{
		};
		GenericTypeIndicator<List<ModelEvent>> tEvents = new GenericTypeIndicator<List<ModelEvent>>()
		{
		};

		locations = data.child("locations").getValue(tLocations);
		List<ModelEvent> scheduleData = data.child("schedule").getValue(tEvents);

		final SimpleDateFormat format = new SimpleDateFormat(dateFormat + " " + timeFormat);
		schedule = new TreeMap<>();
		for (ModelEvent event : scheduleData)
		{
			event.resolveDataSnapshot();

			try
			{
				Date epoch = format.parse(event.date + " " + event.time);
				schedule.put(epoch.getTime(), event);
			}
			catch (ParseException ignore)
			{

			}
		}
	}

	@Override
	public void onDataEvent(DataEvent event, DataSnapshot dataSnapshot)
	{

	}

	public SimpleDateFormat simpleDateFormat()
	{
		return simpleDateFormat;
	}

	public SimpleDateFormat simpleTimeFormat()
	{
		return simpleTimeFormat;
	}

	public ArrayList<ModelEvent> eventList() throws ParseException
	{
		return new ArrayList<>(schedule.values());
	}

	/**
	 * Returns a range of events
	 *
	 * @param from     epoch
	 * @param duration in minutes
	 * @return List of events within range
	 * @throws ParseException
	 */
	public List<ModelEvent> scheduleRange(long from, long duration) throws ParseException
	{
		List<ModelEvent> results = new ArrayList<>();

		long to = from + (duration * 60 * 1000);

		for (Map.Entry<Long, ModelEvent> entry : schedule.entrySet())
			if (entry.getKey() >= from && entry.getKey() <= to)
				results.add(entry.getValue());

		return results;
	}

	public List<ModelEvent> filterRange(ScheduleFilter filter) throws ParseException
	{
		List<ModelEvent> dates = new ArrayList<>();

		for (Map.Entry<Long, ModelEvent> event : schedule.entrySet())
			if (filter.filter(dates, event.getKey(), event.getValue()))
				dates.add(event.getValue());

		return dates;
	}

	public List<Date> sampleDays() throws ParseException
	{
		List<Date> days = new ArrayList<>();

		for (ModelEvent event : schedule.values())
		{
			Date day1 = simpleDateFormat().parse(event.date);
			boolean add = true;
			for (Date day2 : days)
				if (day2.equals(day1))
					add = false;
			if (add)
				days.add(day1);
		}

		return days;
	}
}
