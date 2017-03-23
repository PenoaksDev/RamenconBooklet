package com.ramencon.data.models;

import android.app.Notification;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.android.utils.UtilDate;
import com.penoaks.android.utils.UtilStrings;
import com.penoaks.configuration.ConfigurationSection;
import com.penoaks.android.log.PLog;
import com.ramencon.R;
import com.penoaks.booklet.DataPersistence;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.system.AppService;
import com.ramencon.ui.HomeActivity;
import com.ramencon.ui.ScheduleFragment;

import java.util.Date;

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
		return DataPersistence.getInstance().config( "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/events/" + id );
	}

	public boolean isHearted()
	{
		return getConfigurationSection().getBoolean( "hearted", false );
	}

	public void setHearted( boolean hearted )
	{
		getConfigurationSection().set( "hearted", hearted );
	}

	public String getTitle()
	{
		return UtilStrings.fixQuotes( title );
	}

	public String getDescription()
	{
		return UtilStrings.fixQuotes( description );
	}

	public Date getStartDate()
	{
		try
		{
			return ScheduleDataReceiver.simpleCombinedFormat().parse( date + " " + time );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e.getMessage() + " Pattern: " + ScheduleDataReceiver.simpleCombinedFormat().toPattern(), e );
		}
	}

	public long getStartTime()
	{
		try
		{
			return ScheduleDataReceiver.simpleCombinedFormat().parse( date + " " + time ).getTime();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e.getMessage() + " Pattern: " + ScheduleDataReceiver.simpleCombinedFormat().toPattern(), e );
		}
	}

	public long getEndTime()
	{
		return getStartTime() + ( getDuration() * 60 );
	}

	public int getDuration()
	{
		return Integer.parseInt( duration );
	}

	public ModelLocation getLocation()
	{
		return ScheduleFragment.instance().getLocation( location );
	}

	public boolean hasEventReminderPassed()
	{
		long nowTime = new Date().getTime();
		long startTime = getStartTime();

		PLog.i( ">>> Event time debug (" + id + "): Now " + UtilDate.now( "MMM dx HH:mm:ss a", nowTime ) + " (" + nowTime + ") // Then " + UtilDate.now( "MMM dx HH:mm:ss a", startTime ) + " (" + startTime + ") // Reminder Delay " + AppService.getReminderDelay() );
		PLog.i( "Result: " + startTime + " - " + nowTime + " - " + AppService.getReminderDelay() + " = " + ( startTime - nowTime - AppService.getReminderDelay() ) );

		return getStartTime() - new Date().getTime() - AppService.getReminderDelay() < 0;
	}

	public boolean hasEventPassed()
	{
		return getStartTime() - new Date().getTime() < 0;
	}

	public boolean hasTimer()
	{
		if ( HomeActivity.instance == null || HomeActivity.instance.service == null )
		{
			PLog.e( "The AppService is not running. Why?" );
			return false;
		}

		boolean timer = getConfigurationSection().getBoolean( "timer", false );

		boolean pending = HomeActivity.instance.service.hasPushNotificationPending( id );
		boolean pasted = hasEventReminderPassed();

		if ( timer )
		{
			if ( pending && pasted )
				HomeActivity.instance.service.cancelPushNotification( id );
			else if ( !pending && !pasted )
				HomeActivity.instance.service.schedulePushNotification( this, false );
		}
		else if ( pending )
			HomeActivity.instance.service.cancelPushNotification( id );

		return timer;
	}

	public void setTimer( boolean timer )
	{
		if ( HomeActivity.instance == null || HomeActivity.instance.service == null )
		{
			PLog.e( "The AppService is not running. Why?" );
			return;
		}

		if ( timer && HomeActivity.instance.service.schedulePushNotification( this, true ) )
			getConfigurationSection().set( "timer", true );
		else
		{
			HomeActivity.instance.service.cancelPushNotification( id );
			getConfigurationSection().set( "timer", false );
		}
	}

	public Notification getNotification()
	{
		ModelLocation mLocation = getLocation();
		return new Notification.Builder( HomeActivity.instance ).setWhen( getStartTime() ).setContentTitle( "You have an event starting soon!" ).setSmallIcon( R.mipmap.ic_app ).setContentText( title + " starts at " + UtilDate.now( "h:mm a", getStartTime() ) + ( mLocation == null ? "" : " in " + mLocation.title ) ).build();
	}
}
