package com.ramencon.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ramencon.Common;
import com.ramencon.R;

public class ErroredActivity extends AppCompatActivity
{
	public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_errored);

		Intent intent = getIntent();
		if (intent.hasExtra(ERROR_MESSAGE))
		{
			TextView tv_msg = (TextView) findViewById(R.id.errored_msg);
			tv_msg.setText(intent.getStringExtra(ERROR_MESSAGE));
		}

		final Button tryAgain = (Button) findViewById(R.id.tryagain);
		final ErroredActivity activity = this;
		tryAgain.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_OK);
				finish();
			}
		});
	}
}
