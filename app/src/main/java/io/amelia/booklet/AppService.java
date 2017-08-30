package io.amelia.booklet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.amelia.android.log.PLog;
import io.amelia.booklet.data.ScheduleEventModel;
import io.amelia.booklet.data.ScheduleHandler;
import io.amelia.booklet.data.filters.DefaultScheduleFilter;
import io.amelia.booklet.ui.activity.ContentActivity;

public class AppService extends Service
{
	public final static String TRIGGER_ALARM = "com.ramencon.action.TRIGGER_ALARM";

	static
	{
		PLog.i( "The AppService has been started!" );
	}

	public static int getReminderDelay()
	{
		if ( ContentActivity.instance == null )
			return 10 * 60 * 1000; // Temp fix for inherit app bugs.

		int reminder_delay = Integer.parseInt( PreferenceManager.getDefaultSharedPreferences( ContentActivity.instance ).getString( "pref_reminder_delays", "10" ) );
		reminder_delay = reminder_delay * 60 * 1000;

		return reminder_delay;
	}

	private final IBinder mBinder = new ServiceBinder();
	private final Map<String, PendingIntent> pendingNotices = new ConcurrentHashMap<>();
	public ScheduleHandler scheduleData;

	public void cancelAllNotifications()
	{
		if ( pendingNotices.size() > 0 )
		{
			AlarmManager mgr = ( AlarmManager ) ( ContentActivity.instance.getSystemService( Context.ALARM_SERVICE ) );

			for ( PendingIntent pIntent : pendingNotices.values() )
				mgr.cancel( pIntent );
			pendingNotices.clear();
		}
	}

	public void cancelPushNotification( String id )
	{
		if ( !pendingNotices.containsKey( id ) )
			return;

		AlarmManager mgr = ( AlarmManager ) ( ContentActivity.instance.getSystemService( Context.ALARM_SERVICE ) );

		if ( mgr != null )
			mgr.cancel( pendingNotices.remove( id ) );
	}

	public boolean hasPushNotificationPending( String id )
	{
		return pendingNotices.containsKey( id );
	}

	@Nullable
	@Override
	public IBinder onBind( Intent intent )
	{
		return mBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		PLog.i( "The AppService has been initialized!" );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		PLog.i( "The AppService has been destroyed!" );
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		return START_STICKY;
	}

	public void restartAllNotifications( long reminderDelay )
	{
		cancelAllNotifications();

		Set<ScheduleEventModel> events = scheduleData.filterRange( new DefaultScheduleFilter().setHasTimer( DefaultScheduleFilter.TriStateList.SHOW ) );
		for ( ScheduleEventModel event : events )
			if ( !hasPushNotificationPending( event.id ) )
				if ( !schedulePushNotification( event, event.getStartTime() - reminderDelay, false ) )
					event.setTimer( false );
	}

	public boolean schedulePushNotification( ScheduleEventModel event, boolean makeToast )
	{
		return schedulePushNotification( event, event.getStartTime() - getReminderDelay(), makeToast );
	}

	public boolean schedulePushNotification( ScheduleEventModel event, long when, boolean makeToast )
	{
		Context context = ContentActivity.instance;

		if ( event.hasEventReminderPassed() )
		{
			if ( makeToast )
				Toast.makeText( context, "The event cannot be scheduled because the trigger has time has already passed", Toast.LENGTH_SHORT ).show();
			return false;
		}

		AlarmManager mgr = ( AlarmManager ) ( context.getSystemService( Context.ALARM_SERVICE ) );

		Intent intent = new Intent( context, AppReceiver.class );

		intent.putExtra( "notice", event.getNotification() );
		intent.putExtra( "id", event.id );

		intent.setAction( TRIGGER_ALARM );

		PendingIntent pIntent = PendingIntent.getBroadcast( context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT );

		pendingNotices.put( event.id, pIntent );

		if ( makeToast )
			Toast.makeText( context, "Event reminder set for " + new SimpleDateFormat( "MM/dd hh:mm a" ).format( new Date( when ) ), Toast.LENGTH_LONG ).show();

		PLog.i( "The event " + event.id + " was scheduled for a push notification at " + new SimpleDateFormat( "MM/dd hh:mm a" ).format( new Date( when ) ) );

		mgr.set( AlarmManager.RTC_WAKEUP, when, pIntent );

		return true;
	}

	public class ServiceBinder extends Binder
	{
		public AppService getService()
		{
			return AppService.this;
		}
	}
}
