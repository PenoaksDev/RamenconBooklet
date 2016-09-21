package com.ramencon.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.penoaks.fragments.FragmentStack;
import com.penoaks.helpers.Network;
import com.penoaks.log.PLog;
import com.ramencon.MonitorInstance;
import com.ramencon.R;
import com.ramencon.RamenApp;
import com.ramencon.SigninWorker;
import com.ramencon.data.Persistence;
import com.ramencon.system.AppService;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SigninWorker.SigninParent, MonitorInstance, ServiceConnection
{
	private static final int UPDATE_GOOGLE_PLAY = 24;

	public static final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://ramencon-booklet.appspot.com");

	public static HomeActivity instance;
	public FragmentStack stacker;
	public AppService service;

	private SigninWorker signinWorker;
	private AsyncTask<Void, Void, Void> taskChecker;

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

	private Bundle savedInstanceState;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("APP", "HomeActivity.onCreate() (" + hashCode() + ") " + savedInstanceState);

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

		int error = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
		if (error != ConnectionResult.SUCCESS)
			GoogleApiAvailability.getInstance().getErrorDialog(this, error, UPDATE_GOOGLE_PLAY).show();
		else
			taskChecker = new InternalStateChecker().execute();

		this.savedInstanceState = savedInstanceState;
	}

	private void createHomeView()
	{
		setContentView(R.layout.activity_home);

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
				Persistence.getInstance().renew();
				recreate();

				Toast.makeText(HomeActivity.this, "Booklet data was force refreshed.", Toast.LENGTH_SHORT).show();

				return true;
			}
		});

		authStateChanged(FirebaseAuth.getInstance().getCurrentUser());

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
				mBackToast = Toast.makeText(this, "Press back again to quit.", Toast.LENGTH_SHORT);
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

		if (requestCode == UPDATE_GOOGLE_PLAY)
		{
			if (resultCode == RESULT_OK)
				taskChecker = new InternalStateChecker().execute();
			else
				finish();
		}
		else
			signinWorker.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		signinWorker = SigninWorker.getInstance(this);

		RamenApp.start(this);

		bindService(new Intent(this, AppService.class), this, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		unbindService(this);

		if (signinWorker != null)
		{
			signinWorker.stop();
			signinWorker = null;
		}

		if (taskChecker != null)
		{
			taskChecker.cancel(true);
			taskChecker = null;
		}

		RamenApp.stop(this);
	}

	@Override
	public void authStateChanged(FirebaseUser currentUser)
	{
		final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		if (navigationView != null)
		{
			View header = navigationView.getHeaderView(0);

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

			stacker.refreshFragment();
		}
	}

	@Override
	public Activity getActivity()
	{
		return this;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		PLog.i("Success binding to AppService");

		this.service = ((AppService.ServiceBinder) service).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		PLog.i("Failed binding to AppService");

		service = null;
	}

	class InternalStateChecker extends AsyncTask<Void, Void, Void>
	{
		private int tryCounter = 0;

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

			if (notReady())
			{
				String msg;
				if (!Network.internetAvailable())
					msg = "We had a problem connecting to the internet. Please check your connection.";
				else if (FirebaseAuth.getInstance().getCurrentUser() == null)
					msg = "We had an authentication problem. Are you connected to the internet?";
				else
					msg = "We had a problem syncing with the remote database. Are you connected to the internet?";

				startActivityForResult(new Intent(HomeActivity.this, ErroredActivity.class).putExtra(ErroredActivity.ERROR_MESSAGE, msg), -1);
			}
			else
				createHomeView();

			taskChecker = null;
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			while (notReady())
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

		private boolean notReady()
		{
			return !Persistence.getInstance().check() && FirebaseAuth.getInstance().getCurrentUser() == null;
		}
	}

	public void signOut()
	{
		signinWorker.signOut();
	}
}
