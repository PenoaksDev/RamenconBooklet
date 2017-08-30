package io.amelia.booklet.data;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.data.BoundData;

public class GuideHandler extends ContentHandler
{
	public List<MapsMapModel> maps;

	@Override
	public String getSectionKey()
	{
		return "guide";
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		maps = new ArrayList<>();
		for ( BoundData boundData : data.getBoundDataList( "guide" ) )
		{
			MapsMapModel mapsMapModel = new MapsMapModel();
			mapsMapModel.id = boundData.getString( "id" );
			mapsMapModel.image = boundData.getString( "image" );
			mapsMapModel.title = boundData.getString( "title" );
			maps.add( mapsMapModel );
		}
	}
}
