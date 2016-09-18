package com.ramencon.data.models;

import android.app.Notification;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.helpers.Formatting;
import com.penoaks.helpers.Strings;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.AppService;
import com.ramencon.R;
import com.ramencon.data.Persistence;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.ui.HomeActivity;
import com.ramencon.ui.ScheduleFragment;

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

	public String getTitle()
	{
		return Strings.fixQuotes(title);
	}

	public String getDescription()
	{
		return Strings.fixQuotes(description);
	}

	public long getStartTime()
	{
		try
		{
			return ScheduleDataReceiver.simpleCombinedFormat().parse(date + " " + time).getTime();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage() + " Pattern: " + ScheduleDataReceiver.simpleCombinedFormat().toPattern(), e);
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

	public ModelLocation getLocation()
	{
		return ScheduleFragment.instance().getLocation(location);
	}

	public boolean hasTimer()
	{
		boolean timer = getConfigurationSection().getBoolean("timer", false);

		if (timer && !AppService.instance().hasPushNotificationPending(id))
			AppService.instance().schedulePushNotification(id, getStartTime(), getNotification());
		else if (!timer && AppService.instance().hasPushNotificationPending(id))
			AppService.instance().cancelPushNotification(id);

		return timer;
	}

	public void setTimer(boolean timer)
	{
		if (timer)
			AppService.instance().schedulePushNotification(id, getStartTime(), getNotification());
		else
			AppService.instance().cancelPushNotification(id);

		getConfigurationSection().set("timer", timer);
	}

	private Notification getNotification()
	{
		ModelLocation mLocation = getLocation();
		return new Notification.Builder(HomeActivity.instance).setContentTitle("You have an event starting soon!").setSmallIcon(R.mipmap.ic_app).setContentText(title + " starts at " + Formatting.date("h:mm a", getStartTime()) + (mLocation == null ? "" : " in " + mLocation.title)).build();
	}
}
