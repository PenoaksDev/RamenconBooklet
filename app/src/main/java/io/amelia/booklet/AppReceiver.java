package io.amelia.booklet;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import io.amelia.android.log.PLog;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.ScheduleEventModel;
import io.amelia.booklet.data.ScheduleHandler;

public class AppReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive( Context context, Intent intent )
	{
		if ( "android.intent.action.BOOT_COMPLETED".equals( intent.getAction() ) )
		{
			Intent serviceIntent = new Intent( context, AppService.class );
			context.startService( serviceIntent );
		}
		else if ( AppService.TRIGGER_ALARM.equals( intent.getAction() ) )
		{
			PowerManager pm = ( PowerManager ) context.getSystemService( Context.POWER_SERVICE );
			PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "" );
			wl.acquire();

			NotificationManager mNotificationManager = ( NotificationManager ) context.getSystemService( Context.NOTIFICATION_SERVICE );

			if ( mNotificationManager == null )
				throw new RuntimeException( "The Notification Service is not available" );

			Objs.isTrue( intent.hasExtra( "notice" ) );
			Objs.isTrue( intent.hasExtra( "id" ) );

			Notification notice = intent.getParcelableExtra( "notice" );

			Objs.isTrue( notice != null );

			String id = intent.getStringExtra( "id" );

			mNotificationManager.notify( id, 0, notice );

			if ( PreferenceManager.getDefaultSharedPreferences( context ).getBoolean( "pref_reminder_vibrate", true ) )
			{
				Vibrator v = ( Vibrator ) context.getSystemService( Context.VIBRATOR_SERVICE );
				if ( v != null )
					v.vibrate( 300 );
			}

			ScheduleEventModel event;
			if ( ( event = ContentManager.getActiveBooklet().getSectionHandler( ScheduleHandler.class ).getEvent( id ) ) != null )
				event.setTimer( false );

			// if ( ContentActivity.instance != null && ContentActivity.instance.service != null )
			// ContentActivity.instance.service.cancelPushNotification( id );

			wl.release();
		}
		else
			PLog.w( "Unrecognized action in AppReceiver: " + intent.getAction() );
	}
}
