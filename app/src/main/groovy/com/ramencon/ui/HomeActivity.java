package com.ramencon.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.penoaks.android.fragments.FragmentStack;
import com.penoaks.android.helpers.PostTask;
import com.penoaks.android.log.PLog;
import com.penoaks.booklet.AppState;
import com.penoaks.booklet.AppStateEvent;
import com.penoaks.booklet.AppStates;
import com.penoaks.booklet.AppTicks;
import com.penoaks.booklet.DataPersistence;
import com.ramencon.ContextReference;
import com.ramencon.R;
import com.ramencon.RamenApp;
import com.ramencon.SigninWorker;
import com.ramencon.system.AppService;

import java.util.concurrent.Callable;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SigninWorker.SigninParent, ContextReference, ServiceConnection, AppState.Listener
{
	private static final int UPDATE_GOOGLE_PLAY = 24;

	public static HomeActivity instance;
	public FragmentStack stacker;
	public AppService service;

	private PostTask postTask;
	private SigninWorker signinWorker;

	public HomeActivity()
	{
		instance = this;

		stacker = new FragmentStack( getSupportFragmentManager(), R.id.content_frame );

		stacker.registerFragment( R.id.nav_welcome, WelcomeFragment.class );
		stacker.registerFragment( R.id.nav_schedule, ScheduleFragment.class );
		stacker.registerFragment( R.id.nav_guests_vendors, GuestFragment.class );
		stacker.registerFragment( R.id.nav_maps, MapsFragment.class );
		// stacker.registerFragment(R.id.nav_friends, FriendsFragment.class);
		stacker.registerFragment( R.id.nav_settings, SettingsFragment.class );
		// stacker.registerFragment(R.id.nav_share, .class);
	}

	private Bundle savedInstanceState;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		PLog.i( "HomeActivity.onCreate() (" + hashCode() + ") " + savedInstanceState );

		this.savedInstanceState = savedInstanceState;

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_loading );

		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo( getPackageName(), 0 );
			( ( TextView ) findViewById( R.id.loading_version ) ).setText( pInfo.versionName );
		}
		catch ( Exception ignore )
		{
			// Ignore
		}

		int error = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable( this );
		if ( error != ConnectionResult.SUCCESS )
			GoogleApiAvailability.getInstance().getErrorDialog( this, error, UPDATE_GOOGLE_PLAY ).show();

		if ( !AppTicks.isRunning() && AppTicks.getLastException() != null )
			startActivityForResult( new Intent( HomeActivity.this, ErroredActivity.class ).putExtra( ErroredActivity.ERROR_MESSAGE, AppTicks.getLastException().getClass().getSimpleName() + ": " + AppTicks.getLastException().getMessage() ), -1 );
		else
		{
			postTask = new PostTask( new Callable()
			{
				@Override
				public Object call() throws Exception
				{
					createHomeView();
					return null;
				}
			} );

			AppState.getInstance().addListener( AppState.ListenerLevel.HIGHEST, this );
		}
	}

	private void createHomeView()
	{
		PLog.i( "HomeActivity.createHomeView(" + hashCode() + ")" );

		setContentView( R.layout.activity_home );

		Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
		assert toolbar != null;
		setSupportActionBar( toolbar );

		final DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.drawer_layout );
		assert drawer != null;
		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
		drawer.setDrawerListener( toggle );
		toggle.syncState();

		stacker.addOnBackStackChangedListener( new FragmentStack.OnBackStackChangedListener()
		{
			@Override
			public void onBackStackChanged()
			{
				ActionBar mActionBar = getSupportActionBar();
				if ( mActionBar != null )
				{
					if ( stacker.hasBackstack() )
						mActionBar.setDisplayHomeAsUpEnabled( true );
					else
					{
						mActionBar.setDisplayHomeAsUpEnabled( false );
						toggle.syncState();
					}
				}
			}
		} );

		toolbar.setNavigationOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				if ( stacker.hasBackstack() )
					stacker.popBackstack();
				else
					drawer.openDrawer( GravityCompat.START );
			}
		} );

		final NavigationView navigationView = ( NavigationView ) findViewById( R.id.nav_view );
		assert navigationView != null;
		navigationView.setNavigationItemSelectedListener( this );

		View header = navigationView.getHeaderView( 0 );
		ImageView iHeader = ( ImageView ) header.findViewById( R.id.header_image );
		iHeader.setOnLongClickListener( new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick( View v )
			{
				DataPersistence.getInstance().forceUpdate();
				recreate();

				Toast.makeText( HomeActivity.this, "Booklet data was force refreshed.", Toast.LENGTH_SHORT ).show();

				return true;
			}
		} );

		authStateChanged( FirebaseAuth.getInstance().getCurrentUser() );

		if ( savedInstanceState == null )
			stacker.setFragment( WelcomeFragment.class );
		else
			stacker.loadInstanceState( savedInstanceState );
	}

	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		super.onSaveInstanceState( outState );
		stacker.saveInstanceState( outState );
	}

	long backClickTime = 0;
	Toast mBackToast;

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.drawer_layout );
		if ( drawer != null && drawer.isDrawerOpen( GravityCompat.START ) )
			drawer.closeDrawer( GravityCompat.START );
		else if ( stacker.hasBackstack() )
			stacker.popBackstack();
		else if ( backClickTime > 0 && backClickTime - System.currentTimeMillis() < 3000 )
			finish();
		else
		{
			if ( mBackToast == null )
				mBackToast = Toast.makeText( this, "Press back again to quit.", Toast.LENGTH_SHORT );
			mBackToast.show();
			backClickTime = System.currentTimeMillis();
		}

	}

	@Override
	public boolean onNavigationItemSelected( MenuItem item )
	{
		/* if (item.getItemId() == R.id.nav_signout)
			startActivity(new Intent(this, SigninActivity.class));
		else */
		if ( item.getItemId() == R.id.nav_login_with_google )
			signinWorker.signinWithGoogle();
		else if ( item.getItemId() == R.id.nav_signout )
			signinWorker.signOut();
		else
			stacker.setFragmentById( item.getItemId() );

		DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.drawer_layout );
		assert drawer != null;
		drawer.closeDrawer( GravityCompat.START );

		return true;
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );

		if ( requestCode == UPDATE_GOOGLE_PLAY )
		{
			if ( resultCode != RESULT_OK )
				finish();
		}
		else
			signinWorker.onActivityResult( requestCode, resultCode, data );
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		signinWorker = SigninWorker.getInstance( this );

		RamenApp.start( this );

		bindService( new Intent( this, AppService.class ), this, BIND_AUTO_CREATE );
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		unbindService( this );

		if ( signinWorker != null )
		{
			signinWorker.stop();
			signinWorker = null;
		}

		RamenApp.stop( this );
	}

	@Override
	public void authStateChanged( FirebaseUser currentUser )
	{
		final NavigationView navigationView = ( NavigationView ) findViewById( R.id.nav_view );
		if ( navigationView != null )
		{
			View header = navigationView.getHeaderView( 0 );

			TextView tWelcomeText = ( TextView ) header.findViewById( R.id.header_text );

			FirebaseAuth mAuth = FirebaseAuth.getInstance();
			if ( mAuth.getCurrentUser() == null || mAuth.getCurrentUser().isAnonymous() )
			{
				navigationView.getMenu().findItem( R.id.nav_login_with_google ).setVisible( true );
				navigationView.getMenu().findItem( R.id.nav_signout ).setVisible( false );

				tWelcomeText.setVisibility( View.GONE );
			}
			else
			{
				navigationView.getMenu().findItem( R.id.nav_login_with_google ).setVisible( false );
				navigationView.getMenu().findItem( R.id.nav_signout ).setVisible( true );

				if ( FirebaseAuth.getInstance().getCurrentUser().getDisplayName() == null )
					tWelcomeText.setVisibility( View.GONE );
				else
				{
					tWelcomeText.setVisibility( View.VISIBLE );
					tWelcomeText.setText( "Welcome, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() );
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
	public void onServiceConnected( ComponentName name, IBinder service )
	{
		PLog.i( "Success binding to AppService" );

		this.service = ( ( AppService.ServiceBinder ) service ).getService();
	}

	@Override
	public void onServiceDisconnected( ComponentName name )
	{
		PLog.i( "Failed binding to AppService" );

		service = null;
	}

	public void signOut()
	{
		if ( signinWorker == null )
			FirebaseAuth.getInstance().signOut();
		else
			signinWorker.signOut();
	}

	@Override
	public void onPersistenceEvent( AppStateEvent event )
	{
		if ( event.state == AppStates.SUCCESS )
			postTask.done();
			// createHomeView();
		else if ( !event.isHandled )
		{
			startActivityForResult( new Intent( HomeActivity.this, ErroredActivity.class ).putExtra( ErroredActivity.ERROR_MESSAGE, event.getMessage() ), -1 );
			event.isHandled = true;
		}
	}
}
