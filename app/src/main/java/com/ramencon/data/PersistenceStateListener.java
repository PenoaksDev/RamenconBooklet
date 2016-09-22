package com.ramencon.data;

public interface PersistenceStateListener
{
	void onPersistenceEvent(PersistenceStateChecker.StateResultEvent event);
}
