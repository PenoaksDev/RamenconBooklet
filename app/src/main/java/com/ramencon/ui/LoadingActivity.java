package com.ramencon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.data.Persistence;
import com.ramencon.R;

public class LoadingActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		Persistence persistence = new Persistence(getCacheDir());
		persistence.keepPersistent("booklet-data");
		persistence.keepPersistent("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

		FirebaseAuth auth = FirebaseAuth.getInstance();
		if (auth.getCurrentUser() == null)
		{
			auth.addAuthStateListener(this);
			auth.signInAnonymously();
		}
		else
			initHome();
	}

	private void initHome()
	{
		if (Persistence.getInstance().check())
			startActivity(new Intent(this, HomeActivity.class));
		else
			startActivityForResult(new Intent(this, ErroredActivity.class), ErroredActivity.PERSISTENCE_ERROR);
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
			startActivityForResult(new Intent(this, ErroredActivity.class), ErroredActivity.AUTH_ERROR);
		else
			initHome();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ErroredActivity.AUTH_ERROR)
		{
			FirebaseAuth.getInstance().signInAnonymously();
		}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}
}
