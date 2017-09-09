package io.amelia.booklet.data.filters;

import java.util.TreeSet;

import io.amelia.android.data.BoundData;
import io.amelia.booklet.data.ScheduleEventModel;

public class NoScheduleFilter implements ScheduleFilter
{
	public NoScheduleFilter()
	{

	}

	public NoScheduleFilter( BoundData boundData )
	{

	}

	@Override
	public BoundData encode()
	{
		BoundData boundData = new BoundData();
		boundData.put( "class", getClass().getName() );
		return boundData;
	}

	@Override
	public boolean filter( TreeSet<ScheduleEventModel> events, ScheduleEventModel event )
	{
		return true;
	}

	@Override
	public void reset()
	{

	}
}
