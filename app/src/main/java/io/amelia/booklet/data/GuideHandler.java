package io.amelia.booklet.data;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.data.BoundData;

public class GuideHandler extends ContentHandler
{
	public List<GuidePageModel> guidePageModels;

	@Override
	public String getSectionKey()
	{
		return "guide";
	}

	@Override
	public void onSectionHandle( BoundData data, boolean isRefresh )
	{
		guidePageModels = new ArrayList<>();
		for ( BoundData boundData : data.getBoundDataList( "guide" ) )
		{
			GuidePageModel guidePageModel = new GuidePageModel();
			guidePageModel.pageNo = boundData.getString( "pageNo" );
			guidePageModel.image = boundData.getString( "image" );
			guidePageModels.add( guidePageModel );
		}
	}
}
