package io.amelia.booklet.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.amelia.R;
import io.amelia.android.fragments.FragmentStack;
import io.amelia.android.log.PLog;
import io.amelia.booklet.ui.fragment.DownloadFragment;

public class BootActivity extends AppCompatActivity
{
	public static BootActivity instance;
	public FragmentStack stacker;
	private Bundle savedInstanceState;

	public BootActivity()
	{
		instance = this;

		stacker = new FragmentStack( getSupportFragmentManager(), R.id.boot_content );
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		PLog.i( "BootActivity.onCreate() (" + hashCode() + ") " + savedInstanceState );

		this.savedInstanceState = savedInstanceState;

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_boot );

		Toolbar toolbar = ( Toolbar ) findViewById( R.id.boot_toolbar );
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
}
