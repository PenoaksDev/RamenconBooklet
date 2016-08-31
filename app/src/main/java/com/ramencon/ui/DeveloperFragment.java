package com.ramencon.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramencon.R;
import com.ramencon.cache.CacheManager;

public class DeveloperFragment extends Fragment implements View.OnClickListener
{
	private static DeveloperFragment instance;

	public static DeveloperFragment instance()
	{
		if (instance == null)
			instance = new DeveloperFragment();
		return instance;
	}

	public DeveloperFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View root = inflater.inflate(R.layout.fragment_developer, container, false);

		root.findViewById(R.id.forceDownload).setOnClickListener(this);

		return root;
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.forceDownload)
			CacheManager.instance().forkCacheDownload(getActivity(), true);
	}
}
