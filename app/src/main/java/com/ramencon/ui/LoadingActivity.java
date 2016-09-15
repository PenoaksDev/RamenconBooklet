package com.ramencon.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.helpers.Network;
import com.ramencon.R;
import com.ramencon.data.Persistence;

public class LoadingActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView) findViewById(R.id.loading_version)).setText(pInfo.versionName);
		}
		catch (Exception ignore)
		{

		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onAuthStateChanged(FirebaseAuth firebaseAuth)
	{
		if (firebaseAuth.getCurrentUser() == null)
			firebaseAuth.signInAnonymously();
	}

	private boolean stateNotReady()
	{
		return !Persistence.getInstance().check() && FirebaseAuth.getInstance().getCurrentUser() == null;
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		FirebaseAuth.getInstance().addAuthStateListener(this);

		if (taskChecker == null)
			new InternalStateChecker().execute();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		FirebaseAuth.getInstance().removeAuthStateListener(this);

		if (taskChecker != null)
			taskChecker.cancel(true);
	}

	private InternalStateChecker taskChecker = null;

	class InternalStateChecker extends AsyncTask<Void, Void, Void>
	{
		private int tryCounter = 0;

		InternalStateChecker()
		{
			taskChecker = this;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			Persistence.getInstance().renew();
		}

		@Override
		protected void onPostExecute(Void aVoid)
		{
			super.onPostExecute(aVoid);

			if (stateNotReady())
			{
				String msg;
				if (!Network.internetAvailable())
					msg = "We had a problem connecting to the internet. Please check your connection.";
				else if (FirebaseAuth.getInstance().getCurrentUser() == null)
					msg = "We had an authentication problem. Are you connected to the internet?";
				else
					msg = "We had a problem syncing with the remote database. Are you connected to the internet?";

				startActivityForResult(new Intent(LoadingActivity.this, ErroredActivity.class).putExtra(ErroredActivity.ERROR_MESSAGE, msg), -1);
			}
			else
				startActivity(new Intent(LoadingActivity.this, HomeActivity.class));

			taskChecker = null;
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			while (stateNotReady())
			{
				tryCounter++;

				if (tryCounter > 300 || isCancelled()) // 6.5 Seconds
					break;

				try
				{
					Thread.sleep(25);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
