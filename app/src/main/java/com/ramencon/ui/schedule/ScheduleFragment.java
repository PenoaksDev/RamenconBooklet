package com.ramencon.ui.schedule;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.penoaks.ui.CryogenFragment;
import com.ramencon.R;
import com.ramencon.cache.CacheManager;
import com.ramencon.cache.skel.ModelEvent;
import com.ramencon.ui.HomeActivity;
import com.ramencon.ui.schedule.filters.DefaultScheduleFilter;

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ScheduleFragment extends Fragment implements CryogenFragment
{
	private static ScheduleFragment instance = null;

	public static ScheduleFragment instance()
	{
		return instance;
	}

	public ScheduleManager mgr;
	public DefaultScheduleFilter currentFilter = new DefaultScheduleFilter();

	private Bundle loadedState = null;
	private ScheduleDayAdapter scheduleDayAdapter;
	private ScheduleAdapter scheduleAdapter;

	public ScheduleFragment()
	{
		instance = this;

		try
		{
			mgr = new ScheduleManager(CacheManager.instance().fileSchedule());
		}
		catch (IOException | ParseException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().setTitle("Convention Schedule");

		View root = inflater.inflate(R.layout.fragment_schedule, container, false);

		try
		{
			List<Date> days = mgr.sampleDays();

			TwoWayView mDayView = (TwoWayView) root.findViewById(R.id.daylist);
			TextView mDateDisplay = (TextView) root.findViewById(R.id.date_display);

			ExpandableListView lv = (ExpandableListView) root.findViewById(R.id.listView);

			int selectedPosition = 0;
			List<ModelEvent> data = null;
			int scrollTo = -1;

			if (loadedState == null || loadedState.isEmpty())
			{
				currentFilter.setMin(days.get(0).getTime());
				currentFilter.setMax(days.get(0).getTime() + (1440 * 60 * 1000)); // One Day

				data = mgr.filterRange(currentFilter);
			}
			else
			{
				try
				{
					currentFilter = DefaultScheduleFilter.getAdapter().fromJson(loadedState.getString("filter"));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				selectedPosition = loadedState.getInt("selectedPosition", 0);
				data = mgr.filterRange(currentFilter);

				if (loadedState.getInt("listViewPosition", -1) > 0)
					scrollTo = loadedState.getInt("listViewPosition");
			}

			scheduleDayAdapter = new ScheduleDayAdapter(this, days, mDateDisplay, selectedPosition);
			mDayView.setAdapter(scheduleDayAdapter);

			scheduleAdapter = new ScheduleAdapter(getActivity(), mgr.simpleDateFormat(), mgr.simpleTimeFormat(), mgr.model.locations, data);
			lv.setAdapter(scheduleAdapter);

			if (scrollTo > 0)
				lv.setVerticalScrollbarPosition(scrollTo);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return root;
	}

	public void loadList(List<ModelEvent> list)
	{
		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.listView);
		lv.setAdapter(new ScheduleAdapter(HomeActivity.instance, mgr.simpleDateFormat(), mgr.simpleTimeFormat(), mgr.model.locations, list));
	}

	@Override
	public void saveState(Bundle bundle)
	{
		if (getView() == null)
			return;

		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.listView);

		bundle.putString("filter", DefaultScheduleFilter.getAdapter().toJson(currentFilter));
		bundle.putInt("listViewPosition", lv.getFirstVisiblePosition());
		bundle.putInt("selectedPosition", scheduleDayAdapter.getSelectedPosition());

		// Log.i("APP", "Saving state! (" + hashCode() + ") " + bundle);
	}

	@Override
	public void loadState(Bundle bundle)
	{
		this.loadedState = bundle;

		// Log.i("APP", "Loading state! (" + hashCode() + ") " + bundle);
	}
}
