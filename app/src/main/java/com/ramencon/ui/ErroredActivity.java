package com.ramencon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ramencon.Common;
import com.ramencon.R;

public class ErroredActivity extends AppCompatActivity
{
	private boolean generalException = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_errored);

		Intent intent = getIntent();

		if ( intent.hasExtra("errorMessage") )
		{
			TextView msg = (TextView) findViewById( R.id.errored_msg );

			msg.setText( intent.getStringExtra( "errorMessage" ) );

			generalException = true;
		}

		if( intent.hasExtra("exception") )
		{
			TextView msg = (TextView) findViewById( R.id.errored_msg );

			Exception e = (Exception) intent.getSerializableExtra("exception");

			msg.setText( e.getMessage() );

			generalException = true;
		}

		final Button tryAgain = (Button) findViewById(R.id.tryagain);
		final ErroredActivity activity = this;
		tryAgain.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if ( generalException || Common.haveNetworkConnection())
					startActivity(new Intent(activity, HomeActivity.class).putExtra("download", true));
				else
					Toast.makeText( activity, "We're sorry, it seems we are still having issues.", Toast.LENGTH_SHORT ).show();
			}
		});
	}
}
