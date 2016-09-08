package com.ramencon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.penoaks.data.Persistence;
import com.penoaks.helpers.DataManager;
import com.penoaks.helpers.FragmentStack;
import com.ramencon.R;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DataManager.OnDataListener
{
	public final static StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://ramencon-booklet.appspot.com");

	public static HomeActivity instance;
	public static Persistence persistence;
	public FragmentStack stacker;

	public HomeActivity()
	{
		instance = this;

		stacker = new FragmentStack(getSupportFragmentManager(), R.id.content_frame);

		stacker.registerFragment(R.id.nav_welcome, WelcomeFragment.class);
		stacker.registerFragment(R.id.nav_schedule, ScheduleFragment.class);
		stacker.registerFragment(R.id.nav_guests_vendors, GuestFragment.class);
		stacker.registerFragment(R.id.nav_maps, MapsFragment.class);
		// stacker.registerFragment(R.id.nav_friends, FriendsFragment.class);
		stacker.registerFragment(R.id.nav_settings, SettingsFragment.class);
		// stacker.registerFragment(R.id.nav_share, .class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		Log.i("APP", "HomeActivity.onCreate() (" + hashCode() + ") " + savedInstanceState);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		assert toolbar != null;
		setSupportActionBar(toolbar);

		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		assert drawer != null;
		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		final FragmentManager mgr = getSupportFragmentManager();
		mgr.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
		{
			@Override
			public void onBackStackChanged()
			{
				ActionBar mActionBar = getSupportActionBar();
				if (mActionBar != null)
				{
					if (mgr.getBackStackEntryCount() > 0)
						mActionBar.setDisplayHomeAsUpEnabled(true);
					else
					{
						mActionBar.setDisplayHomeAsUpEnabled(false);
						toggle.syncState();
					}
				}
			}
		});

		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mgr.getBackStackEntryCount() > 0)
					mgr.popBackStackImmediate();
				else
					drawer.openDrawer(GravityCompat.START);
			}
		});

		final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		assert navigationView != null;
		navigationView.setNavigationItemSelectedListener(this);

		assert FirebaseAuth.getInstance().getCurrentUser() != null;

		persistence = new Persistence(getCacheDir());

		persistence.keepPersistent("booklet-data");
		persistence.keepPersistent("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

		((TextView) navigationView.getHeaderView(0).findViewById(R.id.header_text)).setText("Welcome, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

		if (savedInstanceState == null)
		{
			stacker.setFragment(LoadingFragment.class);
			DataManager.instance().setListener(this).checkConnection();
		}
		else
			stacker.loadInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		persistence.destroy();
		persistence = null;
	}

	public void onConnected()
	{
		((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).setChecked(true);
		stacker.setFragment(WelcomeFragment.class);
	}

	public void onError(DatabaseError error)
	{
		startActivityForResult(new Intent(this, ErroredActivity.class).putExtra("errorMessage", error.getMessage()), 99);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 99 && resultCode == RESULT_OK)
		{
			stacker.setFragment(LoadingFragment.class);
			DataManager.instance().setListener(this).checkConnection();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		stacker.saveInstanceState(outState);
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		assert drawer != null;
		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else
			super.onBackPressed();
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.nav_signout)
		{
			startActivity(new Intent(this, SigninActivity.class));
		}
		else
		{
			stacker.setFragmentById(item.getItemId());
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		assert drawer != null;
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}
}
