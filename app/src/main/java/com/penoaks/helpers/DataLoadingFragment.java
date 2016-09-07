package com.penoaks.helpers;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public abstract class DataLoadingFragment extends Fragment implements ChildEventListener, ValueEventListener
{
	private boolean viewCreated = false;
	private DataReceiver receiver = null;
	private ProgressDialog mDialog;
	private boolean autoLoadData = true;
	private boolean waiting = true;
	private DatabaseReference mDatabaseReference;

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
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		viewCreated = true;
		if (autoLoadData)
			handleDataReceiver();

		return onPopulateView(inflater, container, savedInstanceState);
	}

	public final void refreshData()
	{
		handleDataReceiver();
	}

	private final void handleDataReceiver()
	{
		// Test that the prerequisites were met
		if (receiver != null && viewCreated)
		{
			mDialog = new ProgressDialog(getActivity());
			mDialog.setMessage("Loading Data...");
			mDialog.setCancelable(false);
			mDialog.show();

			waiting = true;

			if ( mDatabaseReference != null )
			{
				mDatabaseReference.removeEventListener((ChildEventListener) this);
				mDatabaseReference.removeEventListener((ValueEventListener) this);
			}

			String refUri = receiver.getReferenceUri();
			mDatabaseReference = refUri == null || refUri.isEmpty() ? FirebaseDatabase.getInstance().getReference() : FirebaseDatabase.getInstance().getReference(refUri);

			mDatabaseReference.addValueEventListener(this);
			mDatabaseReference.addChildEventListener(this);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		mDatabaseReference.removeEventListener((ChildEventListener) this);
		mDatabaseReference.removeEventListener((ValueEventListener) this);
	}

	@Override
	public void onChildAdded(DataSnapshot dataSnapshot, String s)
	{
		onDataEvent0( DataReceiver.DataEvent.CHILD_ADDED, dataSnapshot );
	}

	@Override
	public void onChildChanged(DataSnapshot dataSnapshot, String s)
	{
		onDataEvent0( DataReceiver.DataEvent.CHILD_CHANGED, dataSnapshot );
	}

	@Override
	public void onChildRemoved(DataSnapshot dataSnapshot)
	{
		onDataEvent0( DataReceiver.DataEvent.CHILD_REMOVED, dataSnapshot );
	}

	@Override
	public void onChildMoved(DataSnapshot dataSnapshot, String s)
	{
		onDataEvent0( DataReceiver.DataEvent.CHILD_MOVED, dataSnapshot );
	}

	@Override
	public void onDataChange(DataSnapshot dataSnapshot)
	{
		onDataEvent0( DataReceiver.DataEvent.CHANGED, dataSnapshot );
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		mDialog.cancel();
		receiver.onDataError(databaseError);
		onDataError(databaseError);
	}

	public void onDataEvent0(DataReceiver.DataEvent event, DataSnapshot dataSnapshot)
	{
		mDialog.cancel();

		Log.i("APP", "Data Event: " + waiting + " " + event.name());

		if ( waiting )
		{
			waiting = false;
			receiver.onDataReceived(dataSnapshot);
			onDataReceived(dataSnapshot);
		}
		else
		{
			receiver.onDataEvent(event, dataSnapshot);
			onDataEvent(event, dataSnapshot);
		}
	}

	/**
	 * Called from within #onCreateView, used to inflate and populate the basic fragment outline.
	 * Loading the actual data should be done in the #onDataReceived method.
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	protected abstract View onPopulateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	/**
	 * Called when something changes in the remote real-time database
	 *
	 * @param event
	 * @param dataSnapshot
	 */
	protected abstract void onDataEvent(DataReceiver.DataEvent event, DataSnapshot dataSnapshot);

	/**
	 * Called when data is being waited on. First load.
	 *
	 * @param dataSnapshot
	 */
	protected abstract void onDataReceived(DataSnapshot dataSnapshot);

	/**
	 * Called when a DatabaseError is encountered
	 *
	 * @param databaseError
	 */
	protected abstract void onDataError(DatabaseError databaseError);
}
