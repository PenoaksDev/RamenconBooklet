package com.ramencon.data.models;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.data.Persistence;
import com.penoaks.log.PLog;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.ui.HomeActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ModelEvent
{
	public String id;
	public String title;
	public String date;
	public String time;
	public String duration;
	public String location;
	public String description;

	private ConfigurationSection getConfigurationSection()
	{
		return Persistence.getInstance().get("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/events/" + id);
	}

	public boolean isHearted()
	{
		return getConfigurationSection().getBoolean("hearted", false);
	}

	public void setHearted(boolean hearted)
	{
		getConfigurationSection().set("hearted", hearted);
	}

	public long getStartTime()
	{
		try
		{
			return ScheduleDataReceiver.simpleCombinedFormat().parse(date + " " + time).getTime();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public long getEndTime()
	{
		return getStartTime() + (getDuration() * 60);
	}

	public int getDuration()
	{
		return Integer.parseInt(duration);
	}
}
