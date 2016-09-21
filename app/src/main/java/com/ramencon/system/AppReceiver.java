package com.ramencon.system;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.penoaks.log.PLog;
import com.ramencon.data.models.ModelEvent;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.ui.HomeActivity;

public class AppReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
		{
			Intent serviceIntent = new Intent("com.ramencon.system.AppService");
			context.startService(serviceIntent);
		}
		else if (AppService.TRIGGER_ALARM.equals(intent.getAction()))
		{
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wl.acquire();

			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			if (mNotificationManager == null)
				throw new RuntimeException("The Notification Service is not available");

			assert intent.hasExtra("notice");
			assert intent.hasExtra("id");

			Notification notice = intent.getParcelableExtra("notice");

			assert notice != null;

			String id = intent.getStringExtra("id");

			mNotificationManager.notify(id, 0, notice);

			if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_reminder_vibrate", true))
			{
				Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
				if (v != null)
					v.vibrate(300);
			}

			ModelEvent event;
			if ((event = ScheduleDataReceiver.getInstance().getEvent(id)) != null)
				event.setTimer(false);

			if (HomeActivity.instance != null && HomeActivity.instance.service != null)
				HomeActivity.instance.service.cancelPushNotification(id);

			wl.release();
		}
		else
			PLog.w("Unrecognized action in AppReceiver: " + intent.getAction());
	}
}
