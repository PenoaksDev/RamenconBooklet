package io.amelia.booklet.data.guests;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.data.BoundData;
import io.amelia.android.data.DataReceiver;

public class GuestDataReceiver extends DataReceiver
{
	private static GuestDataReceiver instance = null;

	public static GuestDataReceiver getInstance()
	{
		if ( instance == null )
			instance = new GuestDataReceiver();
		return instance;
	}

	public List<ModelGroup> guests;

	private GuestDataReceiver()
	{
		super( true );
	}

	@Override
	public String getReferenceUri()
	{
		return "guests";
	}

	@Override
	protected void onDataUpdate( BoundData data )
	{

	}

	@Override
	public void onDataArrived( BoundData data, boolean isRefresh )
	{
		guests = new ArrayList<>();
		for ( ConfigurationSection section : data.getConfigurationSections() )
		{
			List<ModelGuest> models = new ArrayList<>();
			if ( section.has( "data" ) )
				models = section.getObjectList( "data", ModelGuest.class );

			guests.add( new ModelGroup( section.getString( "id" ), section.getString( "title" ), models ) );
		}

		/* for (ModelGroup group : guests)
		{
			PLog.i("Got Group: " + group.id);
			for (ModelGuest guest : group.children)
				PLog.i("---> Got Guest: " + guest.id);
		}*/
	}

	public ModelGroup getModel( String id )
	{
		for ( ModelGroup group : guests )
			if ( id.equals( group.id ) )
				return group;
		return null;
	}

	public ModelGuest getGuest( String groupId, String guestId )
	{
		ModelGroup group = getModel( groupId );
		if ( group == null )
			return null;
		return group.getModel( guestId );
	}
}
