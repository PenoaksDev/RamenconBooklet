package com.penoaks.helpers;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public abstract class DataLoadingFragment extends Fragment
{
	private boolean viewCreated = false;
	private DataReceiver receiver = null;
	private ProgressDialog mDialog;
	private boolean autoLoadData = true;

	public void setAutoLoadData(boolean autoLoadData)
	{
		this.autoLoadData = autoLoadData;
	}

	public void setReceiver(DataReceiver receiver)
	{
		this.receiver = receiver;
		if (autoLoadData)
			handleDataReceiver();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mDialog = new ProgressDialog(getActivity());
		mDialog.setMessage("Loading Data...");
		mDialog.setCancelable(false);
		mDialog.show();

		viewCreated = true;
		if (autoLoadData)
			handleDataReceiver();

		return onPopulateView(inflater, container, savedInstanceState);
	}

	public void refreshData()
	{
		handleDataReceiver();
	}

	private final void handleDataReceiver()
	{
		// Test that the prerequisites were met
		// DataManager was provided, DataReceiver is set, and view was created
		if (receiver != null && viewCreated)
			DataManager.instance().handleDataReceiver(receiver, this);
	}

	public abstract View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public final void onDataLoaded0(DataSnapshot dataSnapshot)
	{
		mDialog.cancel();
		onDataLoaded(dataSnapshot);
	}

	public abstract void onDataLoaded(DataSnapshot dataSnapshot);

	public final void onCancelled0(DatabaseError databaseError)
	{
		mDialog.cancel();
		onCancelled(databaseError);
	}

	public abstract void onCancelled(DatabaseError databaseError);
}
