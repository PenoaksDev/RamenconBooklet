package com.ramencon.data;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.penoaks.sepher.ConfigurationSection;
import com.penoaks.sepher.OnConfigurationListener;

import static android.R.attr.data;

public abstract class DataAwareFragment<R extends DataReceiver> extends Fragment
{
	protected boolean started = false;
	protected R receiver = null;
	private ProgressDialog mDialog;

	public final void setReceiver(R receiver)
	{
		this.receiver = receiver;

		if (started)
			receiver.attachFragment(this);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		started = true;

		if (receiver != null)
			receiver.attachFragment(this);
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (receiver != null)
			receiver.detachFragment(this);

		started = false;
	}

	void loadStart()
	{
		mDialog = new ProgressDialog(getActivity());
		mDialog.setMessage("Loading Data...");
		mDialog.setCancelable(false);
		mDialog.show();
	}

	void loadStop()
	{
		if (mDialog != null)
			mDialog.cancel();
	}

	public final void refreshData()
	{
		Persistence.getInstance().renew();
		receiver.rebuildData(true);
	}

	/**
	 * Called when data is received
	 *
	 * @param data      The Data Snapshot
	 * @param isRefresh Was this appended update
	 */
	protected abstract void onDataArrived(ConfigurationSection data, boolean isRefresh);

	/**
	 * Called when data is received
	 *
	 * @param data The Data Snapshot
	 */
	protected abstract void onDataUpdate(ConfigurationSection data);
}
