package com.ramencon.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.penoaks.helpers.DataLoadingFragment;
import com.penoaks.helpers.PersistentFragment;
import com.ramencon.R;
import com.ramencon.data.schedule.ScheduleAdapter;
import com.ramencon.data.schedule.ScheduleDataReceiver;
import com.ramencon.data.schedule.ScheduleDayAdapter;
import com.ramencon.data.schedule.filters.DefaultScheduleFilter;
import com.ramencon.models.ModelEvent;

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ScheduleFragment extends DataLoadingFragment implements PersistentFragment, SwipeRefreshLayout.OnRefreshListener
{
	private static ScheduleFragment instance = null;

	public static ScheduleFragment instance()
	{
		return instance;
	}

	public DefaultScheduleFilter currentFilter = new DefaultScheduleFilter();

	private Bundle savedState = null;
	private ScheduleDayAdapter scheduleDayAdapter;
	private ScheduleAdapter scheduleAdapter;
	public ScheduleDataReceiver receiver = new ScheduleDataReceiver();

	public ScheduleFragment()
	{
		instance = this;
		setReceiver(receiver);
	}

	@Override
	public void saveState(Bundle bundle)
	{
		if (getView() == null)
			return;

		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.schedule_listview);

		bundle.putString("filter", DefaultScheduleFilter.getAdapter().toJson(currentFilter));

		if (lv != null)
			bundle.putInt("listViewPosition", lv.getFirstVisiblePosition());

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
	public void onDataLoaded(DataSnapshot dataSnapshot)
	{
		final View root = getView();

		try
		{
			SwipeRefreshLayout refresher = (SwipeRefreshLayout) root.findViewById(R.id.schedule_refresher);
			refresher.setOnRefreshListener(this);
			refresher.setColorSchemeColors(R.color.colorPrimary, R.color.colorAccent, R.color.lighter_gray);
			refresher.setRefreshing(false);

			List<Date> days = receiver.sampleDays();

			TwoWayView mDayView = (TwoWayView) root.findViewById(R.id.daylist);
			TextView mDateDisplay = (TextView) root.findViewById(R.id.date_display);

			ExpandableListView lv = (ExpandableListView) root.findViewById(R.id.schedule_listview);

			int selectedPosition = 2;
			List<ModelEvent> data = null;
			int scrollTo = -1;

			if (savedState == null || savedState.isEmpty())
			{
				currentFilter.setMin(days.get(0).getTime());
				currentFilter.setMax(days.get(0).getTime() + (1440 * 60 * 1000)); // One Day

				data = receiver.filterRange(currentFilter);
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

				selectedPosition = savedState.getInt("selectedPosition", 2);
				data = receiver.filterRange(currentFilter);

				if (savedState.getInt("listViewPosition", -1) > 0)
					scrollTo = savedState.getInt("listViewPosition");

				savedState = null;
			}

			scheduleDayAdapter = new ScheduleDayAdapter(this, days, mDateDisplay, selectedPosition);
			mDayView.setAdapter(scheduleDayAdapter);

			scheduleAdapter = new ScheduleAdapter(getActivity(), receiver.simpleDateFormat(), receiver.simpleTimeFormat(), receiver.locations, data);
			lv.setAdapter(scheduleAdapter);

			if (scrollTo > 0)
				lv.setVerticalScrollbarPosition(scrollTo);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_schedule, container, false);
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		throw databaseError.toException();
	}

	public void loadList(List<ModelEvent> list)
	{
		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.schedule_listview);
		lv.setAdapter(new ScheduleAdapter(HomeActivity.instance, receiver.simpleDateFormat(), receiver.simpleTimeFormat(), receiver.locations, list));
	}
}
