package com.penoaks.helpers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface DataReceiver
{
	String getReferenceUri();

	void onCancelled(DatabaseError databaseError);

	void onDataReceived(DataSnapshot dataSnapshot);
}
