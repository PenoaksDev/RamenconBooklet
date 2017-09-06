package io.amelia.booklet.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.support.WaitFor;
import io.amelia.booklet.data.Booklet;
import io.amelia.booklet.data.BookletState;
import io.amelia.booklet.data.ContentManager;

public class LoadActivity extends BaseActivity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_load );

		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo( getPackageName(), 0 );
			( ( TextView ) findViewById( R.id.loading_version ) ).setText( "Version: " + pInfo.versionName );
		}
		catch ( Exception ignore )
		{

		}

		new WaitFor( new WaitFor.Task()
		{
			@Override
			public boolean isReady()
			{
				return ContentManager.isContentManagerReady();
			}

			@Override
			public void onFinish()
			{
				Booklet booklet = ContentManager.getActiveBookletSafe();

				if ( booklet != null && booklet.getState() == BookletState.READY )
				{
					// Load Booklet
					startActivity( new Intent( LoadActivity.this, ContentActivity.class ) );
				}
				else
				{
					// Send to DownloadActivity
					startActivity( new Intent( LoadActivity.this, BootActivity.class ) );
				}
			}

			@Override
			public void onTimeout()
			{
				// Never Set
			}
		} );
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		ContentManager.onStartActivity( this );
	}

	@Override
	protected void processUpdate0( int type, BoundData data )
	{

	}
}
