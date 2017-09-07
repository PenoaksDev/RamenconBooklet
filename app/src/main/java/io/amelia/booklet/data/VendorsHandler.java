package io.amelia.booklet.data;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.data.BoundData;

public class VendorsHandler extends ContentHandler
{
	public List<VendorsGroupModel> vendors;

	public VendorsGroupModel getModel( String id )
	{
		for ( VendorsGroupModel group : vendors )
			if ( id.equals( group.id ) )
				return group;
		return null;
	}

	@Override
	public String getSectionKey()
	{
		return "vendors";
	}

	public VendorsModel getVendor( String groupId, String vendorId )
	{
		VendorsGroupModel vendor = getModel( groupId );
		if ( vendor == null )
			return null;
		return vendor.getModel( vendorId );
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		vendors = new ArrayList<>();
		for ( BoundData boundData : data.getBoundDataList( "vendors" ) )
		{
			List<VendorsModel> models = new ArrayList<>();
			for ( BoundData child : boundData.getBoundDataList( "data" ) )
			{
				VendorsModel vendorsModel = new VendorsModel();
				vendorsModel.id = child.getString( "id" );
				vendorsModel.image = child.getString( "image" );
				vendorsModel.title = child.getString( "title" );
				vendorsModel.urls = child.getString( "urls" );
				vendorsModel.description = child.getString( "description" );
				models.add( vendorsModel );
			}
			vendors.add( new VendorsGroupModel( boundData.getString( "id" ), boundData.getString( "title" ), models ) );
		}
	}
}
