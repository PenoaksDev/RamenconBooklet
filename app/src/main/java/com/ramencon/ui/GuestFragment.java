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
import com.penoaks.helpers.DataReceiver;
import com.penoaks.helpers.PersistentFragment;
import com.ramencon.R;
import com.ramencon.data.guests.GuestAdapter;
import com.ramencon.data.guests.GuestDataReceiver;

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

		getActivity().setTitle("Guests and Vendors");
	}

	@Override
	public View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_guests, container, false);
	}

	@Override
	public void onDataReceived(DataSnapshot dataSnapshot, boolean isUpdate)
	{
		if (isUpdate)
			return;

		final View root = getView();

		SwipeRefreshLayout refresher = (SwipeRefreshLayout) root.findViewById(R.id.guests_refresher);
		refresher.setOnRefreshListener(this);
		refresher.setColorSchemeColors(R.color.colorPrimary, R.color.colorAccent, R.color.lighter_gray);
		refresher.setRefreshing(false);

		ExpandableListView lv = ((ExpandableListView) root.findViewById(R.id.guests_listview));
		lv.setAdapter(new GuestAdapter(getContext(), receiver.guests));

		if (savedState != null && !savedState.isEmpty())
		{
			lv.onRestoreInstanceState(savedState.getParcelable("listView"));

			if (savedState.getInt("listViewPositionVisible", -1) > 0)
				lv.setSelectionFromTop(savedState.getInt("listViewPositionVisible", 0), savedState.getInt("listViewPositionOffset", 0));

			savedState = null;
		}
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

		ExpandableListView lv = (ExpandableListView) getView().findViewById(R.id.guests_listview);

		if (lv != null)
		{
			bundle.putParcelable("listView", lv.onSaveInstanceState());
			bundle.putInt("listViewPositionVisible", lv.getFirstVisiblePosition());
			bundle.putInt("listViewPositionOffset", lv.getChildAt(0) == null ? 0 : lv.getChildAt(0).getTop() - lv.getPaddingTop());
		}
	}

	@Override
	public void loadState(Bundle bundle)
	{
		this.savedState = bundle;
	}

	@Override
	public void onDataError(DatabaseError databaseError)
	{
		throw databaseError.toException();
	}
}
