package com.ramencon.data.maps;

import com.ramencon.data.DataReceiver;
import com.penoaks.sepher.ConfigurationSection;
import com.ramencon.data.models.ModelMap;

import java.util.ArrayList;
import java.util.List;

public class MapsDataReceiver implements DataReceiver
{
	public List<ModelMap> maps = new ArrayList<>();

	@Override
	public String getReferenceUri()
	{
		return "booklet-data/maps";
	}

	@Override
	public void onDataReceived(ConfigurationSection data, boolean isUpdate)
	{
		if (isUpdate)
			return;

		maps.clear();

		for (ConfigurationSection section : data.getConfigurationSections())
			maps.add((ModelMap) section.asObject(ModelMap.class));
	}
}
