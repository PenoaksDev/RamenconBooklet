package com.ramencon.data;

import com.ramencon.data.models.ModelGuest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListGroup
{
	public List<ModelGuest> children;
	public String id;
	public String title;
	public Map<String, String> resolvedImages = new HashMap<>();

	public ListGroup(String id, String title, List<ModelGuest> children)
	{
		this.id = id;
		this.title = title;
		this.children = children;
	}
}
