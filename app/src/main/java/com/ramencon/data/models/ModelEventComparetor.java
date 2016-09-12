package com.ramencon.data.models;

import java.util.Comparator;

public class ModelEventComparetor implements Comparator<ModelEvent>
{
	@Override
	public int compare(ModelEvent lhs, ModelEvent rhs)
	{
		int pos = (int) (lhs.getStartTime() - rhs.getStartTime());
		if (pos == 0)
			pos = (int) (lhs.getEndTime() - rhs.getEndTime());
		return pos;
	}

	@Override
	public boolean equals(Object object)
	{
		return object == this;
	}
}
