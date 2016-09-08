package com.penoaks.log;

import android.util.Log;

public class PLog
{
	private static final String DEFAULT_TAG = "PLOG";
	private static Logger logger = new AndroidLogger();

	public static void i(String msg, Object... objs)
	{
		Log.i(DEFAULT_TAG, String.format(msg, objs));
	}

	public static void w(String msg, Object... objs)
	{
		Log.w(DEFAULT_TAG, String.format(msg, objs));
	}

	public static void w(Throwable throwable, String msg, Object... objs)
	{
		Log.w(DEFAULT_TAG, String.format(msg, objs), throwable);
	}

	public static void e(String msg, Object... objs)
	{
		Log.e(DEFAULT_TAG, String.format(msg, objs));
	}

	public static void e(Throwable throwable, String msg, Object... objs)
	{
		Log.e(DEFAULT_TAG, String.format(msg, objs), throwable);
	}
}
