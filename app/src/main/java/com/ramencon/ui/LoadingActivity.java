package com.ramencon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.data.Persistence;
import com.ramencon.R;

import java.io.File;

public class LoadingActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener
{
	public static final int AUTH_ERROR = 99;
	public static final int PERSISTENCE_ERROR = 98;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		initPersistence(getCacheDir());

		FirebaseAuth auth = FirebaseAuth.getInstance();
		if (auth.getCurrentUser() == null)
		{
			auth.addAuthStateListener(this);
			auth.signInAnonymously();
		}
		else
			initHome();
	}

	public static void initPersistence(File cacheDir)
	{
		Persistence persistence = new Persistence(cacheDir);
		persistence.keepPersistent("booklet-data");
		persistence.keepPersistent("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
	}

	private void initHome()
	{
		if (Persistence.getInstance().check())
			startActivity(new Intent(this, HomeActivity.class));
		else
			startActivityForResult(new Intent(this, ErroredActivity.class).putExtra(ErroredActivity.ERROR_MESSAGE, "We had a problem syncing with the remote database. Are you connected to the internet?"), PERSISTENCE_ERROR);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
	{
		if (firebaseAuth.getCurrentUser() == null)
			startActivityForResult(new Intent(this, ErroredActivity.class).putExtra(ErroredActivity.ERROR_MESSAGE, "We had an authentication problem. Are you connected to the internet?"), AUTH_ERROR);
		else
			initHome();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == AUTH_ERROR)
			FirebaseAuth.getInstance().signInAnonymously();
		else if (requestCode == PERSISTENCE_ERROR)
			initHome();
		else
			super.onActivityResult(requestCode, resultCode, data);
	}
}
