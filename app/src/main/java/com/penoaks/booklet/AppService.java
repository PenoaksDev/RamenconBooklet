package com.penoaks.booklet;

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

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.android.events.EventManager;
import com.penoaks.android.log.PLog;
import com.penoaks.android.tasks.TaskManager;
import com.penoaks.booklet.events.ApplicationExceptionEvent;
import com.penoaks.booklet.events.ApplicationFinishEvent;
import com.penoaks.booklet.events.ApplicationStateEvent;
import com.penoaks.configuration.ConfigurationSection;
import com.ramencon.data.models.ModelEvent;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.data.schedule.filters.DefaultScheduleFilter;
import com.ramencon.ui.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppService extends Service
{
	public final static String TRIGGER_ALARM = "com.ramencon.action.TRIGGER_ALARM";

	private final EventManager eventManager = new EventManager( this );
	private final TaskManager taskManager = new TaskManager( this );
	private final AppTicks appTicks = new AppTicks( this );
	private Thread primaryThread = null;
	private AppStates lastState = null;
	private Throwable lastException;

	static
	{
		PLog.i( "The AppService has been started!" );
	}

	public EventManager getEventManager()
	{
		return eventManager;
	}

	public TaskManager getTaskManager()
	{
		return taskManager;
	}

	public boolean isPrimaryThread()
	{
		return Thread.currentThread().equals( primaryThread );
	}

	public void tick( int currentTick )
	{
		taskManager.heartbeat( currentTick );
		DataPersistence.getInstance().heartbeat( currentTick );

		AppStates newState = DataPersistence.getInstance().eventState();

		// Every 4 seconds
		if ( currentTick % 80 == 0 )
			PLog.i( hashCode() + ") Application Tick! " + currentTick + " --> " + newState );

		// Run state check every half-second.
		if ( currentTick % 10 == 0 )
		{
			if ( newState == AppStates.SUCCESS && FirebaseAuth.getInstance().getCurrentUser() == null )
				newState = AppStates.NO_USER;

			if ( newState != lastState )
			{
				PLog.i( "App State has changed from " + lastState + " to " + newState );

				ApplicationStateEvent event = new ApplicationStateEvent( newState );

				lastState = newState;
				eventManager.callEvent( event );

				if ( newState == AppStates.SUCCESS )
				{
					scheduleData = ScheduleDataReceiver.getInstance();
					restartAllNotifications( getReminderDelay() );
				}
			}
		}
	}

	public void onException( Throwable t )
	{
		lastException = t;
		t.printStackTrace();
		eventManager.callEvent( new ApplicationExceptionEvent( t ) );
	}

	public void onFinish()
	{
		eventManager.callEvent( new ApplicationFinishEvent() );
	}

	public Throwable getLastException()
	{
		return lastException;
	}


	public ScheduleDataReceiver scheduleData;

	private final Map<String, PendingIntent> pendingNotices = new ConcurrentHashMap<>();

	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		return START_STICKY;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		if ( primaryThread != null )
			throw new IllegalStateException( "The Primary Thread is not null! Something is wrong!" );

		primaryThread = new Thread( appTicks, "Tick Thread" );
		primaryThread.setPriority( Thread.MAX_PRIORITY );
		primaryThread.start();

		DataSource.DataIntegrityChecker checker = new DataSource.DataIntegrityChecker()
		{
			@Override
			public boolean checkIntegrity( String uri, ConfigurationSection section )
			{
				// PLog.i( "Data Debug (" + uri + ") (" + ( section.getChildren().size() > 0 ) + ") " + section.getValues( false ) );
				return section.getChildren().size() > 0;
			}
		};

		DataPersistence.getInstance().registerDataSource( "booklet-data/guests", "guests", checker );
		DataPersistence.getInstance().registerDataSource( "booklet-data/locations", "locations", checker );
		DataPersistence.getInstance().registerDataSource( "booklet-data/maps", "maps", checker );
		DataPersistence.getInstance().registerDataSource( "booklet-data/schedule", "schedule", checker );

		PLog.i( "The AppService has been initialized!" );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		appTicks.stop();

		PLog.i( "The AppService has been destroyed!" );
	}

	public void restartAllNotifications( long reminderDelay )
	{
		cancelAllNotifications();

		Set<ModelEvent> events = scheduleData.filterRange( new DefaultScheduleFilter().setHasTimer( DefaultScheduleFilter.TriStateList.SHOW ) );
		for ( ModelEvent event : events )
			if ( !hasPushNotificationPending( event.id ) )
				if ( !schedulePushNotification( event, event.getStartTime() - reminderDelay, false ) )
					event.setTimer( false );
	}

	public void cancelAllNotifications()
	{
		if ( pendingNotices.size() > 0 )
		{
			AlarmManager mgr = ( AlarmManager ) ( HomeActivity.instance.getSystemService( Context.ALARM_SERVICE ) );

			for ( PendingIntent pIntent : pendingNotices.values() )
				mgr.cancel( pIntent );
			pendingNotices.clear();
		}
	}

	public static int getReminderDelay()
	{
		if ( HomeActivity.instance == null )
			return 10 * 60 * 1000; // Temp fix for inherit app bugs.

		int reminder_delay = Integer.parseInt( PreferenceManager.getDefaultSharedPreferences( HomeActivity.instance ).getString( "pref_reminder_delays", "10" ) );
		reminder_delay = reminder_delay * 60 * 1000;

		return reminder_delay;
	}

	public boolean schedulePushNotification( ModelEvent event, boolean makeToast )
	{
		return schedulePushNotification( event, event.getStartTime() - getReminderDelay(), makeToast );
	}

	public boolean schedulePushNotification( ModelEvent event, long when, boolean makeToast )
	{
		Context context = HomeActivity.instance;

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

	public void cancelPushNotification( String id )
	{
		if ( !pendingNotices.containsKey( id ) )
			return;

		AlarmManager mgr = ( AlarmManager ) ( HomeActivity.instance.getSystemService( Context.ALARM_SERVICE ) );

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

	private final IBinder mBinder = new ServiceBinder();

	public class ServiceBinder extends Binder
	{
		public AppService getService()
		{
			return AppService.this;
		}
	}
}
