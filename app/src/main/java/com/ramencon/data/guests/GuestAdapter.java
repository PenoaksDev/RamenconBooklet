package com.ramencon.data.guests;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.penoaks.helpers.ACRAHelper;
import com.ramencon.R;
import com.ramencon.data.ImageCache;
import com.ramencon.data.ListGroup;
import com.ramencon.data.models.ModelGuest;
import com.ramencon.ui.GuestViewFragment;
import com.ramencon.ui.HomeActivity;

import java.util.List;

public class GuestAdapter extends BaseExpandableListAdapter
{
	private LayoutInflater inflater = null;
	private Context context;

	private List<ListGroup> list;

	public GuestAdapter(Context context, List<ListGroup> list)
	{
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;

		assert list != null;
		this.list = list;

		GuestViewFragment.adapter = this;
	}

	@Override
	public int getGroupCount()
	{
		return list.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		List<ModelGuest> guests = list.get(groupPosition).children;
		return guests == null ? 0 : guests.size();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return childPosition;
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
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		View group = convertView == null ? inflater.inflate(R.layout.guest_listheader, null) : convertView;

		((TextView) group.findViewById(R.id.group_title)).setText(list.get(groupPosition).title);

		return group;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		View listItemView = convertView == null ? inflater.inflate(R.layout.guest_listitem, null) : convertView;

		final ListGroup listGroup = list.get(groupPosition);
		final ModelGuest guest = listGroup.children.get(childPosition);

		final ImageView iv_thumbnail = (ImageView) listItemView.findViewById(R.id.guest_thumbnail);
		final TextView tv_title = (TextView) listItemView.findViewById(R.id.guest_title);

		if (guest.image == null)
			iv_thumbnail.setImageResource(R.drawable.noimagefound);
		else
		{
			iv_thumbnail.setImageResource(R.drawable.loading_image);
			ImageCache.cacheRemoteImage("guest-" + guest.id, HomeActivity.storageReference.child("images/guests/" + listGroup.id + "/" + guest.image), false, new ImageCache.ImageFoundListener()
			{
				@Override
				public void update(Bitmap bitmap)
				{
					iv_thumbnail.setImageBitmap(bitmap);
				}

				@Override
				public void error(Exception exception)
				{
					ACRAHelper.handleExceptionOnce("loading_failure_" + listGroup.id + "_" + guest.image, new RuntimeException("Failed to load image from Google Firebase [images/guests/" + listGroup.id + "/" + guest.image + "]", exception));
					iv_thumbnail.setImageResource(R.drawable.error);
				}
			}, null);
		}

		tv_title.setText(guest.getTitle());

		listItemView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				HomeActivity.instance.stacker.setFragment(GuestViewFragment.instance(groupPosition, childPosition), true, true);
			}
		});

		return listItemView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	public ListGroup getListGroup(int groupPosition)
	{
		return list.get(groupPosition);
	}
}
