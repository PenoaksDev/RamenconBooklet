package com.penoaks.booklet.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ramencon.R;
import com.penoaks.booklet.AppService;

public class ErroredActivity extends AppCompatActivity
{
	public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_errored );

		Intent intent = getIntent();
		if ( intent.hasExtra( ERROR_MESSAGE ) )
		{
			TextView tv_msg = ( TextView ) findViewById( R.id.errored_msg );
			tv_msg.setText( intent.getStringExtra( ERROR_MESSAGE ) );
		}

		ImageView errored_image = ( ImageView ) findViewById( R.id.errored_image );
		errored_image.setOnLongClickListener( new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick( View v )
			{
				AlertDialog.Builder builder = new AlertDialog.Builder( ErroredActivity.this );
				builder.setTitle( "App Data Reset" ).setMessage( "Are you sure you wish to factory reset the app?" ).setPositiveButton( "Yes Please!", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						HomeActivity.instance.signOut();

						HomeActivity.instance.stacker.clearFragments();

						DataPersistence.getInstance().forceUpdate();

						Toast.makeText( ErroredActivity.this, "App Successfully Factory Reset", Toast.LENGTH_LONG ).show();

						stopService( new Intent( AppService.class.getName() ) );
						HomeActivity.instance.finish();
						finish();
					}
				} ).setNegativeButton( "Opps, wrong button", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						dialog.dismiss();
					}
				} ).create().show();

				return true;
			}
		} );

		final Button tryAgain = ( Button ) findViewById( R.id.tryagain );
		tryAgain.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				setResult( RESULT_OK );
				finish();
			}
		} );
	}
}
