package com.ramencon.ui.schedule;

import com.penoaks.Files;
import com.ramencon.cache.skel.ModelEvent;
import com.ramencon.cache.skel.ModelSchedule;
import com.ramencon.ui.schedule.filters.ScheduleFilter;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScheduleManager
{
	public SimpleDateFormat simpleDateFormat = null;
	public SimpleDateFormat simpleTimeFormat = null;

	public ModelSchedule model;
	private Map<Long, ModelEvent> events;

	public ScheduleManager(File file) throws IOException, ParseException
	{
		Moshi moshi = new Moshi.Builder().build();
		JsonAdapter<ModelSchedule> scheduleAdapater = moshi.adapter(ModelSchedule.class);

		String source = Files.getStringFromFile(file);
		model = scheduleAdapater.fromJson(source);

		events = new TreeMap<>();
		SimpleDateFormat format = new SimpleDateFormat(model.dateFormat + " " + model.timeFormat);
		for (ModelEvent event : model.schedule)
		{
			Date epoch = format.parse(event.date + " " + event.time);
			events.put(epoch.getTime(), event);
		}
	}

	public SimpleDateFormat simpleDateFormat()
	{
		if (simpleDateFormat == null)
			simpleDateFormat = new SimpleDateFormat(model.dateFormat);
		return simpleDateFormat;
	}

	public SimpleDateFormat simpleTimeFormat()
	{
		if (simpleTimeFormat == null)
			simpleTimeFormat = new SimpleDateFormat(model.timeFormat);
		return simpleTimeFormat;
	}

	public ArrayList<ModelEvent> eventList() throws ParseException
	{
		return new ArrayList<>(events.values());
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

		for (Map.Entry<Long, ModelEvent> entry : events.entrySet())
			if (entry.getKey() >= from && entry.getKey() <= to)
				results.add(entry.getValue());

		return results;
	}

	public List<ModelEvent> filterRange(ScheduleFilter filter) throws ParseException
	{
		List<ModelEvent> dates = new ArrayList<>();

		for (Map.Entry<Long, ModelEvent> event : events.entrySet())
			if (filter.filter(dates, event.getKey(), event.getValue()))
				dates.add(event.getValue());

		return dates;
	}

	public List<Date> sampleDays() throws ParseException
	{
		List<Date> days = new ArrayList<>();

		for (ModelEvent event : events.values())
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
