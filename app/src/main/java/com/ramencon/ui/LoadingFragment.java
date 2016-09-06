package com.ramencon.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramencon.R;

public class LoadingFragment extends Fragment
{
	private static LoadingFragment instance;

	public static LoadingFragment instance()
	{
		if (instance == null)
			instance = new LoadingFragment();
		return instance;
	}

	public LoadingFragment()
	{
		instance = this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_loading, container, false);
	}
}
