package com.penoaks.helpers;

import android.os.Bundle;

public interface PersistentFragment
{
	void saveState(Bundle bundle);

	void loadState(Bundle bundle);
}
