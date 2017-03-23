package com.ramencon.data;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

import com.penoaks.booklet.DataPersistence;
import com.penoaks.configuration.ConfigurationSection;

public abstract class DataAwareFragment<R extends DataReceiver> extends Fragment
{
	protected boolean started = false;
	protected R receiver = null;
	private ProgressDialog mDialog;

	public final void setReceiver( R receiver )
	{
		this.receiver = receiver;

		if ( started )
			receiver.attachFragment( this );
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if ( receiver != null )
			receiver.attachFragment( this );

		started = true;
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if ( receiver != null )
			receiver.detachFragment( this );

		started = false;
	}

	void loadStart()
	{
		mDialog = new ProgressDialog( getActivity() );
		mDialog.setMessage( "Loading Data..." );
		mDialog.setCancelable( false );
		mDialog.show();
	}

	void loadStop()
	{
		if ( mDialog != null )
			mDialog.cancel();
	}

	public final void refreshData()
	{
		DataPersistence.getInstance().forceUpdate( receiver.getReferenceUri() );
		receiver.rebuildData( true );
	}

	/**
	 * Called when data is received
	 *
	 * @param data      The Data Snapshot
	 * @param isRefresh Was this appended update
	 */
	protected abstract void onDataArrived( ConfigurationSection data, boolean isRefresh );

	/**
	 * Called when data is received
	 *
	 * @param data The Data Snapshot
	 */
	protected abstract void onDataUpdate( ConfigurationSection data );
}
