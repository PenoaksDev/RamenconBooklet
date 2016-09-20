package com.ramencon.data;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.penoaks.sepher.ConfigurationSection;
import com.penoaks.sepher.OnConfigurationListener;

public abstract class DataLoadingFragment extends Fragment implements OnConfigurationListener
{
	private boolean started = false;
	private DataReceiver receiver = null;
	private ProgressDialog mDialog;
	private boolean autoLoadData = true;
	private ConfigurationSection data;

	public final void setAutoLoadData(boolean autoLoadData)
	{
		this.autoLoadData = autoLoadData;
	}

	public final void setReceiver(DataReceiver receiver)
	{
		this.receiver = receiver;
		if (autoLoadData)
			handleDataReceiver();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		mDialog = new ProgressDialog(getActivity());
		mDialog.setMessage("Loading Data...");
		mDialog.setCancelable(false);
		mDialog.show();

		started = true;
		if (autoLoadData)
			handleDataReceiver();
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (mDialog != null)
			mDialog.cancel();

		started = false;
	}

	public final void refreshData()
	{
		Persistence.getInstance().renew();
		handleDataReceiver(true);
	}

	private final void handleDataReceiver()
	{
		handleDataReceiver(false);
	}

	private final void handleDataReceiver(boolean isRefresh)
	{
		// Test that the prerequisites were met
		if (receiver != null && started)
		{
			if (data != null)
				data.removeListener(this);

			data = Persistence.getInstance().get(receiver.getReferenceUri());

			if (data != null)
				data.addListener(this);

			if (mDialog != null)
				mDialog.cancel();

			receiver.onDataReceived(data, isRefresh);
			onDataReceived(data, isRefresh);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		if (data != null)
			data.removeListener(this);
	}

	/**
	 * Called after a new section is added to tree
	 *
	 * @param section The new child section
	 */
	public final void onSectionAdd(ConfigurationSection section)
	{

	}

	/**
	 * Called after a section is removed from the tree
	 *
	 * @param parent        The parent of the removed child
	 * @param orphanedChild The orphaned child before it's left for the GC
	 */
	public final void onSectionRemove(ConfigurationSection parent, ConfigurationSection orphanedChild)
	{

	}

	/**
	 * Called after a section has been altered, i.e., values added, removed, or changed.
	 *
	 * @param parent      The parent section
	 * @param affectedKey The altered key
	 */
	public final void onSectionChange(ConfigurationSection parent, String affectedKey)
	{

	}

	/*public void onCancelled(DatabaseError databaseError)
	{
		// TODO REUSE?

		mDialog.cancel();
		receiver.onDataError(databaseError);
		onDataError(databaseError);
	}*/

	/**
	 * Called when data is received
	 *
	 * @param data     The Data Snapshot
	 * @param isUpdate Was this appended update
	 */
	protected abstract void onDataReceived(ConfigurationSection data, boolean isRefresh);
}
