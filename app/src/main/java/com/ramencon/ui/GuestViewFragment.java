package com.ramencon.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.koushikdutta.ion.Ion;
import com.ramencon.R;
import com.ramencon.data.ListGroup;
import com.ramencon.data.guests.GuestAdapter;
import com.ramencon.data.models.ModelGuest;

public class GuestViewFragment extends Fragment
{
	public static GuestAdapter adapter = null;

	public static Fragment instance(int groupPosition, int childPosition)
	{
		Bundle bundle = new Bundle();
		bundle.putInt("groupPosition", groupPosition);
		bundle.putInt("childPosition", childPosition);

		GuestViewFragment frag = new GuestViewFragment();
		frag.setArguments(bundle);
		return frag;
	}

	public GuestViewFragment()
	{

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().setTitle("View Guest");

		View root = inflater.inflate(R.layout.fragment_guest_view, container, false);

		Bundle bundle = getArguments();

		ListGroup listGroup = adapter.getListGroup(bundle.getInt("groupPosition"));
		ModelGuest guest = listGroup.children.get(bundle.getInt("childPosition"));

		final ImageView image = (ImageView) root.findViewById(R.id.guest_view_image);
		TextView tv_title = (TextView) root.findViewById(R.id.guest_view_title);
		// TextView tv_desc = (TextView) root.findViewById(R.id.guest_view_description);
		WebView tv_desc = (WebView) root.findViewById(R.id.guest_view_description);

		tv_title.setText(guest.getTitle());
		//tv_desc.setText(guest.getDescription());
		tv_desc.setBackgroundColor(root.getDrawingCacheBackgroundColor());
		tv_desc.loadData("<p style=\"text-align: justified;\">" + guest.getDescription() + "</p>", "text/html", "UTF-8");

		if (guest.image == null)
			image.setImageResource(R.drawable.noimagefound);
		else
			HomeActivity.storageReference.child("images/guests/" + listGroup.id + "/" + guest.image).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
			{
				@Override
				public void onComplete(@NonNull Task<Uri> task)
				{
					if (task.isSuccessful())
						Ion.with(GuestViewFragment.this).load(task.getResult().toString()).intoImageView(image);
					else
						image.setImageResource(R.drawable.error);
				}
			});

		return root;
	}
}
