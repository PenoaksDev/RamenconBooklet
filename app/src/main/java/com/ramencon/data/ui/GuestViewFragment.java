package com.ramencon.data.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ramencon.R;
import com.ramencon.data.guests.GuestAdapter;
import com.ramencon.models.ModelGuest;
import com.squareup.picasso.Picasso;

public class GuestViewFragment extends Fragment
{
	private static StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://ramencon-booklet.appspot.com");
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

		ModelGuest guest = adapter.getModelGuest(bundle.getInt("groupPosition"), bundle.getInt("childPosition"));

		final ImageView image = (ImageView) root.findViewById(R.id.guest_view_image);
		TextView tv_title = (TextView) root.findViewById(R.id.guest_view_title);
		TextView tv_desc = (TextView) root.findViewById(R.id.guest_view_description);

		tv_title.setText(guest.title);
		tv_desc.setText(guest.description);

		storageReference.child("images/guests/" + guest.image).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
		{
			@Override
			public void onComplete(@NonNull Task<Uri> task)
			{
				if (task.isSuccessful())
					Picasso.with(getContext()).load(task.getResult()).into(image);
				else
					image.setImageResource(R.drawable.error);
			}
		});

		return root;
	}
}
