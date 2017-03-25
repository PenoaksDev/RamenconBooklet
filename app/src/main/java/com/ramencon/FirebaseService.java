package com.ramencon;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseService extends FirebaseMessagingService
{
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		Log.d("APP", "From: " + remoteMessage.getFrom());

		if (remoteMessage.getData().size() > 0)
		{
			Log.d("APP", "Message data payload" + remoteMessage.getData());
		}

		if (remoteMessage.getNotification() != null)
		{
			Log.d("APP", "Message Notification Body: " + remoteMessage.getNotification().getBody());
		}
	}
}
