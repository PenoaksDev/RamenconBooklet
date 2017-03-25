package com.penoaks.android.log;

import android.util.Log;

public class PLog
{
	private static final String DEFAULT_TAG = "PLOG";

	public static void i(String msg, Object... objs)
	{
		Log.i(DEFAULT_TAG, objs == null || objs.length == 0 ? msg : String.format(msg, objs));
	}

	public static void w(String msg, Object... objs)
	{
		Log.w(DEFAULT_TAG, objs == null || objs.length == 0 ? msg : String.format(msg, objs));
	}

	public static void w(Throwable throwable, String msg, Object... objs)
	{
		Log.w(DEFAULT_TAG, objs == null || objs.length == 0 ? msg : String.format(msg, objs), throwable);
	}

	public static void e(String msg, Object... objs)
	{
		Log.e(DEFAULT_TAG, String.format(msg, objs));
	}

	public static void e(Throwable throwable, String msg, Object... objs)
	{
		Log.e(DEFAULT_TAG, objs == null || objs.length == 0 ? msg : String.format(msg, objs), throwable);
	}
}
