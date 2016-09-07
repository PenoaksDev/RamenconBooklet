package com.penoaks.helpers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface DataReceiver
{
	/**
	 * Returns the Reference Uri
	 *
	 * @return The uri to request, root is used on null.
	 */
	String getReferenceUri();

	/**
	 * Called when a DatabaseError is encountered
	 *
	 * @param databaseError
	 */
	void onDataError(DatabaseError databaseError);

	/**
	 * Called when data is received
	 *
	 * @param dataSnapshot The Data Snapshot
	 * @param isUpdate Was this appended update
	 */
	void onDataReceived(DataSnapshot dataSnapshot, boolean isUpdate);
}
