package io.amelia.booklet.data;

import android.app.Notification;

import java.util.Date;

import io.amelia.R;
import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.support.DateAndTime;
import io.amelia.android.support.Strs;
import io.amelia.booklet.ui.activity.ContentActivity;
import io.amelia.booklet.ui.fragment.ScheduleFragment;

public class ScheduleEventModel
{
	public String date;
	public String description;
	public String duration;
	public String id;
	public String location;
	public String time;
	public String title;

	private ConfigurationSection getConfigurationSection()
	{
		// return DataPersistence.getInstance().config( "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/events/" + id );

		return null;
	}

	public String getDescription()
	{
		return Strs.fixQuotes( description );
	}

	public int getDuration()
	{
		return Integer.parseInt( duration );
	}

	public long getEndTime()
	{
		return getStartTime() + ( getDuration() * 60 );
	}

	public MapsLocationModel getLocation()
	{
		return ScheduleFragment.instance().getLocation( location );
	}

	public Notification getNotification()
	{
		MapsLocationModel mLocation = getLocation();
		return new Notification.Builder( ContentActivity.instance ).setWhen( getStartTime() ).setContentTitle( "You have an event starting soon!" ).setSmallIcon( R.mipmap.ic_app ).setContentText( title + " starts at " + DateAndTime.now( "h:mm a", getStartTime() ) + ( mLocation == null ? "" : " in " + mLocation.title ) ).build();
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
		try
		{
			return ScheduleHandler.simpleCombinedFormat().parse( date + " " + time ).getTime();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e.getMessage() + " Pattern: " + ScheduleHandler.simpleCombinedFormat().toPattern(), e );
		}
	}

	public String getTitle()
	{
		return Strs.fixQuotes( title );
	}

	public boolean hasEventPassed()
	{
		return getStartTime() - new Date().getTime() < 0;
	}

	public boolean hasTimer()
	{
		/*if ( ContentActivity.instance == null || ContentActivity.instance.service == null )
		{
			PLog.e( "The AppService is not running. Why?" );
			return false;
		}

		boolean timer = getConfigurationSection().getBoolean( "timer", false );

		boolean pending = ContentActivity.instance.service.hasPushNotificationPending( id );
		boolean pasted = hasEventReminderPassed();

		if ( timer )
		{
			if ( pending && pasted )
				ContentActivity.instance.service.cancelPushNotification( id );
			else if ( !pending && !pasted )
				ContentActivity.instance.service.schedulePushNotification( this, false );
		}
		else if ( pending )
			ContentActivity.instance.service.cancelPushNotification( id );

		return timer;*/

		return false;
	}

	public boolean isHearted()
	{
		return getConfigurationSection().getBoolean( "hearted", false );
	}

	public void setHearted( boolean hearted )
	{
		getConfigurationSection().set( "hearted", hearted );
	}

	public void setTimer( boolean timer )
	{
		/*if ( ContentActivity.instance == null || ContentActivity.instance.service == null )
		{
			PLog.e( "The AppService is not running. Why?" );
			return;
		}

		if ( timer && ContentActivity.instance.service.schedulePushNotification( this, true ) )
			getConfigurationSection().set( "timer", true );
		else
		{
			ContentActivity.instance.service.cancelPushNotification( id );
			getConfigurationSection().set( "timer", false );
		}*/
	}
}
