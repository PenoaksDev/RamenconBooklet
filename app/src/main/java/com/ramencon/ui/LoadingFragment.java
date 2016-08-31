package com.ramencon.ui;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramencon.R;
import com.ramencon.cache.CacheManager;

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

		new AsyncCacheChecker().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_loading, container, false);
	}

	private class AsyncCacheChecker extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			CacheManager.instance().isCached();
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			if (!CacheManager.instance().forkCacheDownload(getActivity(), false, new Runnable()
			{
				@Override
				public void run()
				{
					HomeActivity.instance.stacker.setFragmentById(R.id.nav_welcome);
				}
			}))
				HomeActivity.instance.stacker.setFragmentById(R.id.nav_welcome);
		}
	}
}
