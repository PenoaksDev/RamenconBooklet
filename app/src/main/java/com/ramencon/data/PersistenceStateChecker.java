package com.ramencon.data;

import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.helpers.Network;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceStateChecker extends AsyncTask<Void, Void, Void>
{
	public enum ListenerLevel
	{
		HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR
	}

	private static Map<ListenerLevel, Set<WeakReference<PersistenceStateListener>>> listeners = new ConcurrentHashMap<>();
	private static PersistenceStateChecker instance = null;

	private int tryCounter = 0;

	public static PersistenceStateChecker makeInstance(boolean forceNew)
	{
		if (instance == null || forceNew)
		{
			if (instance != null && forceNew)
				instance.cancel(true);

			instance = new PersistenceStateChecker();
			instance.execute();
		}
		return instance;
	}

	public static void cancel()
	{
		if (instance != null)
			instance.cancel(true);
	}

	private static Set<WeakReference<PersistenceStateListener>> getListenersRaw(ListenerLevel level)
	{
		if (listeners.containsKey(level))
			return listeners.get(level);
		else
		{
			Set<WeakReference<PersistenceStateListener>> set = new HashSet<>();
			listeners.put(level, set);
			return set;
		}
	}

	private static Set<PersistenceStateListener> getListeners(final ListenerLevel level)
	{
		return new HashSet<PersistenceStateListener>()
		{{
			Set<WeakReference<PersistenceStateListener>> set = getListenersRaw(level);
			for (WeakReference<PersistenceStateListener> listener : set)
				if (listener.get() == null)
					set.remove(listener);
				else
					add(listener.get());
		}};
	}

	public static void addListener(ListenerLevel level, PersistenceStateListener listener)
	{
		Set<WeakReference<PersistenceStateListener>> ls = getListenersRaw(level);
		ls.add(new WeakReference<>(listener));
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		Persistence.getInstance().renew();
	}

	@Override
	protected void onPostExecute(Void aVoid)
	{
		super.onPostExecute(aVoid);

		if (isCancelled())
			return;

		if (notReady())
		{
			String msg;
			if (Persistence.getInstance().getLastFirebaseError() != null)
				msg = "We had an error with the remote google database, message: " + Persistence.getInstance().getLastFirebaseError().getMessage();
			else if (!Network.internetAvailable())
				msg = "We had a problem connecting to the internet. Please check your connection.";
			else if (FirebaseAuth.getInstance().getCurrentUser() == null)
				msg = "We had an authentication problem. Are you connected to the internet?";
			else
				msg = "We had a problem syncing with the remote database. Are you connected to the internet?";

			for (ListenerLevel level : ListenerLevel.values())
			{
				Set<PersistenceStateListener> ls = getListeners(level);
				if (ls.size() > 0)
				{
					for (PersistenceStateListener listener : ls)
						listener.onPersistenceError(msg);
					break;
				}
			}
		}
		else
			for (ListenerLevel level : ListenerLevel.values())
			{
				Set<PersistenceStateListener> ls = getListeners(level);
				if (ls.size() > 0)
				{
					for (PersistenceStateListener listener : ls)
						listener.onPersistenceReady();
					break;
				}
			}
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		while (notReady())
		{
			tryCounter++;

			if (tryCounter > 150 || isCancelled()) // 15 Seconds
				break;

			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean notReady()
	{
		return !Persistence.getInstance().check() || FirebaseAuth.getInstance().getCurrentUser() == null;
	}
}
