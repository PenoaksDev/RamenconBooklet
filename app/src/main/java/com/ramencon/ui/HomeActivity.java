package com.ramencon.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.penoaks.ui.FragmentStack;
import com.ramencon.R;
import com.ramencon.ui.schedule.ScheduleFragment;

import org.acra.ACRA;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	public static HomeActivity instance;

	public FragmentStack stacker;
	// private long lastClick = 0;
	// private int clickCount = 0;
	// private Toast lastToast;

	public HomeActivity()
	{
		instance = this;

		stacker = new FragmentStack(getFragmentManager(), R.id.content_frame);

		stacker.registerFragment(-1, DeveloperFragment.class);
		stacker.registerFragment(R.id.nav_welcome, WelcomeFragment.class);
		stacker.registerFragment(R.id.nav_schedule, ScheduleFragment.class);
		// stacker.registerFragment(R.id.nav_exhibitors_guests, .class);
		// stacker.registerFragment(R.id.nav_maps, .class);
		// stacker.registerFragment(R.id.nav_share, .class);
		// stacker.registerFragment(R.id.nav_friends, .class);
		stacker.registerFragment(R.id.nav_settings, SettingsFragment.class);
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
		// drawer.setDrawerListener(toggle);
		toggle.syncState();

		final FragmentManager mgr = getFragmentManager();
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

		navigationView.getMenu().getItem(0).setChecked(true);

		/*
		View header = navigationView.getHeaderView(0);
		header.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (lastClick - System.currentTimeMillis() < 1000)
					clickCount++;
				else
					clickCount = 1;

				lastClick = System.currentTimeMillis();

				if (lastToast != null)
					lastToast.cancel();

				if (clickCount >= 7 && clickCount < 12)
				{
					lastToast = Toast.makeText(HomeActivity.this, (12 - clickCount) + " clicks until developer", Toast.LENGTH_SHORT);
					lastToast.show();
				}

				if (clickCount == 12)
					navigationView.getMenu().add(0, -1, 0, "Developer Tools").setIcon(R.drawable.ic_developer);
			}
		});
		*/

		if (savedInstanceState == null)
			stacker.setFragment(LoadingFragment.class);
		else
			stacker.loadInstanceState(savedInstanceState);
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
		// if (stacker.setFragmentById(item.getItemId()))
		//	setTitle(item.getTitle());

		stacker.setFragmentById(item.getItemId());

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		assert drawer != null;
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}
