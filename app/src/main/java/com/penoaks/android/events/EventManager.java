package com.penoaks.android.events;

import com.penoaks.android.lang.ReportingLevel;
import com.penoaks.android.log.PLog;
import com.penoaks.android.utils.UtilObjects;
import com.penoaks.booklet.AppService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager
{
	private static Map<Class<? extends AbstractEvent>, EventHandlers> handlers = new ConcurrentHashMap<>();

	private Object lock = new Object();
	private AppService app;

	public EventManager( AppService app )
	{
		this.app = app;
	}

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 */
	public <T extends AbstractEvent> T callEvent( T event )
	{
		try
		{
			if ( event.isAsynchronous() )
			{
				if ( Thread.holdsLock( lock ) )
					throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from inside synchronized code." );
				if ( app.isPrimaryThread() )
					throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from primary server thread." );
				fireEvent( event );
			}
			else
				synchronized ( lock )
				{
					fireEvent( event );
				}
		}
		catch ( EventException ex )
		{
			// Ignore
		}

		return event;
	}

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 * @throws EventException Thrown if you try to call an async event on a sync thread
	 */
	public <T extends AbstractEvent> T callEventWithException( T event ) throws EventException
	{
		if ( event.isAsynchronous() )
		{
			if ( Thread.holdsLock( lock ) )
				throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from inside synchronized code." );
			if ( app.isPrimaryThread() )
				throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from primary server thread." );
			fireEvent( event );
		}
		else
			synchronized ( lock )
			{
				fireEvent( event );
			}

		return event;
	}

	public Map<Class<? extends AbstractEvent>, Set<RegisteredListener>> createRegisteredListeners( EventListener listener, final EventRegistrar registrar )
	{
		UtilObjects.notNull( registrar, "Context can not be null" );
		UtilObjects.notNull( listener, "Listener can not be null" );

		Map<Class<? extends AbstractEvent>, Set<RegisteredListener>> ret = new HashMap<>();
		Set<Method> methods;
		try
		{
			Method[] publicMethods = listener.getClass().getMethods();
			methods = new HashSet<>( publicMethods.length, Float.MAX_VALUE );
			for ( Method method : publicMethods )
				methods.add( method );
			for ( Method method : listener.getClass().getDeclaredMethods() )
				methods.add( method );
		}
		catch ( NoClassDefFoundError e )
		{
			PLog.e( String.format( "%s has failed to register events for %s because %s does not exist.", registrar.getName(), listener.getClass(), e.getMessage() ) );
			return ret;
		}

		for ( final Method method : methods )
		{
			final EventHandler eh = method.getAnnotation( EventHandler.class );
			if ( eh == null )
				continue;
			final Class<?> checkClass;
			if ( method.getParameterTypes().length != 1 || !AbstractEvent.class.isAssignableFrom( checkClass = method.getParameterTypes()[0] ) )
			{
				PLog.e( registrar.getName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass() );
				continue;
			}
			final Class<? extends AbstractEvent> eventClass = checkClass.asSubclass( AbstractEvent.class );
			method.setAccessible( true );
			Set<RegisteredListener> eventSet = ret.get( eventClass );
			if ( eventSet == null )
			{
				eventSet = new HashSet<>();
				ret.put( eventClass, eventSet );
			}

			if ( ReportingLevel.E_DEPRECATED.isEnabled() )
				for ( Class<?> clazz = eventClass; AbstractEvent.class.isAssignableFrom( clazz ); clazz = clazz.getSuperclass() )
				{
					if ( clazz.isAnnotationPresent( Deprecated.class ) )
					{
						PLog.w( String.format( "The creator '%s' has registered a listener for %s on method '%s', but the event is Deprecated!", registrar.getName(), clazz.getName(), method.toGenericString() ) );
						break;
					}
				}

			EventExecutor executor = new EventExecutor()
			{
				@Override
				public void execute( EventListener listener, AbstractEvent event ) throws EventException
				{
					try
					{
						if ( !eventClass.isAssignableFrom( event.getClass() ) )
							return;
						method.invoke( listener, event );
					}
					catch ( InvocationTargetException ex )
					{
						throw new EventException( ex.getCause() );
					}
					catch ( Throwable t )
					{
						throw new EventException( t );
					}
				}
			};

			eventSet.add( new RegisteredListener( listener, executor, eh.priority(), registrar, eh.ignoreCancelled() ) );
		}
		return ret;
	}

	private void fireEvent( AbstractEvent event ) throws EventException
	{
		for ( RegisteredListener registration : getEventListeners( event.getClass() ) )
		{
			if ( !registration.getRegistrar().isEnabled() )
				continue;

			try
			{
				registration.callEvent( event );
			}
			catch ( EventException ex )
			{
				if ( ex.getCause() == null )
				{
					ex.printStackTrace();
					PLog.e( "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName() + "\nEvent Exception Reason: " + ex.getMessage() );
				}
				else
				{
					ex.getCause().printStackTrace();
					PLog.e( "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName() + "\nEvent Exception Reason: " + ex.getCause().getMessage() );
				}
				throw ex;
			}
			catch ( Throwable ex )
			{
				PLog.e( "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName(), ex );
			}
		}
	}

	private EventHandlers getEventListeners( Class<? extends AbstractEvent> event )
	{
		EventHandlers eventHandlers = handlers.get( event );

		if ( eventHandlers == null )
		{
			eventHandlers = new EventHandlers();
			handlers.put( event, eventHandlers );
		}

		return eventHandlers;
	}

	public void registerEvent( Class<? extends AbstractEvent> event, EventListener listener, EventPriority priority, EventExecutor executor, EventRegistrar registrar )
	{
		registerEvent( event, listener, priority, executor, registrar, false );
	}

	/**
	 * Registers the given event to the specified listener using a directly passed EventExecutor
	 *
	 * @param event           Event class to register
	 * @param listener        EventListener to register
	 * @param priority        Priority of this event
	 * @param executor        EventExecutor to register
	 * @param registrar       The Object Context
	 * @param ignoreCancelled Do not call executor if event was already cancelled
	 */
	public void registerEvent( Class<? extends AbstractEvent> event, EventListener listener, EventPriority priority, EventExecutor executor, EventRegistrar registrar, boolean ignoreCancelled )
	{
		UtilObjects.notNull( listener, "Listener cannot be null" );
		UtilObjects.notNull( priority, "Priority cannot be null" );
		UtilObjects.notNull( executor, "Executor cannot be null" );
		UtilObjects.notNull( registrar, "Creator cannot be null" );

		getEventListeners( event ).register( new RegisteredListener( listener, executor, priority, registrar, ignoreCancelled ) );
	}

	public void registerEvents( EventListener listener, EventRegistrar context )
	{
		for ( Map.Entry<Class<? extends AbstractEvent>, Set<RegisteredListener>> entry : createRegisteredListeners( listener, context ).entrySet() )
			getEventListeners( entry.getKey() ).registerAll( entry.getValue() );
	}

	public void unregisterEvents( EventRegistrar creator )
	{
		EventHandlers.unregisterAll( creator );
	}

	public void unregisterEvents( EventListener listener )
	{
		EventHandlers.unregisterAll( listener );
	}
}
