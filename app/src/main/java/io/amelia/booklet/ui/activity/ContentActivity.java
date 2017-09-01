package io.amelia.booklet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.fragments.FragmentStack;
import io.amelia.android.log.PLog;
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.UIUpdater;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.ui.fragment.GuestFragment;
import io.amelia.booklet.ui.fragment.GuideFragment;
import io.amelia.booklet.ui.fragment.MapsFragment;
import io.amelia.booklet.ui.fragment.ScheduleFragment;
import io.amelia.booklet.ui.fragment.WelcomeFragment;

public class ContentActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener
{
	private static final int UPDATE_GOOGLE_PLAY = 24;
	public static ContentActivity instance;
	public FragmentStack stacker;
	private Bundle savedInstanceState;

	public ContentActivity()
	{
		instance = this;

		stacker = new FragmentStack( getSupportFragmentManager(), R.id.content_frame );

		stacker.registerFragment( R.id.nav_welcome, WelcomeFragment.class );
		stacker.registerFragment( R.id.nav_schedule, ScheduleFragment.class );
		stacker.registerFragment( R.id.nav_guests_vendors, GuestFragment.class );
		stacker.registerFragment( R.id.nav_maps, MapsFragment.class );
		stacker.registerFragment( R.id.nav_guide, GuideFragment.class );
		// stacker.registerFragment( R.id.nav_friends, FriendsFragment.class );
		// stacker.registerFragment( R.id.nav_share, .class );
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );

		if ( requestCode == UPDATE_GOOGLE_PLAY && resultCode != RESULT_OK )
			finish();
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = findViewById( R.id.drawer_layout );
		if ( drawer != null && drawer.isDrawerOpen( Gravity.START ) )
			drawer.closeDrawer( Gravity.START );
		else if ( stacker.hasBackstack() )
			stacker.popBackstack();
		else
			startActivity( new Intent( this, BootActivity.class ) );
		/*else if ( backClickTime > 0 && backClickTime - System.currentTimeMillis() < 3000 )
			finish();
		else
		{
			if ( mBackToast == null )
				mBackToast = Toast.makeText( this, "Press back again to quit.", Toast.LENGTH_SHORT );
			mBackToast.show();
			backClickTime = System.currentTimeMillis();
		}*/
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		PLog.i( "ContentActivity.onCreate() (" + hashCode() + ") " + savedInstanceState );

		this.savedInstanceState = savedInstanceState;

		super.onCreate( savedInstanceState );

		if ( ContentManager.getActiveBooklet() == null )
		{
			startActivity( new Intent( this, BootActivity.class ) );
			return;
		}

		int error = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable( this );
		if ( error != ConnectionResult.SUCCESS )
		{
			GoogleApiAvailability.getInstance().getErrorDialog( this, error, UPDATE_GOOGLE_PLAY ).show();
			return;
		}

		setContentView( R.layout.activity_content );

		Toolbar toolbar = findViewById( R.id.home_toolbar );
		assert toolbar != null;
		setSupportActionBar( toolbar );

		final DrawerLayout drawer = findViewById( R.id.drawer_layout );
		assert drawer != null;

		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
		// toggle.getDrawerArrowDrawable().setColor( android.graphics.Color.argb( 0xFF, 0xA6, 0x00, 0xFF ) );
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

		// TODO Should this only open certain times?
		drawer.openDrawer( Gravity.START );

		final NavigationView navigationView = findViewById( R.id.nav_view );
		assert navigationView != null;
		navigationView.setNavigationItemSelectedListener( this );

		View header = navigationView.getHeaderView( 0 );
		ImageView iHeader = header.findViewById( R.id.header_image );
		iHeader.setOnLongClickListener( new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick( View v )
			{
				ContentManager.refreshBooklets();
				recreate();

				Toast.makeText( ContentActivity.this, "Booklet was force refreshed.", Toast.LENGTH_SHORT ).show();

				return true;
			}
		} );

		if ( savedInstanceState == null )
			try
			{
				stacker.setFragment( WelcomeFragment.class );
			}
			catch ( RuntimeException e )
			{
				ACRAHelper.handleExceptionOnce( "welcome-fragment-exception", e );
				startActivity( new Intent( this, BootActivity.class ) );
			}
		else
			stacker.loadInstanceState( savedInstanceState );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		getMenuInflater().inflate( R.menu.content_options_menu, menu );
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	public boolean onNavigationItemSelected( @NonNull MenuItem item )
	{
		/* if (item.getItemId() == R.id.nav_signout)
			startActivity(new Intent(this, SigninActivity.class));
		else
		if ( item.getItemId() == R.id.nav_login_with_google )
			signinWorker.signinWithGoogle();
		else */
		/* if ( item.getItemId() == R.id.nav_booklets )
			startActivity( new Intent( this, BootActivity.class ) );
		else */
		stacker.setFragmentById( item.getItemId() );

		DrawerLayout drawer = findViewById( R.id.drawer_layout );
		assert drawer != null;
		drawer.closeDrawer( GravityCompat.START );

		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch ( item.getItemId() )
		{
			case R.id.options_booklets:
				startActivity( new Intent( this, BootActivity.class ) );
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
	public void processUpdate0( int type, BoundData data )
	{
		Fragment fragment = stacker.getCurrentFragment();
		if ( fragment instanceof UIUpdater.Receiver )
			( ( UIUpdater.Receiver ) fragment ).processUpdate( type, data );
	}

	public void uiHideProgressBar()
	{
		putUIUpdate( HIDE_PROGRESS_BAR, null );
	}
}
