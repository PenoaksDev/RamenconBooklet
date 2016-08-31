package com.ramencon.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ramencon.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class WelcomeFragment extends Fragment
{
	public WelcomeFragment()
	{
	}

	public static WelcomeFragment instance()
	{
		return new WelcomeFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().setTitle("About the Convention");

		View root = inflater.inflate(R.layout.fragment_welcome, container, false);

		/*
		try
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			final Intent iCal = new Intent(Intent.ACTION_EDIT);
			iCal.setType("vnd.android.cursor.item/event");
			iCal.putExtra(CalendarContract.Events.TITLE, "Ramencon");
			iCal.putExtra(CalendarContract.Events.DESCRIPTION, "Ramencon is an anime convention in Northwest Indiana, built by anime fans, for anime fans. Much of what you experience at the con has been suggested by all of you! We want everyone to have the best time possible and be comfortable enough to do everything you want in one weekend.");
			iCal.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, format.parse("2016-09-23"));
			iCal.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, format.parse("2016-09-25"));
			iCal.putExtra(CalendarContract.Events.ALL_DAY, true);

			TextView tv_date = (TextView) root.findViewById(R.id.welcome_date);
			tv_date.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (iCal.resolveActivity(getActivity().getPackageManager()) != null)
						startActivity(iCal);
					else
						Toast.makeText(getActivity(), "Sorry, it appears you don't have a calendar app installed.", Toast.LENGTH_LONG).show();
				}
			});
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		*/

		Uri mapIntentUri = Uri.parse("gwo:0,0?q=800 E 81st Ave Merrillville, IN 46410");
		final Intent iMap = new Intent(Intent.ACTION_VIEW, mapIntentUri);
		iMap.setPackage("com.google.android.apps.maps");

		TextView tv_location = (TextView) root.findViewById(R.id.welcome_location);
		tv_location.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (iMap.resolveActivity(getActivity().getPackageManager()) != null)
					startActivity(iMap);
				else
					Toast.makeText(getActivity(), "Sorry, it appears you don't have Google Maps installed.", Toast.LENGTH_LONG).show();
			}
		});

		/*
		WebView web = (WebView) root.findViewById(R.id.htmlWelcome);

		if (CacheManager.instance().fileWelcome().exists())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(CacheManager.instance().fileWelcome());

				StringBuffer fileContent = new StringBuffer("");

				byte[] buffer = new byte[1024];

				int n;
				while ((n = fis.read(buffer)) != -1)
					fileContent.append(new String(buffer, 0, n));

				web.loadData(fileContent.toString(), "text/html", "utf-8");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (fis != null)
					try
					{
						fis.close();
					}
					catch (IOException ignore)
					{

					}
			}
		}
		else
			web.loadData("The Welcome Data File is missing!", "text/html", "utf-8");
			*/

		return root;
	}
}
