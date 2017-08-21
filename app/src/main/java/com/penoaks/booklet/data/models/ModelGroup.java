package com.penoaks.booklet.data.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelGroup
{
	public List<ModelGuest> children;
	public String id;
	public String title;
	public Map<String, String> resolvedImages = new HashMap<>();

	public ModelGroup(String id, String title, List<ModelGuest> children)
	{
		this.id = id;
		this.title = title;
		this.children = children;
	}

	public ModelGuest getModel(String id)
	{
		if (id == null)
			throw new IllegalArgumentException("id can not be null");

		for (ModelGuest guest : children)
			if (guest != null && id.equals(guest.id))
				return guest;
		return null;
	}
}
