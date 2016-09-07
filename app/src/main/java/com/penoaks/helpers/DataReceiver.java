package com.penoaks.helpers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface DataReceiver
{
	enum DataEvent
	{
		CHILD_ADDED, CHILD_CHANGED, CHILD_REMOVED, CHILD_MOVED, CHANGED
	}

	String getReferenceUri();

	void onDataError(DatabaseError databaseError);

	void onDataReceived(DataSnapshot dataSnapshot);

	void onDataEvent(DataEvent event, DataSnapshot dataSnapshot);
}
