package com.ramencon.data;

import com.penoaks.sepher.ConfigurationSection;

public interface DataReceiver
{
	/**
	 * Returns the Reference Uri
	 *
	 * @return The uri to request, root is used on null.
	 */
	String getReferenceUri();

	/**
	 * Called when data is received
	 *
	 * @param data The Data Snapshot
	 * @param isUpdate Was this appended update
	 */
	void onDataReceived(ConfigurationSection data, boolean isUpdate);
}
