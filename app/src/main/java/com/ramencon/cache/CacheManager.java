package com.ramencon.cache;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.penoaks.Files;
import com.ramencon.Common;
import com.ramencon.ui.ErroredActivity;
import com.ramencon.ui.HomeActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CacheManager
{
	private static final String URL_WELCOME = "http://api.penoaks.com/appdata/ramencon/welcome";
	private static final String URL_SCHEDULE = "http://api.penoaks.com/appdata/ramencon/schedule";
	private static final String URL_GUESTS = "http://api.penoaks.com/appdata/ramencon/guests";
	private static final String URL_EXHIBITORS = "http://api.penoaks.com/appdata/ramencon/exhibitors";
	private static final String URL_CACHE_VERSION = "http://api.penoaks.com/appdata/ramencon/lastUpdated";

	private boolean hasCheckedCache = false;
	private boolean downloadWelcomePack = false;
	private boolean downloadSchedule = false;
	private boolean downloadGuest = false;
	private boolean downloadExhibitors = false;
	private boolean cacheIsExpired = false;

	private static CacheManager instance;

	public static CacheManager instance()
	{
		if (instance == null)
			instance = new CacheManager();
		return instance;
	}

	private File cacheDir = HomeActivity.instance.getCacheDir();

	public boolean isCached()
	{
		hasCheckedCache = true;

		if (!fileWelcome().exists())
			downloadWelcomePack = true;

		if (!fileSchedule().exists())
			downloadSchedule = true;

		if (!fileGuests().exists())
			downloadGuest = true;

		if (!fileExhibitors().exists())
			downloadExhibitors = true;

		try
		{
			String cacheCurrentVersion = Files.getStringFromFile(fileCacheVersion(), "NullifiedString").trim();
			Log.i("CacheManager", "Current Booklet Data Version: " + cacheCurrentVersion);
			String cacheLatestVersion = cacheLatestVersion().trim();

			if (!cacheCurrentVersion.equals(cacheLatestVersion))
			{
				Log.i("CacheManager", "Booklet Data Update Available: " + cacheLatestVersion);
				Files.putStringToFile(fileCacheVersion(), cacheLatestVersion);
				cacheIsExpired = true;
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return cacheIsExpired || downloadWelcomePack || downloadSchedule || downloadGuest || downloadExhibitors;
	}

	public boolean forkCacheDownload(Context context, boolean force)
	{
		return forkCacheDownload(context, force, null);
	}

	/**
	 * Fork a cache download
	 *
	 * @param context       The calling context
	 * @param force         Force the downloads and don't check local cache
	 * @param afterDownload Runnable to be called after download finishes
	 * @return boolean Was the cache download successfully forked
	 */
	public boolean forkCacheDownload(Context context, boolean force, Runnable afterDownload)
	{
		if (!hasCheckedCache && !force)
			return false;

		if (!Common.haveNetworkConnection())
			context.startActivity(new Intent(context, ErroredActivity.class));

		List<DownloadReference> files = new ArrayList<>();

		if (downloadWelcomePack || cacheIsExpired || force)
			files.add(new DownloadReference(URL_WELCOME, fileWelcome(), "Downloading Booklet Data"));

		if (downloadSchedule || cacheIsExpired || force)
			files.add(new DownloadReference(URL_SCHEDULE, fileSchedule(), "Downloading Schedule Data"));

		if (downloadGuest || cacheIsExpired || force)
			files.add(new DownloadReference(URL_GUESTS, fileGuests(), "Downloading Guest Data"));

		if (downloadExhibitors || cacheIsExpired || force)
			files.add(new DownloadReference(URL_EXHIBITORS, fileExhibitors(), "Downloading Exhibitors Data"));

		if (files.size() > 0)
		{
			new DownloadTask(context, afterDownload).execute(files.toArray(new DownloadReference[0]));
			return true;
		}
		else
			return false;
	}

	public File fileWelcome()
	{
		return new File(cacheDir, "welcome.html");
	}

	public File fileSchedule()
	{
		return new File(cacheDir, "schedule.json");
	}

	public File fileGuests()
	{
		return new File(cacheDir, "guests.json");
	}

	public File fileExhibitors()
	{
		return new File(cacheDir, "exhibitors.json");
	}

	public File fileCacheVersion()
	{
		return new File(cacheDir, "version.txt");
	}

	/**
	 * Gets the latest cache version string from remote server.
	 * MUST BE CALLED FROM AN ASYNC TASK!
	 *
	 * @return String The latest cache version
	 */
	public String cacheLatestVersion() throws IOException
	{
		InputStream input = null;
		ByteArrayOutputStream output = null;
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(URL_CACHE_VERSION);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity");

			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new IOException("HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());

			input = connection.getInputStream();
			output = new ByteArrayOutputStream();

			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1)
			{
				total += count;
				output.write(data, 0, count);
			}
		}
		finally
		{
			try
			{
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			}
			catch (IOException ignored)
			{
			}

			if (connection != null)
				connection.disconnect();
		}

		return new String(output.toByteArray());
	}
}
