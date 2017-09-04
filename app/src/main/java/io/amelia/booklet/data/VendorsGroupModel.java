package io.amelia.booklet.data;

import java.util.List;

public class VendorsGroupModel
{
	public List<VendorsModel> children;
	public String id;
	public String title;

	public VendorsGroupModel( String id, String title, List<VendorsModel> children )
	{
		this.id = id;
		this.title = title;
		this.children = children;
	}

	public VendorsModel getModel( String id )
	{
		if ( id == null )
			throw new IllegalArgumentException( "id can not be null" );

		for ( VendorsModel vendor : children )
			if ( vendor != null && id.equals( vendor.id ) )
				return vendor;
		return null;
	}
}
