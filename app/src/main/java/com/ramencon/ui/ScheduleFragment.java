package com.ramencon.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.penoaks.helpers.DataLoadingFragment;
import com.penoaks.helpers.PersistentFragment;
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
	private ScheduleDayAdapter scheduleDayAdapter;
	private ScheduleAdapter scheduleAdapter;

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

			ExpandableListView lv = (ExpandableListView) root.findViewById(R.id.schedule_listview);

			int selectedPosition = 1;
			List<ModelEvent> data;
			int positionVisible = 0;
			int positionOffset = 0;

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

				selectedPosition = savedState.getInt("selectedPosition", 0);
				data = receiver.filterRange(currentFilter);

				positionVisible = savedState.getInt("listViewPositionVisible", 0);
				positionOffset = savedState.getInt("listViewPositionOffset", 0);

				savedState = null;
			}

			scheduleDayAdapter = new ScheduleDayAdapter(this, days, mDateDisplay, selectedPosition);
			mDayView.setAdapter(scheduleDayAdapter);

			scheduleAdapter = new ScheduleAdapter(getActivity(), receiver.simpleDateFormat(), receiver.simpleTimeFormat(), receiver.locations, data);
			lv.setAdapter(scheduleAdapter);

			if (positionVisible > 0)
				lv.setSelectionFromTop(positionVisible, positionOffset);
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

	public void loadList(List<ModelEvent> list)
	{
		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.schedule_listview);
		lv.setAdapter(new ScheduleAdapter(HomeActivity.instance, receiver.simpleDateFormat(), receiver.simpleTimeFormat(), receiver.locations, list));
	}
}
