package com.ramencon.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.penoaks.data.DataLoadingFragment;
import com.penoaks.fragments.PersistentFragment;
import com.penoaks.helpers.Formatting;
import com.penoaks.log.PLog;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.R;
import com.ramencon.data.models.ModelEvent;
import com.ramencon.data.schedule.ScheduleAdapter;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.data.schedule.ScheduleDayAdapter;
import com.ramencon.data.schedule.filters.DefaultScheduleFilter;

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ScheduleFragment extends DataLoadingFragment implements PersistentFragment, SwipeRefreshLayout.OnRefreshListener
{
	public static final long ONEDAY = 1440 * 60 * 1000;

	private static ScheduleFragment instance = null;

	public static ScheduleFragment instance()
	{
		return instance;
	}

	public DefaultScheduleFilter currentFilter = new DefaultScheduleFilter();
	private ScheduleDayAdapter scheduleDayAdapter;
	private ScheduleAdapter scheduleAdapter;
	private ExpandableListView mListView;

	public ScheduleDataReceiver receiver = new ScheduleDataReceiver();
	private Bundle savedState = null;

	public ScheduleFragment()
	{
		instance = this;
		setReceiver(receiver);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getActivity().setTitle("Convention Schedule");
		setHasOptionsMenu(true);
	}

	@Override
	public void saveState(Bundle bundle)
	{
		if (getView() == null)
			return;

		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.schedule_listview);

		bundle.putString("filter", DefaultScheduleFilter.getAdapter().toJson(currentFilter));

		if (lv != null)
		{
			bundle.putInt("listViewPositionVisible", lv.getFirstVisiblePosition());
			bundle.putInt("listViewPositionOffset", lv.getChildAt(0) == null ? 0 : lv.getChildAt(0).getTop() - lv.getPaddingTop());
		}

		if (scheduleDayAdapter != null)
			bundle.putInt("selectedPosition", scheduleDayAdapter.getSelectedPosition());
	}

	@Override
	public void loadState(Bundle bundle)
	{
		this.savedState = bundle;
	}

	@Override
	public void onRefresh()
	{
		savedState = new Bundle();
		saveState(savedState);

		super.refreshData();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		menu.add("Show Next Events").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				try
				{
					List<Date> days = receiver.sampleDays();
					long nowTime = new Date().getTime();

					assert days.size() > 0;

					if (days.get(0).getTime() > nowTime)
					{
						Toast.makeText(getContext(), "Sorry, Ramencon has not started yet... err, hurray... Ramencon is starting soon!", Toast.LENGTH_LONG).show();
						return true;
					}

					if (days.get(days.size() - 1).getTime() + ONEDAY < nowTime)
					{
						Toast.makeText(getContext(), "Sorry, Ramencon is over. :( I hope you had a great time at the convention!", Toast.LENGTH_LONG).show();
						return true;
					}

					int dayPosition = 0;

					SimpleDateFormat sdf = new SimpleDateFormat("MM d yyyy");
					String now = sdf.format(new Date());
					for (int i = 0; i < days.size(); i++)
						if (sdf.format(days.get(i)).equals(now))
							dayPosition = i;

					currentFilter.reset();
					currentFilter.setMin(days.get(dayPosition).getTime());
					currentFilter.setMax(days.get(dayPosition).getTime() + ONEDAY);

					TreeSet<ModelEvent> data = receiver.filterRange(currentFilter);

					int positionVisible = -1;

					long nowDate = new Date().getTime();
					ModelEvent[] events = data.toArray(new ModelEvent[0]);
					for (int i = 0; i < events.length; i++)
					{
						Long l = events[i].getEndTime();
						if (l > nowDate)
						{
							positionVisible = i;
							break;
						}
					}

					scheduleDayAdapter.setSelectedPosition(dayPosition + 1, new ArrayList<>(data), Formatting.date(ScheduleDayAdapter.DATEFORMAT, days.get(dayPosition)));

					if (positionVisible > 0)
						mListView.setSelectionFromTop(positionVisible, 0);

					return true;
				}
				catch (ParseException e)
				{
					e.printStackTrace();
					Toast.makeText(getContext(), "Sorry, there was a problem!", Toast.LENGTH_LONG).show();
				}

				return true;
			}
		});
	}

	@Override
	public void onDataReceived(ConfigurationSection section, boolean isUpdate)
	{
		if (isUpdate)
			return;

		final View root = getView();

		SwipeRefreshLayout refresher = (SwipeRefreshLayout) root.findViewById(R.id.schedule_refresher);
		refresher.setOnRefreshListener(this);
		refresher.setColorSchemeColors(R.color.colorPrimary, R.color.colorAccent, R.color.lighter_gray);
		refresher.setRefreshing(false);

		try
		{
			assert receiver.schedule.size() > 0;

			List<Date> days = receiver.sampleDays();

			TwoWayView mDayView = (TwoWayView) root.findViewById(R.id.daylist);
			TextView mDateDisplay = (TextView) root.findViewById(R.id.date_display);

			assert mDayView != null;
			assert mDateDisplay != null;
			assert days.size() > 0;

			mListView = (ExpandableListView) root.findViewById(R.id.schedule_listview);

			int selectedPosition = 1;
			TreeSet<ModelEvent> data;
			int positionVisible = 0;
			int positionOffset = 0;

			currentFilter.reset();

			if (savedState == null || savedState.isEmpty())
			{
				int dayPosition = -1;

				SimpleDateFormat sdf = new SimpleDateFormat("MM d yyyy");
				String now = sdf.format(new Date());
				for (int i = 0; i < days.size(); i++)
					if (sdf.format(days.get(i)).equals(now))
						dayPosition = i;

				if (dayPosition >= 0)
				{
					currentFilter.setMin(days.get(dayPosition).getTime());
					currentFilter.setMax(days.get(dayPosition).getTime() + ONEDAY);
					selectedPosition = dayPosition + 1;
				}
				else if (receiver.hasHeartedEvents())
				{
					currentFilter.setHearted(DefaultScheduleFilter.TriStateList.SHOW);
					selectedPosition = 0;
				}
				else
				{
					currentFilter.setMin(days.get(0).getTime());
					currentFilter.setMax(days.get(0).getTime() + ONEDAY);
					selectedPosition = 1;
				}

				data = receiver.filterRange(currentFilter);

				if (dayPosition >= 0)
				{
					// Find the most recent event and set the scroll position
					long nowDate = new Date().getTime();
					ModelEvent[] events = data.toArray(new ModelEvent[0]);
					for (int i = 0; i < events.length; i++)
					{
						Long l = events[i].getEndTime();
						if (l > nowDate)
						{
							positionVisible = i;
							break;
						}
					}
				}
			}
			else
			{
				try
				{
					currentFilter = DefaultScheduleFilter.getAdapter().fromJson(savedState.getString("filter"));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				selectedPosition = savedState.getInt("selectedPosition", 0);
				data = receiver.filterRange(currentFilter);

				positionVisible = savedState.getInt("listViewPositionVisible", 0);
				positionOffset = savedState.getInt("listViewPositionOffset", 0);

				savedState = null;
			}

			scheduleDayAdapter = new ScheduleDayAdapter(this, days, mDateDisplay, receiver, mListView, mDayView, selectedPosition);
			mDayView.setAdapter(scheduleDayAdapter);

			scheduleAdapter = new ScheduleAdapter(getActivity(), receiver.simpleDateFormat(), receiver.simpleTimeFormat(), receiver.locations, new ArrayList<>(data));
			mListView.setAdapter(scheduleAdapter);

			if (positionVisible > 0)
				mListView.setSelectionFromTop(positionVisible, positionOffset);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_schedule, container, false);
	}
}
