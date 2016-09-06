package com.ramencon.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.penoaks.helpers.DataLoadingFragment;
import com.penoaks.helpers.PersistentFragment;
import com.ramencon.R;
import com.ramencon.data.schedule.ScheduleDataReceiver;

public class ExhibitorFragment extends DataLoadingFragment implements PersistentFragment, SwipeRefreshLayout.OnRefreshListener
{
	private static ExhibitorFragment instance = null;

	public static ExhibitorFragment instance()
	{
		if ( instance == null )
			instance = new ExhibitorFragment();
		return instance;
	}

	public ScheduleDataReceiver receiver = new ScheduleDataReceiver();
	private Bundle savedState = null;

	public ExhibitorFragment()
	{
		instance = this;
		setReceiver(receiver);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getActivity().setTitle("Exhibitors");
	}

	@Override
	public View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_exhibitors, container, false);
	}

	@Override
	public void onDataLoaded(DataSnapshot dataSnapshot)
	{
		final View root = getView();

		SwipeRefreshLayout refresher = (SwipeRefreshLayout) root.findViewById(R.id.exhibitors_refresher);
		refresher.setOnRefreshListener(this);
		refresher.setColorSchemeColors(R.color.colorPrimary, R.color.colorAccent, R.color.lighter_gray);
		refresher.setRefreshing(false);
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
