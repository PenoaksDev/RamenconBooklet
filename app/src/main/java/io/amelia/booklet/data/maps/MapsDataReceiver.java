package io.amelia.booklet.data.maps;

import java.util.ArrayList;
import java.util.List;

import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.data.DataReceiver;
import io.amelia.booklet.data.models.ModelMap;

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
