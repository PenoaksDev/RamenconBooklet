package io.amelia.booklet.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.fragments.FragmentStack;
import io.amelia.android.log.PLog;
import io.amelia.android.support.UIUpdater;
import io.amelia.booklet.ui.fragment.DownloadFragment;

public class BootActivity extends BaseActivity
{
	public static BootActivity instance;
	public FragmentStack stacker;
	long backClickTime = 0;
	Toast mBackToast;
	private Bundle savedInstanceState;

	public BootActivity()
	{
		instance = this;
		stacker = new FragmentStack( getSupportFragmentManager(), R.id.content );
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = findViewById( R.id.drawer_layout );
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

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		PLog.i( "BootActivity.onCreate() (" + hashCode() + ") " + savedInstanceState );

		this.savedInstanceState = savedInstanceState;

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_boot );

		Toolbar toolbar = findViewById( R.id.toolbar );
		assert toolbar != null;
		setSupportActionBar( toolbar );

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled( true );
		actionBar.setDisplayUseLogoEnabled( true );

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
			stacker.setFragment( DownloadFragment.class );
		else
			stacker.loadInstanceState( savedInstanceState );
	}

	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		super.onSaveInstanceState( outState );
		stacker.saveInstanceState( outState );
	}

	@Override
	protected void processUpdate0( int type, BoundData data )
	{
		Fragment fragment = stacker.getCurrentFragment();
		if ( fragment instanceof UIUpdater.Receiver )
			( ( UIUpdater.Receiver ) fragment ).processUpdate( type, data );
	}
}
