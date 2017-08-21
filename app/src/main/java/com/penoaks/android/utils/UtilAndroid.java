package com.penoaks.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UtilAndroid
{
	public static boolean haveNetworkConnection()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) HomeActivity.instance.getSystemService( Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
