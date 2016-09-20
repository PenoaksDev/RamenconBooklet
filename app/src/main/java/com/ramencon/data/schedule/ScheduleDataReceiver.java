package com.ramencon.data.schedule;

import com.penoaks.log.PLog;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.data.DataReceiver;
import com.ramencon.data.models.ModelEvent;
import com.ramencon.data.models.ModelEventComparator;
import com.ramencon.data.models.ModelLocation;
import com.ramencon.data.schedule.filters.ScheduleFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ScheduleDataReceiver implements DataReceiver
{
	private static SimpleDateFormat simpleCombinedFormat = null;
	private static SimpleDateFormat simpleDateFormat = null;
	private static SimpleDateFormat simpleTimeFormat = null;
	public List<ModelLocation> locations;
	public TreeSet<ModelEvent> schedule;

	@Override
	public String getReferenceUri()
	{
		return "booklet-data";
	}

	@Override
	public void onDataReceived(ConfigurationSection data, boolean isUpdate)
	{
		if (isUpdate)
			return;

		PLog.i("Schedule data path " + data.getCurrentPath() + " // " + data.getKeys());

		String dateFormat = data.getString("dateFormat", "yyyy-MM-dd");
		String timeFormat = data.getString("timeFormat", "hh:mm a");

		try
		{
			simpleDateFormat = new SimpleDateFormat(dateFormat);
		}
		catch (Exception e)
		{
			PLog.e(e, "We had a problem parsing the dateFormat, value: " + dateFormat);
			simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		}

		try
		{
			simpleTimeFormat = new SimpleDateFormat(timeFormat);
		}
		catch (Exception e)
		{
			PLog.e(e, "We had a problem parsing the dateFormat, value: " + dateFormat);
			simpleTimeFormat = new SimpleDateFormat("hh:mm a");
		}

		simpleCombinedFormat = new SimpleDateFormat(simpleDateFormat.toPattern() + " " + simpleTimeFormat.toPattern());

		locations = new ArrayList<>();
		if (data.getConfigurationSection("locations") != null)
			for (ConfigurationSection section : data.getConfigurationSection("locations").getConfigurationSections())
				locations.add(section.asObject(ModelLocation.class));

		schedule = new TreeSet<>(new ModelEventComparator());
		if (data.getConfigurationSection("schedule") != null)
			for (ConfigurationSection section : data.getConfigurationSection("schedule").getConfigurationSections())
				schedule.add(section.asObject(ModelEvent.class));
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

	public Set<ModelEvent> eventList()
	{
		return Collections.unmodifiableSet(schedule);
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

		for (ModelEvent event : schedule)
		{
			long start = event.getStartTime();
			long end = event.getEndTime();
			if (start >= from && start <= to || end >= from && end <= to)
				results.add(event);
		}

		return results;
	}

	public List<ModelEvent> filterRangeList(ScheduleFilter filter) throws ParseException
	{
		return new ArrayList<>(filterRange(filter));
	}

	public TreeSet<ModelEvent> filterRange(ScheduleFilter filter) throws ParseException
	{
		TreeSet<ModelEvent> events = new TreeSet<>(new ModelEventComparator());

		for (ModelEvent event : schedule)
			if (filter.filter(events, event))
				events.add(event);

		return events;
	}

	public TreeSet<Date> sampleDays() throws ParseException
	{
		assert schedule.size() > 0;

		return new TreeSet<Date>()
		{{
			for (ModelEvent event : schedule)
			{
				Date day1 = simpleDateFormat().parse(event.date);
				boolean add = true;
				for (Date day2 : this)
					if (day2.equals(day1))
						add = false;
				if (add)
					add(day1);
			}
		}};
	}

	public boolean hasHeartedEvents()
	{
		for (ModelEvent event : schedule)
			if (event.isHearted())
				return true;
		return false;
	}
}
