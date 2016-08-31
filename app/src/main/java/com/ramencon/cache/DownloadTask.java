package com.ramencon.cache;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.ramencon.ui.ErroredActivity;
import com.ramencon.ui.HomeActivity;
import com.ramencon.ui.LoadingFragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DownloadTask extends AsyncTask<DownloadReference, Void, DownloadResult>
{
	// private DownloadReference lastDownload = null;
	private DownloadReference currentDownload;
	private int fileIndex;
	private int fileCount;

	private Runnable afterDownload;
	private Context context;
	private PowerManager.WakeLock mWakeLock;

	private ProgressDialog mDialog;

	public DownloadTask(Context context, Runnable afterDownload)
	{
		this.context = context;
		this.afterDownload = afterDownload;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		PowerManager pm = (PowerManager) HomeActivity.instance.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		mWakeLock.acquire();

		mDialog = new ProgressDialog(context);
		mDialog.setMessage("Downloading Booklet Data...");
		mDialog.setCancelable(false);
		mDialog.show();
	}

	@Override
	protected void onProgressUpdate(Void... arg)
	{
		super.onProgressUpdate(arg);

		String percent = currentDownload.progress > 0 ? currentDownload.progress + "% Complete" : "";

		mDialog.setMessage(currentDownload.displayName + "...\n" + "(Stage " + fileIndex + " of " + fileCount + ") " + percent);

		if ( currentDownload.progress > 0 )
		{
			mDialog.setIndeterminate(false);
			mDialog.setProgress(currentDownload.progress);
		}
		else
			mDialog.setIndeterminate(true);
	}

	@Override
	protected void onPostExecute(DownloadResult result)
	{
		mWakeLock.release();
		mDialog.cancel();

		if (result != null && result.exception != null)
		{
			context.startActivity(new Intent(context, ErroredActivity.class).putExtra("exception", result.exception));
			return;
		}

		if (result.errored)
		{
			context.startActivity(new Intent(context, ErroredActivity.class).putExtra("errorMessage", result.message));
			return;
		}

		if ( afterDownload != null )
			afterDownload.run();
	}

	@Override
	protected DownloadResult doInBackground(DownloadReference... files)
	{
		fileIndex = 0;
		fileCount = files.length;

		for (DownloadReference file : files)
		{
			fileIndex++;
			currentDownload = file;

			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try
			{
				URL url = new URL(file.sourceUrl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("Accept-Encoding", "identity");

				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return new DownloadResult(true, "HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getHeaderFieldInt("content-length", -1);

				// download the file
				input = connection.getInputStream();
				output = new FileOutputStream(file.dest);

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1)
				{
					// allow canceling with back button
					if (isCancelled())
					{
						input.close();
						return new DownloadResult(false, "Download has been cancelled.");
					}
					total += count;
					currentDownload.progress = fileLength > 0 ? (int)  (total * 100 / fileLength) : -0;

					publishProgress();
					output.write(data, 0, count);
				}
			}
			catch (Exception e)
			{
				return new DownloadResult(e);
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
		}

		return new DownloadResult(false, "Booklet Data Downloaded Successfully");
	}
}