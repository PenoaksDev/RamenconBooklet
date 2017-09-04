package io.amelia.booklet.data;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.data.BoundData;

public class MapsHandler extends ContentHandler
{
	public List<MapsMapModel> mapsMapModels;

	@Override
	public String getSectionKey()
	{
		return "maps";
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		mapsMapModels = new ArrayList<>();
		for ( BoundData boundData : data.getBoundDataList( "maps" ) )
		{
			MapsMapModel mapsMapModel = new MapsMapModel();
			mapsMapModel.id = boundData.getString( "id" );
			mapsMapModel.image = boundData.getString( "image" );
			mapsMapModel.title = boundData.getString( "title" );
			mapsMapModels.add( mapsMapModel );
		}
	}
}
