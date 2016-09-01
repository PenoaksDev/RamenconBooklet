package com.ramencon.data.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramencon.R;
import com.ramencon.models.ModelEvent;
import com.ramencon.models.ModelLocation;
import com.ramencon.ui.ScheduleViewFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScheduleAdapter implements ExpandableListAdapter
{
	public static final SimpleDateFormat DISPLAY_FORMAT_DATE = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat DISPLAY_FORMAT_TIME12 = new SimpleDateFormat("hh:mm a");
	public static final SimpleDateFormat DISPLAY_FORMAT_TIME24 = new SimpleDateFormat("HH:mm");

	private Context context;
	public SimpleDateFormat dateFormat;
	public SimpleDateFormat timeFormat;
	private LayoutInflater inflater = null;
	public List<ModelLocation> locations;
	public List<ModelEvent> events;

	public ScheduleAdapter(Context context, SimpleDateFormat dateFormat, SimpleDateFormat timeFormat, List<ModelLocation> locations, List<ModelEvent> events)
	{
		this.context = context;
		this.dateFormat = dateFormat;
		this.timeFormat = timeFormat;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.locations = locations;
		this.events = events;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer)
	{

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer)
	{

	}

	@Override
	public int getGroupCount()
	{
		return events.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return groupPosition * childPosition;
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public View getGroupView(final int position, boolean isExpanded, View convertView, ViewGroup parent)
	{
		View rowView = inflater.inflate(R.layout.listview_item, null);

		TextView tv_title = (TextView) rowView.findViewById(R.id.title);
		TextView tv_time = (TextView) rowView.findViewById(R.id.time);
		TextView tv_location = (TextView) rowView.findViewById(R.id.location);

		ModelEvent event = events.get(position);

		for (ModelLocation loc : locations)
			if (loc.id.equals(event.location))
				event.location = loc.title;

		ScheduleViewFragment.adapter = this;

		try
		{
			// Date date = dateFormat.parse(event.date);
			Date from = timeFormat.parse(event.time);
			Date to = new Date(from.getTime() + (60000 * Integer.parseInt(event.duration)));

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			boolean use24h = pref.getBoolean("pref_military", false);

			tv_title.setText(event.title);

			String format_from = use24h ? DISPLAY_FORMAT_TIME24.format(from) : DISPLAY_FORMAT_TIME12.format(from);
			String format_to = use24h ? DISPLAY_FORMAT_TIME24.format(to) : DISPLAY_FORMAT_TIME12.format(to);
			tv_time.setText(format_from + " — " + format_to);
			tv_location.setText(event.location);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		/*
		rowView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				HomeActivity.instance.setFragment(ScheduleViewFragment.newInstance(position), true);
			}
		});
		*/

		return rowView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		View childView = inflater.inflate(R.layout.scheduleview_child, null);

		TextView tv_description = (TextView) childView.findViewById(R.id.description);

		ModelEvent event = events.get(groupPosition);

		final ImageView iv_heart = (ImageView) childView.findViewById(R.id.heart);

		iv_heart.setSelected(event.isHearted());

		iv_heart.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				startPopAnimation(v, true);
			}
		});

		final ImageView iv_alarm = (ImageView) childView.findViewById(R.id.alarm);

		iv_alarm.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				startPopAnimation(v, false);
			}
		});

		tv_description.setText(event.description);

		return childView;
	}

	public void startPopAnimation(final View v, final boolean overpop)
	{
		Animation pop = AnimationUtils.loadAnimation(context, R.anim.icon_shinkout);

		pop.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				v.setVisibility(View.INVISIBLE);

				v.setSelected(!v.isSelected());

				Animation pop = AnimationUtils.loadAnimation(context, v.isSelected() ? (overpop ? R.anim.icon_popout : R.anim.icon_shinkin) : R.anim.icon_shinkin);

				pop.setAnimationListener(new Animation.AnimationListener()
				{
					@Override
					public void onAnimationStart(Animation animation)
					{

					}

					@Override
					public void onAnimationEnd(Animation animation)
					{
						v.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{

					}
				});

				v.startAnimation(pop);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});

		v.startAnimation(pop);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return false;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public void onGroupExpanded(int groupPosition)
	{

	}

	@Override
	public void onGroupCollapsed(int groupPosition)
	{

	}

	@Override
	public long getCombinedChildId(long groupId, long childId)
	{
		return 0;
	}

	@Override
	public long getCombinedGroupId(long groupId)
	{
		return groupId;
	}
}
