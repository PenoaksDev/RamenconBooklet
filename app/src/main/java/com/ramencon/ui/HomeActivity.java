package com.ramencon.ui;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramencon.data.Persistence;
import com.penoaks.fragments.FragmentStack;
import com.ramencon.R;
import com.ramencon.SigninWorker;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	public static final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://ramencon-booklet.appspot.com");

	public static HomeActivity instance;
	public FragmentStack stacker;
	private SigninWorker signinWorker;

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

		stacker.addOnBackStackChangedListener(new FragmentStack.OnBackStackChangedListener()
		{
			@Override
			public void onBackStackChanged()
			{
				ActionBar mActionBar = getSupportActionBar();
				if (mActionBar != null)
				{
					if (stacker.hasBackstack())
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
				if (stacker.hasBackstack())
					stacker.popBackstack();
				else
					drawer.openDrawer(GravityCompat.START);
			}
		});

		final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		assert navigationView != null;
		navigationView.setNavigationItemSelectedListener(this);

		View header = navigationView.getHeaderView(0);

		ImageView iHeader = (ImageView) header.findViewById(R.id.header_image);
		iHeader.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				recreate();
				return true;
			}
		});

		TextView tWelcomeText = (TextView) header.findViewById(R.id.header_text);

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		if (mAuth.getCurrentUser() == null || mAuth.getCurrentUser().isAnonymous())
		{
			navigationView.getMenu().findItem(R.id.nav_login_with_google).setVisible(true);
			navigationView.getMenu().findItem(R.id.nav_signout).setVisible(false);

			tWelcomeText.setVisibility(View.GONE);
		}
		else
		{
			navigationView.getMenu().findItem(R.id.nav_login_with_google).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_signout).setVisible(true);

			if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() == null)
				tWelcomeText.setVisibility(View.GONE);
			else
			{
				tWelcomeText.setVisibility(View.VISIBLE);
				tWelcomeText.setText("Welcome, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
			}
		}

		if (savedInstanceState == null)
			stacker.setFragment(WelcomeFragment.class);
		else
			stacker.loadInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		stacker.saveInstanceState(outState);
	}

	long backClickTime = 0;
	Toast mBackToast;

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		assert drawer != null;
		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else if (stacker.hasBackstack())
			stacker.popBackstack();
		else if (backClickTime > 0 && backClickTime - System.currentTimeMillis() < 3000)
			finish();
		else
		{
			if (mBackToast == null)
				mBackToast = Toast.makeText(this, "Press back again to quit", Toast.LENGTH_SHORT);
			mBackToast.show();
			backClickTime = System.currentTimeMillis();
		}

	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		/* if (item.getItemId() == R.id.nav_signout)
			startActivity(new Intent(this, SigninActivity.class));
		else */
		if (item.getItemId() == R.id.nav_login_with_google)
			signinWorker.signinWithGoogle();
		else if (item.getItemId() == R.id.nav_signout)
			signinWorker.signOut();
		else
			stacker.setFragmentById(item.getItemId());

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		assert drawer != null;
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		signinWorker.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		signinWorker = SigninWorker.getInstance(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		Persistence.getInstance().destroy();

		signinWorker.stop();
		signinWorker = null;
	}
}
