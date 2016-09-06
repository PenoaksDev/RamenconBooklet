package com.ramencon.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ramencon.R;
import com.ramencon.models.ModelEvent;
import com.ramencon.data.schedule.ScheduleAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduleViewFragment extends Fragment
{
	public static ScheduleAdapter adapter;

	public static ScheduleViewFragment instance(int id)
	{
		Bundle bundle = new Bundle();
		bundle.putInt("event", id);

		ScheduleViewFragment frag = new ScheduleViewFragment();
		frag.setArguments(bundle);
		return frag;
	}

	public ScheduleViewFragment()
	{

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().setTitle("View Event");

		View root = inflater.inflate(R.layout.fragment_schedule_view, container, false);

		TextView tv_title = (TextView) root.findViewById(R.id.event_view_title);
		TextView tv_location = (TextView) root.findViewById(R.id.event_view_location);
		TextView tv_date = (TextView) root.findViewById(R.id.event_view_date);
		TextView tv_time = (TextView) root.findViewById(R.id.event_view_time);
		TextView tv_desc = (TextView) root.findViewById(R.id.event_view_description);

		if (getArguments() == null)
			tv_title.setText("There was a problem! :(");
		else
		{
			ModelEvent event = adapter.events.get(getArguments().getInt("event"));

			tv_title.setText(event.title);
			tv_location.setText(event.location);
			tv_desc.setText( event.description );

			try
			{
				Date date = adapter.dateFormat.parse(event.date);
				Date from = adapter.timeFormat.parse(event.time);
				Date to = new Date(from.getTime() + (60000 * Integer.parseInt(event.duration)));

				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
				boolean use24h = pref.getBoolean("pref_military", false);

				String format_from = use24h ? ScheduleAdapter.DISPLAY_FORMAT_TIME24.format(from) : ScheduleAdapter.DISPLAY_FORMAT_TIME12.format(from);
				String format_to = use24h ? ScheduleAdapter.DISPLAY_FORMAT_TIME24.format(to) : ScheduleAdapter.DISPLAY_FORMAT_TIME12.format(to);

				tv_date.setText( new SimpleDateFormat("EEEE MM/dd").format( date ) );
				tv_time.setText(format_from + " — " + format_to);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}

		return root;
	}
}
