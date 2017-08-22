package io.amelia.android.support;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.amelia.booklet.ui.BootActivity;

public class LibAndroid
{
	public static boolean haveNetworkConnection()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) BootActivity.instance.getSystemService( Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
