package io.amelia.booklet.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ramencon.R;

import io.amelia.android.support.WaitFor;
import io.amelia.booklet.Booklet;
import io.amelia.booklet.BookletState;
import io.amelia.booklet.ContentManager;

public class LoadActivity extends AppCompatActivity
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
				Booklet booklet = ContentManager.getActiveBooklet();

				if ( booklet == null || booklet.getState() == BookletState.OUTDATED )
				{
					// Send to DownloadActivity
					startActivity( new Intent( LoadActivity.this, DownloadActivity.class ) );
				}
				else
				{
					// Load Booklet
					startActivity( new Intent( LoadActivity.this, ContentActivity.class ) );
				}
			}

			@Override
			public void onTimeout()
			{
				// Never Set
			}
		} );
	}

}
