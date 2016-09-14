package com.ramencon.data.guests;

import com.ramencon.data.DataReceiver;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.data.ListGroup;
import com.ramencon.data.models.ModelGuest;

import java.util.ArrayList;
import java.util.List;

public class GuestDataReceiver implements DataReceiver
{
	public List<ListGroup> guests = new ArrayList<>();

	@Override
	public String getReferenceUri()
	{
		return "booklet-data/guests";
	}

	@Override
	public void onDataReceived(ConfigurationSection data, boolean isUpdate)
	{
		if (isUpdate)
			return;

		guests = new ArrayList<>();

		for (ConfigurationSection section : data.getConfigurationSections())
		{
			List<ModelGuest> models = new ArrayList<>();
			if (section.has("data"))
				models = section.getObjectList("data", ModelGuest.class);

			guests.add(new ListGroup(section.getString("id"), section.getString("title"), models));
		}
	}
}
