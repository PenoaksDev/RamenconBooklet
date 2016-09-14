package com.ramencon;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.penoaks.log.PLog;
import com.ramencon.alarm.AlarmReceiver;
import com.ramencon.ui.HomeActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppService extends FirebaseInstanceIdService
{
	private final static AppService service = new AppService();
	private final static AlarmReceiver receiver = new AlarmReceiver();

	private final Map<String, PendingIntent> pendingNotices = new ConcurrentHashMap<>();

	public static AppService instance()
	{
		return service;
	}

	@Override
	public void onTokenRefresh()
	{
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		PLog.i("Refreshed token: " + refreshedToken);
	}

	public void cancelAllNotifications()
	{
		AlarmManager mgr = (AlarmManager) (HomeActivity.instance.getSystemService(Context.ALARM_SERVICE));

		for (PendingIntent pIntent : pendingNotices.values())
			mgr.cancel(pIntent);
		pendingNotices.clear();
	}

	public void schedulePushNotification(String id, long epoch, Notification notice)
	{
		if (epoch < 0)
			return;

		Context context = HomeActivity.instance;

		AlarmManager mgr = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));

		Intent intent = new Intent(context, AlarmReceiver.class);

		intent.putExtra("notice", notice);
		intent.putExtra("id", id);

		PendingIntent pIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		pendingNotices.put(id, pIntent);

		Toast.makeText(context, "Event reminder set for " + new SimpleDateFormat("MM/dd hh:mm a").format(new Date(epoch)), Toast.LENGTH_LONG).show();

		int reminder_delay = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_reminder_delays", "15"));
		reminder_delay = reminder_delay * 60 * 1000;

		epoch = epoch - reminder_delay;

		mgr.set(AlarmManager.RTC_WAKEUP, epoch, pIntent);
	}

	public void cancelPushNotification(String id)
	{
		if (!pendingNotices.containsKey(id))
			return;

		AlarmManager mgr = (AlarmManager) (HomeActivity.instance.getSystemService(Context.ALARM_SERVICE));

		mgr.cancel(pendingNotices.remove(id));
	}

	public boolean hasPushNotificationPending(String id)
	{
		return pendingNotices.containsKey(id);
	}
}
