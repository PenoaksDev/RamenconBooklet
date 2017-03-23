package com.penoaks.booklet;

import com.google.firebase.auth.FirebaseAuth;
import com.penoaks.android.log.PLog;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppState implements AppTicks.EventReceiver
{
	private static AppState instance;

	public static AppState getInstance()
	{
		if ( instance == null )
			instance = new AppState();
		return instance;
	}

	private AppState()
	{
		AppTicks.getInstance().registerReceiver( "appState", this );
	}

	private Map<ListenerLevel, Set<WeakReference<Listener>>> listeners = new ConcurrentHashMap<>();
	private AppStates lastState = null;

	@Override
	public void onTick( long tick )
	{
		AppStates newState = DataPersistence.getInstance().eventState();

		if ( newState == AppStates.SUCCESS && FirebaseAuth.getInstance().getCurrentUser() == null )
			newState = AppStates.NO_USER;

		if ( newState != lastState )
		{
			PLog.i( "App State has changed from " + lastState + " to " + newState );

			lastState = newState;
			AppStateEvent event = new AppStateEvent( newState );

			for ( ListenerLevel level : ListenerLevel.values() )
			{
				Set<Listener> ls = getListeners( level );
				if ( ls.size() > 0 )
					for ( Listener listener : ls )
						listener.onPersistenceEvent( event );
			}

			if ( !event.isHandled )
				PLog.w( "The AppStateEvent went unhandled!" );
		}
	}

	@Override
	public void onException( Throwable cause )
	{
		cause.printStackTrace();
	}

	@Override
	public void onShutdown()
	{
		PLog.i( "Application is stopping!" );
	}

	public enum ListenerLevel
	{
		HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR
	}

	private Set<WeakReference<Listener>> getListenersRaw( ListenerLevel level )
	{
		if ( listeners.containsKey( level ) )
			return listeners.get( level );
		else
		{
			Set<WeakReference<Listener>> set = new HashSet<>();
			listeners.put( level, set );
			return set;
		}
	}

	private Set<Listener> getListeners( final ListenerLevel level )
	{
		return new HashSet<Listener>()
		{{
			Set<WeakReference<Listener>> set = getListenersRaw( level );
			for ( WeakReference<Listener> listener : set )
				if ( listener.get() == null )
					set.remove( listener );
				else
					add( listener.get() );
		}};
	}

	public void clearListeners()
	{
		listeners.clear();
	}

	public void addListener( ListenerLevel level, Listener listener )
	{
		if ( !AppTicks.isRunning() )
			PLog.e( "Listener was registered but AppTicks is not running!" );

		Set<WeakReference<Listener>> ls = getListenersRaw( level );
		ls.add( new WeakReference<>( listener ) );

		if ( lastState != null )
			listener.onPersistenceEvent( new AppStateEvent( lastState ) );
	}

	public interface Listener
	{
		void onPersistenceEvent( AppStateEvent event );
	}
}
