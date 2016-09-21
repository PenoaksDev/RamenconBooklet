package com.ramencon.data;

public interface PersistenceStateListener
{
	void onPersistenceError(String msg);

	void onPersistenceReady();
}
