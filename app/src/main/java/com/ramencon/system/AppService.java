package com.ramencon.system;

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

import com.penoaks.log.PLog;
import com.ramencon.MonitorInstance;
import com.ramencon.RamenApp;
import com.ramencon.data.PersistenceStateChecker;
import com.ramencon.data.PersistenceStateListener;
import com.ramencon.data.models.ModelEvent;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.data.schedule.filters.DefaultScheduleFilter;
import com.ramencon.ui.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppService extends Service implements MonitorInstance, PersistenceStateListener
{
	public final static String TRIGGER_ALARM = "com.ramencon.action.TRIGGER_ALARM";

	public ScheduleDataReceiver scheduleData;

	private final Map<String, PendingIntent> pendingNotices = new ConcurrentHashMap<>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		RamenApp.start(this);

		PersistenceStateChecker.addListener(PersistenceStateChecker.ListenerLevel.NORMAL, this);
		PersistenceStateChecker.makeInstance(false);
	}

	public void restartAllNotifications(long reminderDelay)
	{
		cancelAllNotifications();

		Set<ModelEvent> events = scheduleData.filterRange(new DefaultScheduleFilter().setHasTimer(DefaultScheduleFilter.TriStateList.SHOW));
		for (ModelEvent event : events)
			if (!hasPushNotificationPending(event.id))
				if (!schedulePushNotification(event, event.getStartTime() - reminderDelay, false))
					event.setTimer(false);
	}

	public void cancelAllNotifications()
	{
		if (pendingNotices.size() > 0)
		{
			AlarmManager mgr = (AlarmManager) (HomeActivity.instance.getSystemService(Context.ALARM_SERVICE));

			for (PendingIntent pIntent : pendingNotices.values())
				mgr.cancel(pIntent);
			pendingNotices.clear();
		}
	}

	@Override
	public void onDestroy()
	{
		RamenApp.stop(this);

		super.onDestroy();
	}

	public static int getReminderDelay()
	{
		int reminder_delay = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(HomeActivity.instance).getString("pref_reminder_delays", "10"));
		reminder_delay = reminder_delay * 60 * 1000;

		return reminder_delay;
	}

	public boolean schedulePushNotification(ModelEvent event, boolean makeToast)
	{
		return schedulePushNotification(event, event.getStartTime() - getReminderDelay(), makeToast);
	}

	public boolean schedulePushNotification(ModelEvent event, long when, boolean makeToast)
	{
		Context context = HomeActivity.instance;

		if (event.hasEventReminderPassed())
		{
			if (makeToast)
				Toast.makeText(context, "The event cannot be scheduled because the trigger has time has already passed", Toast.LENGTH_SHORT).show();
			return false;
		}

		AlarmManager mgr = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));

		Intent intent = new Intent(context, AppReceiver.class);

		intent.putExtra("notice", event.getNotification());
		intent.putExtra("id", event.id);

		intent.setAction(TRIGGER_ALARM);

		PendingIntent pIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		pendingNotices.put(event.id, pIntent);

		if (makeToast)
			Toast.makeText(context, "Event reminder set for " + new SimpleDateFormat("MM/dd hh:mm a").format(new Date(when)), Toast.LENGTH_LONG).show();

		PLog.i("The event " + event.id + " was scheduled for a push notification at " + new SimpleDateFormat("MM/dd hh:mm a").format(new Date(when)));

		mgr.set(AlarmManager.RTC_WAKEUP, when, pIntent);

		return true;
	}

	public void cancelPushNotification(String id)
	{
		if (!pendingNotices.containsKey(id))
			return;

		AlarmManager mgr = (AlarmManager) (HomeActivity.instance.getSystemService(Context.ALARM_SERVICE));

		if (mgr != null)
			mgr.cancel(pendingNotices.remove(id));
	}

	public boolean hasPushNotificationPending(String id)
	{
		return pendingNotices.containsKey(id);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	private final IBinder mBinder = new ServiceBinder();

	@Override
	public void onPersistenceError(String msg)
	{
		Toast.makeText(this, "Ramencon App Error: " + msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPersistenceReady()
	{
		scheduleData = ScheduleDataReceiver.getInstance();
		restartAllNotifications(getReminderDelay());
	}

	public class ServiceBinder extends Binder
	{
		public AppService getService()
		{
			return AppService.this;
		}
	}
}
