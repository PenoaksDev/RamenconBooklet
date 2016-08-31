package com.ramencon.ui.schedule;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.penoaks.Formatting;
import com.ramencon.R;
import com.ramencon.ui.schedule.filters.DefaultScheduleFilter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduleDayAdapter extends BaseAdapter
{
	private LayoutInflater inflater = null;
	private ScheduleFragment parent;
	private List<Date> days = new ArrayList<>();
	private TextView mDateDisplay;
	private int selectedPosition;

	public ScheduleDayAdapter(ScheduleFragment parent, List<Date> days, TextView mDateDisplay, int selectedPosition)
	{
		this.inflater = (LayoutInflater) parent.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.parent = parent;
		this.days = days;
		this.mDateDisplay = mDateDisplay;
		this.selectedPosition = selectedPosition;
	}

	@Override
	public int getCount()
	{
		return days.size();// + 1;
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View view = inflater.inflate(R.layout.scheduleview_day, null);

		TextView dayName = (TextView) view.findViewById(R.id.day_name);
		TextView dayNumber = (TextView) view.findViewById(R.id.day_number);

		assert dayName != null;
		assert dayNumber != null;

		final Date day = days.get(position);

		dayName.setText(Formatting.date("EEE", day));
		dayNumber.setText(Formatting.date("d", day));

		if (position == selectedPosition)
		{
			view.setBackgroundColor(0xFFff4081);
			if (mDateDisplay != null)
				mDateDisplay.setText(Formatting.date("MMMM dx yyyy", day));
		}

		final ScheduleDayAdapter adapter = this;
		view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					DefaultScheduleFilter filter = ScheduleFragment.instance().currentFilter;
					filter.setMin(day.getTime());
					filter.setMax(day.getTime() + (1440 * 60 * 1000)); // One Day

					adapter.selectedPosition = position;

					adapter.parent.loadList(ScheduleFragment.instance().mgr.filterRange(filter));

					ViewGroup parent = (ViewGroup) v.getParent();
					for (int i = 0; i < parent.getChildCount(); i++)
						parent.getChildAt(i).setBackgroundColor(0x00000000);

					v.setBackgroundColor(0xFFff4081);

					if (mDateDisplay != null)
						mDateDisplay.setText(Formatting.date("MMMM dx yyyy", day));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		});

		return view;
	}

	public int getSelectedPosition()
	{
		return selectedPosition;
	}
}
