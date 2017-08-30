package io.amelia.booklet.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuestsGroupModel
{
	public List<GuestsModel> children;
	public String id;
	public String title;
	public Map<String, String> resolvedImages = new HashMap<>();

	public GuestsGroupModel( String id, String title, List<GuestsModel> children )
	{
		this.id = id;
		this.title = title;
		this.children = children;
	}

	public GuestsModel getModel( String id )
	{
		if ( id == null )
			throw new IllegalArgumentException( "id can not be null" );

		for ( GuestsModel guest : children )
			if ( guest != null && id.equals( guest.id ) )
				return guest;
		return null;
	}
}
