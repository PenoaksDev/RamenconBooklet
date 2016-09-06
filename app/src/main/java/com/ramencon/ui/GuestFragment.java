package com.ramencon.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.penoaks.helpers.DataLoadingFragment;
import com.penoaks.helpers.PersistentFragment;
import com.ramencon.R;
import com.ramencon.data.ListGroup;
import com.ramencon.data.guests.GuestAdapter;
import com.ramencon.data.guests.GuestDataReceiver;

import java.util.ArrayList;
import java.util.List;

public class GuestFragment extends DataLoadingFragment implements PersistentFragment, SwipeRefreshLayout.OnRefreshListener
{
	private static GuestFragment instance = null;

	public static GuestFragment instance()
	{
		if (instance == null)
			instance = new GuestFragment();
		return instance;
	}

	public GuestDataReceiver receiver = new GuestDataReceiver();
	private Bundle savedState = null;

	public GuestFragment()
	{
		instance = this;
		setReceiver(receiver);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getActivity().setTitle("Guests");
	}

	@Override
	public View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_guests, container, false);
	}

	@Override
	public void onDataLoaded(DataSnapshot dataSnapshot)
	{
		final View root = getView();

		SwipeRefreshLayout refresher = (SwipeRefreshLayout) root.findViewById(R.id.guests_refresher);
		refresher.setOnRefreshListener(this);
		refresher.setColorSchemeColors(R.color.colorPrimary, R.color.colorAccent, R.color.lighter_gray);
		refresher.setRefreshing(false);

		List<ListGroup> list = new ArrayList<>();

		list.add(new ListGroup("Special Guests", receiver.guests));
		list.add(new ListGroup("DJ Guests", receiver.guests));
		list.add(new ListGroup("Cosplay Guests", receiver.guests));

		((ExpandableListView) root.findViewById(R.id.guests_listview)).setAdapter(new GuestAdapter(getContext(), list));
	}

	@Override
	public void onRefresh()
	{
		savedState = new Bundle();
		saveState(savedState);

		super.refreshData();
	}

	@Override
	public void saveState(Bundle bundle)
	{
		if (getView() == null)
			return;
	}

	@Override
	public void loadState(Bundle bundle)
	{
		this.savedState = bundle;
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		throw databaseError.toException();
	}
}
