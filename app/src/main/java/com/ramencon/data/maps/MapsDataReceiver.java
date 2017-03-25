package com.ramencon.data.maps;

import com.penoaks.configuration.ConfigurationSection;
import com.ramencon.data.DataReceiver;
import com.ramencon.data.models.ModelMap;

import java.util.ArrayList;
import java.util.List;

public class MapsDataReceiver extends DataReceiver
{
	public List<ModelMap> maps;

	public MapsDataReceiver()
	{
		super( true );
	}

	@Override
	public String getReferenceUri()
	{
		return "booklet-data/maps";
	}

	@Override
	protected void onDataUpdate( ConfigurationSection data )
	{

	}

	@Override
	public void onDataArrived( ConfigurationSection data, boolean isRefresh )
	{
		maps = new ArrayList<>();
		for ( ConfigurationSection section : data.getConfigurationSections() )
			maps.add( section.asObject( ModelMap.class ) );
	}
}
