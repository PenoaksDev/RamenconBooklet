package com.ramencon.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.penoaks.helpers.PersistentFragment;

public class GuestFragment extends Fragment implements PersistentFragment, SwipeRefreshLayout.OnRefreshListener
{
	private static GuestFragment instance = null;

	public static GuestFragment instance()
	{
		return instance;
	}

	public GuestFragment()
	{
		instance = this;
	}

	@Override
	public void saveState(Bundle bundle)
	{

	}

	@Override
	public void loadState(Bundle bundle)
	{

	}

	@Override
	public void onRefresh()
	{

	}
}
