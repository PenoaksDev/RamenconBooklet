package com.ramencon;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ramencon.ui.HomeActivity;

public class Common
{
	public static boolean haveNetworkConnection()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) HomeActivity.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
