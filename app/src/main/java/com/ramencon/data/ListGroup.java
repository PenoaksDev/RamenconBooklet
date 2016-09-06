package com.ramencon.data;

import com.ramencon.models.ModelGuest;

import java.util.List;

public class ListGroup
{
	public List<ModelGuest> children;
	public String title;

	public ListGroup(String title, List<ModelGuest> children)
	{
		this.title = title;
		this.children = children;
	}
}
