package io.amelia.booklet.data;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.data.BoundData;

public class GuestsHandler extends ContentHandler
{
	public List<GuestsGroupModel> guests;

	public GuestsModel getGuest( String groupId, String guestId )
	{
		GuestsGroupModel group = getModel( groupId );
		if ( group == null )
			return null;
		return group.getModel( guestId );
	}

	public GuestsGroupModel getModel( String id )
	{
		for ( GuestsGroupModel group : guests )
			if ( id.equals( group.id ) )
				return group;
		return null;
	}

	@Override
	public String getSectionKey()
	{
		return "guests";
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		guests = new ArrayList<>();
		for ( BoundData boundData : data.getBoundDataList( "guests" ) )
		{
			List<GuestsModel> models = new ArrayList<>();
			for ( BoundData child : boundData.getBoundDataList( "data" ) )
			{
				GuestsModel guestsModel = new GuestsModel();
				guestsModel.id = child.getString( "id" );
				guestsModel.image = child.getString( "image" );
				guestsModel.title = child.getString( "title" );
				guestsModel.description = child.getString( "description" );
				models.add( guestsModel );
			}
			guests.add( new GuestsGroupModel( boundData.getString( "id" ), boundData.getString( "title" ), models ) );
		}

		/* for (ModelGroup group : guests)
		{
			PLog.i("Got Group: " + group.id);
			for (ModelGuest guest : group.children)
				PLog.i("---> Got Guest: " + guest.id);
		}*/
	}
}
