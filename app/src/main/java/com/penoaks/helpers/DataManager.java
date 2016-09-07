package com.penoaks.helpers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

	public interface OnDataListener
	{
		void onConnected();

		void onError(DatabaseError error);
	}
}
