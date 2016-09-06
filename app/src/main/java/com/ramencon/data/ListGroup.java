package com.ramencon.data;

import com.ramencon.data.models.ModelGuest;

import java.util.List;

public class ListGroup
{
	public List<ModelGuest> children;
	public String id;
	public String title;

	public ListGroup(String id, String title, List<ModelGuest> children)
	{
		this.id = id;
		this.title = title;
		this.children = children;
	}
}
