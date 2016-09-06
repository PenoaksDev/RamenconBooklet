package com.penoaks.helpers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.ramencon.ui.LoadingFragment;

public class DataManager
{
	private static DataManager instance;

	public static DataManager instance()
	{
		if (instance == null)
			instance = new DataManager();
		return instance;
	}

	private DataManager()
	{

	}

	private OnDataListener listener;

	public DataManager setListener(OnDataListener listener)
	{
		this.listener = listener;
		return this;
	}

	public void checkConnection()
	{
		final DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
		connectedRef.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				if (listener != null)
					listener.onConnected();
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				if (listener == null)
					throw databaseError.toException();
				else
					listener.onError(databaseError);
			}
		});
	}

	public void handleDataReceiver(final DataReceiver receiver)
	{
		handleDataReceiver(receiver, null);
	}

	public void handleDataReceiver(final DataReceiver receiver, final DataLoadingFragment fragment)
	{
		String refUri = receiver.getReferenceUri();
		DatabaseReference reference = refUri == null || refUri.isEmpty() ? FirebaseDatabase.getInstance().getReference() : FirebaseDatabase.getInstance().getReference(refUri);

		reference.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				receiver.onDataReceived(dataSnapshot);
				if (fragment != null)
					fragment.onDataLoaded0(dataSnapshot);
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				receiver.onCancelled(databaseError);
				if (fragment != null)
					fragment.onCancelled0(databaseError);
			}
		});
	}

	public interface OnDataListener
	{
		void onConnected();

		void onError(DatabaseError error);
	}
}
