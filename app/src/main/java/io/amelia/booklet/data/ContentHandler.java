package io.amelia.booklet.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.amelia.android.data.BoundData;
import io.amelia.android.log.PLog;

public abstract class ContentHandler
{
	private final Map<String, ContentListener> listeners = new HashMap<>();
	private Booklet booklet;
	private BoundData data = null;
	private long lastUpdated = 0; // Never Updated

	protected final void attachListener( String key, ContentListener listener )
	{
		listeners.put( key.toLowerCase(), listener );

		if ( data != null )
			listener.sectionHandle( data, false );
	}

	protected final void detachListener( String key )
	{
		listeners.remove( key );
	}

	public final void forceRefresh() throws IOException
	{
		booklet.updateSectionHandler( this );
	}

	public final Booklet getBooklet()
	{
		return booklet;
	}

	protected final ContentHandler setBooklet( Booklet booklet )
	{
		this.booklet = booklet;
		return this;
	}

	public final BoundData getData()
	{
		return data;
	}

	public final long getLastUpdated()
	{
		return lastUpdated;
	}

	/**
	 * Returns the Reference Uri
	 *
	 * @return The uri to request, root is used on null.
	 */
	public abstract String getSectionKey();

	/**
	 * Called when data is received
	 *
	 * @param data      The section data
	 * @param isRefresh Was this appended update
	 */
	protected abstract void onSectionHandle( BoundData data, boolean isRefresh );

	/**
	 * Called after data has been altered
	 *
	 * @param data The Data snapshot
	 */
	protected final void sectionHandle( BoundData data, long lastUpdated )
	{
		this.lastUpdated = lastUpdated;
		this.data = data;

		PLog.i( "[Data Dump]\n" + data.dump() );

		onSectionHandle( data, lastUpdated > 0 );
		for ( ContentListener listener : listeners.values() )
			if ( listener.isEnabled() )
				listener.sectionHandle( data, lastUpdated > 0 );
	}

	public interface ContentListener
	{
		default boolean isEnabled()
		{
			return true;
		}

		void sectionHandle( BoundData data, boolean isRefresh );
	}
}
