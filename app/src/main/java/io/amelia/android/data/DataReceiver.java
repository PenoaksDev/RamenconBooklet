package io.amelia.android.data;

import java.util.HashSet;
import java.util.Set;

import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.configuration.OnConfigurationListener;

public abstract class DataReceiver implements OnConfigurationListener
{
	private Set<DataAwareFragment> fragments = new HashSet<>();

	private ConfigurationSection data = null;

	public DataReceiver( boolean autoload )
	{
		if ( autoload )
			rebuildData( false );
	}

	public void rebuildData( boolean isRefresh )
	{
		for ( DataAwareFragment fragment : fragments )
			fragment.loadStart();

		if ( data != null )
			data.removeListener( this );

		data = null; // DataPersistence.getInstance().config( getReferenceUri() );

		onDataArrived( data, isRefresh );

		for ( DataAwareFragment fragment : fragments )
			fragment.onDataArrived( data, isRefresh );

		data.addListener( this );

		for ( DataAwareFragment fragment : fragments )
			fragment.loadStop();
	}

	public void attachFragment( DataAwareFragment fragment )
	{
		fragments.add( fragment );

		if ( data != null )
			fragment.onDataArrived( data, false );
	}

	public void detachFragment( DataAwareFragment fragment )
	{
		fragments.remove( fragment );
	}

	/**
	 * Called after a new section is added to tree
	 *
	 * @param section The new child section
	 */
	public final void onSectionAdd( ConfigurationSection section )
	{

	}

	/**
	 * Called after a section is removed from the tree
	 *
	 * @param parent        The parent of the removed child
	 * @param orphanedChild The orphaned child before it's left for the GC
	 */
	public final void onSectionRemove( ConfigurationSection parent, ConfigurationSection orphanedChild )
	{

	}

	/**
	 * Called after a section has been altered, i.e., values added, removed, or changed.
	 *
	 * @param parent      The parent section
	 * @param affectedKey The altered key
	 */
	public final void onSectionChange( ConfigurationSection parent, String affectedKey )
	{
		if ( data != null ) // Safe Check
		{
			for ( DataAwareFragment fragment : fragments )
				fragment.onDataUpdate( data );
			onDataUpdate( data );
		}
	}

	/**
	 * Returns the Reference Uri
	 *
	 * @return The uri to request, root is used on null.
	 */
	protected abstract String getReferenceUri();

	/**
	 * Called when data is received
	 *
	 * @param data      The Data Snapshot
	 * @param isRefresh Was this appended update
	 */
	protected abstract void onDataArrived( ConfigurationSection data, boolean isRefresh );

	/**
	 * Called when data is received
	 *
	 * @param data The Data Snapshot
	 */
	protected abstract void onDataUpdate( ConfigurationSection data );
}
