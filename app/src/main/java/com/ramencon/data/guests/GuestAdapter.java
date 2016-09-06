package com.ramencon.data.guests;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ramencon.R;
import com.ramencon.data.ListGroup;
import com.ramencon.data.models.ModelGuest;
import com.ramencon.ui.GuestViewFragment;
import com.ramencon.ui.HomeActivity;
import com.squareup.picasso.Picasso;

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

		ListGroup listGroup = list.get(groupPosition);
		final ModelGuest guest = listGroup.children.get(childPosition);

		final ImageView iv_thumbnail = (ImageView) listItemView.findViewById(R.id.guest_thumbnail);
		final TextView tv_title = (TextView) listItemView.findViewById(R.id.guest_title);

		HomeActivity.storageReference.child("images/guests/" + listGroup.id + "/" + guest.image).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
		{
			@Override
			public void onComplete(@NonNull Task<Uri> task)
			{
				if (task.isSuccessful())
					Picasso.with(context).load(task.getResult()).into(iv_thumbnail);
				else
					iv_thumbnail.setImageResource(R.drawable.error);
			}
		});

		tv_title.setText(guest.title);

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
