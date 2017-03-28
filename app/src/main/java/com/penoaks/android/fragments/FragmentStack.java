package com.penoaks.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.penoaks.android.log.PLog;
import com.penoaks.android.utils.UtilObjects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentStack
{
	private Map<Integer, Class<? extends Fragment>> registeredFragments = new HashMap<>();
	private Map<String, FragmentSaveState> states = new HashMap<>();

	private List<OnBackStackChangedListener> backstackListeners = new ArrayList<>();
	private List<FragmentSaveState> backstack = new ArrayList<>();

	private FragmentManager mgr;
	private int containerId;

	public FragmentStack( FragmentManager mgr, int containerId )
	{
		this.mgr = mgr;
		this.containerId = containerId;
	}

	public void registerFragment( Integer id, Class<? extends Fragment> classFrag )
	{
		registeredFragments.put( id, classFrag );
	}

	/**
	 * Save the FragmentStack state
	 *
	 * @param state The Bundle to save to
	 */
	public void saveInstanceState( Bundle state )
	{
		Bundle savedStates = new Bundle();
		for ( Map.Entry<String, FragmentSaveState> es : states.entrySet() )
			savedStates.putBundle( es.getKey(), es.getValue().saveState() );
		state.putBundle( "savedStates", savedStates );
	}

	public void loadInstanceState( Bundle state )
	{
		if ( state.get( "savedStates" ) != null )
		{
			Bundle savedStates = state.getBundle( "savedStates" );
			for ( String key : savedStates.keySet() )
			{
				if ( !states.containsKey( key ) )
				{
					Bundle bundle = savedStates.getBundle( key );

					FragmentSaveState save = new FragmentSaveState( ( Class<? extends Fragment> ) bundle.getSerializable( "fragmentClass" ) );
					save.state = bundle;
					save.init();
					save.loadState();
					states.put( key, save );

					if ( UtilObjects.castToBool( bundle.getString( "visible" ) ) )
					{
						PLog.i( "Showing Active Fragment: " + key );
						save.show( false );
					}
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
	public FragmentSaveState loadFragment( Class<? extends Fragment> classFrag )
	{
		FragmentSaveState state = loadFragment( classFrag.getSimpleName() );
		if ( state == null )
		{
			state = new FragmentSaveState( classFrag );
			states.put( classFrag.getSimpleName(), state );
			state.init();
		}
		return state;
	}

	public FragmentSaveState loadFragment( String fragKey )
	{
		return states.containsKey( fragKey ) ? states.get( fragKey ) : null;
	}

	public boolean setFragmentById( int id )
	{
		return setFragmentById( id, false );
	}

	public boolean setFragmentById( int id, boolean withBack )
	{
		if ( registeredFragments.containsKey( id ) )
		{
			setFragment( registeredFragments.get( id ), withBack );
			return true;
		}

		return false;
	}

	public void setFragment( Class<? extends Fragment> classFrag )
	{
		setFragment( classFrag, false );
	}

	public void setFragment( Class<? extends Fragment> classFrag, boolean withBack )
	{
		FragmentSaveState state = loadFragment( classFrag );
		if ( !state.isVisible() )
			state.show( withBack );
	}

	public void setFragment( Fragment fragment, boolean withBack )
	{
		setFragment( fragment, withBack, false );
	}

	/**
	 * Existing fragment will config reused
	 *
	 * @param fragment The new fragment
	 * @param withBack Allow back?
	 * @param force    Replace existing fragment
	 */
	public void setFragment( Fragment fragment, boolean withBack, boolean force )
	{
		FragmentSaveState state;
		if ( states.containsKey( fragment.getClass().getSimpleName() ) )
		{
			state = states.get( fragment.getClass().getSimpleName() );
			if ( state.fragment == null || force )
				state.fragment = fragment;
		}
		else
		{
			state = new FragmentSaveState( fragment );
			states.put( fragment.getClass().getSimpleName(), state );
		}

		if ( state.isVisible() )
			return; // Has this fragment already been made visible?

		state.show( withBack );
	}

	public boolean hasBackstack()
	{
		return backstack.size() > 0;
	}

	public void popBackstack()
	{
		if ( backstack.size() == 0 )
			return;

		FragmentSaveState state = backstack.remove( 0 );
		state.show( false );

		for ( OnBackStackChangedListener listener : backstackListeners )
			listener.onBackStackChanged();
	}

	public void addOnBackStackChangedListener( OnBackStackChangedListener listener )
	{
		backstackListeners.add( listener );
	}

	public void removeOnBackStackChangedListener( OnBackStackChangedListener listener )
	{
		backstackListeners.remove( listener );
	}

	public void refreshFragment()
	{
		for ( FragmentSaveState state : states.values() )
			if ( state.isVisible() && state.fragment instanceof PersistentFragment )
				( ( PersistentFragment ) state.fragment ).refreshState();
	}

	public void clearFragments()
	{
		backstack.clear();
		states.clear();
	}

	public class FragmentSaveState
	{
		Class<? extends Fragment> clz;
		Bundle state = new Bundle();
		Fragment fragment;

		public FragmentSaveState( Class<? extends Fragment> clz )
		{
			this.clz = clz;
		}

		public FragmentSaveState( Fragment fragment )
		{
			this.clz = fragment.getClass();
			this.fragment = fragment;
		}

		public boolean isVisible()
		{
			Fragment frag = mgr.findFragmentByTag( clz.getSimpleName() );
			return frag != null && frag.isVisible();
		}

		public void show( boolean withBack )
		{
			if ( fragment == null )
				init();
			if ( fragment.isVisible() )
				return;

			// Move visible fragments to save state
			for ( FragmentSaveState state : states.values() )
				if ( state.fragment != null && state.fragment.isVisible() )
				{
					if ( withBack )
					{
						backstack.add( state );
						for ( OnBackStackChangedListener listener : backstackListeners )
							listener.onBackStackChanged();
					}

					state.saveState();
					break;
				}

			if ( state != null && !state.isEmpty() )
				loadState();

			FragmentTransaction trans = mgr.beginTransaction();
			trans.replace( containerId, fragment, fragment.getClass().getSimpleName() );
			// if (withBack)
			// trans.addToBackStack(fragment.getClass().getSimpleName());
			trans.commit();
		}

		public Bundle saveState()
		{
			if ( fragment != null && fragment instanceof PersistentFragment )
				( ( PersistentFragment ) fragment ).saveState( state );
			// TODO Make other ways to save a fragment state

			state.putSerializable( "fragmentClass", clz );
			state.putString( "visible", isVisible() ? "true" : "false" );

			Log.i( "APP", "Storing " + fragment.getClass().getSimpleName() + " -> " + state );

			return state;
		}

		public void loadState()
		{
			Log.i( "APP", "Loading " + fragment.getClass().getSimpleName() + " -> " + state );

			if ( state != null && fragment != null && fragment instanceof PersistentFragment )
				( ( PersistentFragment ) fragment ).loadState( state );
			// TODO Make other ways to load a fragment state
		}

		private Fragment init()
		{
			return init( false );
		}

		private Fragment init( boolean force )
		{
			PLog.i( "Init " + clz.getSimpleName() );

			if ( fragment != null && !force )
				return fragment;

			// Check FragmentManager for instance of fragment
			Fragment instance = mgr.findFragmentByTag( clz.getSimpleName() );

			if ( instance != null )
				Log.i( "APP", "Fragment was loaded by FragmentManager" );

			if ( instance == null )
				try
				{
					Method method = clz.getMethod( "instance" );
					if ( method.getReturnType().isAssignableFrom( Fragment.class ) )
					{
						Fragment frag = ( Fragment ) method.invoke( null );

						if ( frag != null )
							instance = frag;
					}
				}
				catch ( InvocationTargetException e )
				{
					throw new RuntimeException( "The fragment [" + clz.getName() + "] has thrown an exception.", e.getCause() == null ? e : e.getCause() );
				}
				catch ( Exception e )
				{
					Log.e( "APP", "Failed the invoke instance() method of fragment class [" + clz.getSimpleName() + "]" );
					// e.printStackTrace();
				}

			if ( instance == null )
				try
				{
					Constructor constructor = clz.getConstructor();
					instance = ( Fragment ) constructor.newInstance();
				}
				catch ( InvocationTargetException e )
				{
					throw new RuntimeException( "The fragment [" + clz.getName() + "] has thrown an exception.", e.getCause() == null ? e : e.getCause() );
				}
				catch ( Exception e )
				{
					Log.e( "APP", "Failed to invoke a zero argument constructor in fragment class [" + clz.getSimpleName() + "]" );
					e.printStackTrace();
				}

			if ( instance == null )
				throw new RuntimeException( "Failed to instigate the fragment [" + clz.getName() + "]" );

			this.fragment = instance;
			return instance;
		}
	}

	public interface OnBackStackChangedListener
	{
		/**
		 * Called whenever the contents of the back stack change.
		 */
		void onBackStackChanged();
	}
}
