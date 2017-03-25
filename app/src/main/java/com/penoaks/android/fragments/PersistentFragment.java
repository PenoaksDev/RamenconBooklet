package com.penoaks.android.fragments;

import android.os.Bundle;

public interface PersistentFragment
{
	void saveState(Bundle bundle);

	void loadState(Bundle bundle);

	void refreshState();
}
