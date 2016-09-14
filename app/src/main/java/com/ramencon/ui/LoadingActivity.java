package com.ramencon.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ramencon.R;
import com.ramencon.data.Persistence;

public class LoadingActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		new InternalStateChecker().execute();

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
		// startActivityForResult(new Intent(this, ErroredActivity.class).putExtra(ErroredActivity.ERROR_MESSAGE, "We had an authentication problem. Are you connected to the internet?"), AUTH_ERROR);
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
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		FirebaseAuth.getInstance().removeAuthStateListener(this);
	}

	class InternalStateChecker extends AsyncTask<Void, Void, Void>
	{
		private int tryCounter = 0;

		@Override
		protected void onPostExecute(Void aVoid)
		{
			super.onPostExecute(aVoid);

			if (!stateNotReady())
				startActivity(new Intent(LoadingActivity.this, HomeActivity.class));
			else
				throw new RuntimeException("");
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			while (stateNotReady())
			{
				tryCounter++;

				if (tryCounter > 300) // 6.5 Seconds
					startActivity(new Intent(LoadingActivity.this, ErroredActivity.class).putExtra(ErroredActivity.ERROR_MESSAGE, FirebaseAuth.getInstance().getCurrentUser() == null ? "We had an authentication problem. Are you connected to the internet?" : "We had a problem syncing with the remote database. Are you connected to the internet?"));

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
