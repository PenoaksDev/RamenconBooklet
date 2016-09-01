package com.penoaks.helpers;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
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

	public void setReceiver(DataReceiver receiver)
	{
		this.receiver = receiver;
		handleDataReceiver();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mDialog = new ProgressDialog(getActivity());
		mDialog.setMessage("Loading Data...");
		mDialog.setCancelable(false);
		mDialog.show();

		viewCreated = true;
		handleDataReceiver();

		return onPopulateView(inflater, container, savedInstanceState);
	}

	public void refreshData()
	{
		handleDataReceiver();
	}

	private void handleDataReceiver()
	{
		// Test that the prerequisites were met
		// DataManager was provided, DataReceiver is set, and view was created
		if (receiver != null && viewCreated)
			DataManager.instance().handleDataReceiver(receiver, this);
	}

	public abstract View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public void onDataLoaded0(DataSnapshot dataSnapshot)
	{
		mDialog.cancel();
		onDataLoaded(dataSnapshot);
	}

	public abstract void onDataLoaded(DataSnapshot dataSnapshot);

	public void onCancelled0(DatabaseError databaseError)
	{
		mDialog.cancel();
		onCancelled(databaseError);
	}

	public abstract void onCancelled(DatabaseError databaseError);
}
