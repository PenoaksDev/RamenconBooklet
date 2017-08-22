package io.amelia.booklet.ui;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ramencon.R;

import io.amelia.android.fragments.FragmentStack;
import io.amelia.android.log.PLog;
import io.amelia.booklet.ui.fragments.BootFragment;
import io.amelia.booklet.ui.fragments.SettingsFragment;

public class BootActivity extends AppCompatActivity
{
	private static final int UPDATE_GOOGLE_PLAY = 24;
	public FragmentStack stacker;
	long backClickTime = 0;
	Toast mBackToast;
	private Bundle savedInstanceState;

	public BootActivity()
	{
		stacker = new FragmentStack( getSupportFragmentManager(), R.id.boot_content );

		// stacker.registerFragment( R.id.nav_welcome, WelcomeFragment.class );
		// stacker.registerFragment( R.id.nav_schedule, ScheduleFragment.class );
		// stacker.registerFragment( R.id.nav_guests_vendors, GuestFragment.class );
		// stacker.registerFragment( R.id.nav_maps, MapsFragment.class );
		// stacker.registerFragment( R.id.nav_friends, FriendsFragment.class );
		stacker.registerFragment( R.id.options_settings, SettingsFragment.class );
		// stacker.registerFragment( R.id.nav_share, .class );
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		PLog.i( "BootActivity.onCreate() (" + hashCode() + ") " + savedInstanceState );
		this.savedInstanceState = savedInstanceState;

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_boot );

		int error = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable( this );
		if ( error != ConnectionResult.SUCCESS )
			GoogleApiAvailability.getInstance().getErrorDialog( this, error, UPDATE_GOOGLE_PLAY ).show();

		Toolbar toolbar = ( Toolbar ) findViewById( R.id.boot_toolbar );
		assert toolbar != null;
		setSupportActionBar( toolbar );

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
						mActionBar.setDisplayHomeAsUpEnabled( false );
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
					PLog.i( "There is no back stack!" );
			}
		} );

		if ( savedInstanceState == null )
			stacker.setFragment( BootFragment.class );
		else
			stacker.loadInstanceState( savedInstanceState );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.default_options_menu, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch ( item.getItemId() )
		{
			case R.id.options_settings:
				stacker.setFragmentById( item.getItemId(), true );
				return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		super.onSaveInstanceState( outState );
		stacker.saveInstanceState( outState );
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = ( DrawerLayout ) findViewById( R.id.drawer_layout );
		if ( drawer != null && drawer.isDrawerOpen( Gravity.START ) )
			drawer.closeDrawer( Gravity.START );
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

	public void factoryReset()
	{
		stacker.clearFragments();

		finish();
	}
}
