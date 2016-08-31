package com.penoaks.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FragmentStack
{
	class FragmentSaveState
	{
		Class<? extends Fragment> clz;
		Bundle state = new Bundle();
		Fragment fragment;

		public FragmentSaveState(Class<? extends Fragment> clz)
		{
			this.clz = clz;
		}

		public FragmentSaveState(Fragment fragment)
		{
			this.clz = fragment.getClass();
			this.fragment = fragment;
		}

		public boolean isCurrent()
		{
			Fragment frag = mgr.findFragmentByTag(clz.getSimpleName());
			return frag != null && frag.isVisible();
		}

		public void show(boolean withBack)
		{
			if (fragment == null)
				init();
			if (fragment.isVisible())
				return;

			// Instruct visible fragments to save state
			for( FragmentSaveState state : states.values() )
				if ( state.fragment != null && state.fragment.isVisible() )
					state.saveState();

			if ( state != null && !state.isEmpty() )
				loadState();

			FragmentTransaction trans = mgr.beginTransaction();
			trans.replace(containerId, fragment, fragment.getClass().getSimpleName());
			if (withBack)
				trans.addToBackStack(null);
			trans.commit();
		}

		public Bundle saveState()
		{
			if (fragment != null && fragment instanceof CryogenFragment)
				((CryogenFragment) fragment).saveState(state);
			// TODO Make other ways to save a fragment state

			state.putSerializable("fragmentClass", clz);
			return state;
		}

		public void loadState()
		{
			if (fragment != null && fragment instanceof CryogenFragment)
				((CryogenFragment) fragment).loadState(state);
			// TODO Make other ways to load a fragment state
		}

		private Fragment init()
		{
			return init(false);
		}

		private Fragment init(boolean force)
		{
			Log.i("APP", "Init " + clz.getSimpleName());

			if (fragment != null && !force)
				return fragment;

			// Check FragmentManager for instance of fragment
			Fragment instance = mgr.findFragmentByTag(clz.getSimpleName());

			if ( instance != null )
				Log.i("APP", "Fragment was loaded by FragmentManager");

			if (instance == null)
				try
				{
					Method method = clz.getMethod("instance");
					if (method.getReturnType().isAssignableFrom(Fragment.class))
					{
						Fragment frag = (Fragment) method.invoke(null);

						if (frag != null)
							instance = frag;
					}
				}
				catch (Exception e)
				{
					Log.e("APP", "Failed the invoke instance() method of fragment class [" + clz.getSimpleName() + "]");
					e.printStackTrace();
				}

			if (instance == null)
				try
				{
					Constructor constructor = clz.getConstructor();
					instance = (Fragment) constructor.newInstance();
				}
				catch (Exception e)
				{
					Log.e("APP", "Failed to invoke a zero argument constructor in fragment class [" + clz.getSimpleName() + "]");
					e.printStackTrace();
				}

			this.fragment = instance;
			return instance;
		}
	}

	private Map<Integer, Class<? extends Fragment>> registeredFragments = new HashMap<>();
	private Map<String, FragmentSaveState> states = new HashMap<>();

	private FragmentManager mgr;
	private int containerId;

	public FragmentStack(FragmentManager mgr, int containerId)
	{
		this.mgr = mgr;
		this.containerId = containerId;
	}

	public void registerFragment(Integer id, Class<? extends Fragment> classFrag)
	{
		registeredFragments.put(id, classFrag);
	}

	/**
	 * Save the FragmentStack state
	 *
	 * @param state The Bundle to save to
	 */
	public void saveInstanceState(Bundle state)
	{
		Bundle savedStates = new Bundle();
		for (Map.Entry<String, FragmentSaveState> es : states.entrySet())
		{
			Bundle save = es.getValue().saveState();
			Log.i("APP", "Storing Long-term " + es.getKey() + " -> " + save);
			savedStates.putBundle(es.getKey(), save);
		}
		state.putBundle("savedStates", savedStates);
	}

	public void loadInstanceState(Bundle state)
	{
		if (state.get("savedStates") != null)
		{
			Bundle savedStates = state.getBundle("savedStates");
			for (String key : savedStates.keySet())
			{
				if (!states.containsKey(key))
				{
					Log.i("APP", "Restoring Long-term " + key + " -> " + savedStates.getBundle(key));

					Bundle bundle = savedStates.getBundle(key);

					FragmentSaveState save = new FragmentSaveState((Class<? extends Fragment>) bundle.getSerializable("fragmentClass"));
					save.state = bundle;
					save.init();
					save.loadState();
					states.put(key, save);
				}
			}
		}
	}

	/**
	 * Loads a fragment
	 *
	 * @param classFrag The fragment class to be loaded
	 * @return The fragment save state
	 */
	public FragmentSaveState loadFragment(Class<? extends Fragment> classFrag)
	{
		FragmentSaveState state = states.containsKey(classFrag.getSimpleName()) ? states.get(classFrag.getSimpleName()) : new FragmentSaveState(classFrag);
		assert state != null;

		if (!states.containsKey(classFrag.getSimpleName()))
			states.put(classFrag.getSimpleName(), state);

		state.init();

		return state;
	}

	public boolean setFragmentById(int id)
	{
		return setFragmentById(id, false);
	}

	public boolean setFragmentById(int id, boolean withBack)
	{
		if (registeredFragments.containsKey(id))
		{
			setFragment(registeredFragments.get(id), withBack);
			return true;
		}

		return false;
	}

	public void setFragment(Class<? extends Fragment> classFrag)
	{
		setFragment(classFrag, false);
	}

	public void setFragment(Class<? extends Fragment> classFrag, boolean withBack)
	{
		FragmentSaveState state = loadFragment(classFrag);
		state.show(withBack);
	}

	public void setFragment(Fragment fragment, boolean withBack)
	{
		FragmentSaveState state;
		if (states.containsKey(fragment.getClass().getSimpleName()))
		{
			state = states.get(fragment.getClass().getSimpleName());
			if (state.fragment == null)
				state.fragment = fragment;
			else if (state.fragment != fragment)
				throw new RuntimeException("Double fragment instance detected!");
		}
		else
		{
			state = new FragmentSaveState(fragment);
			states.put(fragment.getClass().getSimpleName(), state);
		}

		if (state.isCurrent())
			return; // Has this fragment already been made visible?

		state.show(withBack);
	}
}
