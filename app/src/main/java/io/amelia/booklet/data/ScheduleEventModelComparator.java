package io.amelia.booklet.data;

import java.util.Comparator;

public class ScheduleEventModelComparator implements Comparator<ScheduleEventModel>
{
	@Override
	public int compare( ScheduleEventModel lhs, ScheduleEventModel rhs )
	{
		int pos = ( int ) ( lhs.getStartTime() - rhs.getStartTime() );
		if ( pos == 0 )
			pos = ( int ) ( lhs.getEndTime() - rhs.getEndTime() );
		return pos;
	}

	@Override
	public boolean equals( Object object )
	{
		return object == this;
	}
}
