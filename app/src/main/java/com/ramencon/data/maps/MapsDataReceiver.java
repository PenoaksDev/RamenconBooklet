package com.ramencon.data.maps;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.penoaks.helpers.DataReceiver;
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
	public void onCancelled(DatabaseError databaseError)
	{

	}

	@Override
	public void onDataReceived(DataSnapshot dataSnapshot)
	{
		maps.clear();

		for (DataSnapshot d : dataSnapshot.getChildren())
			maps.add(d.getValue(ModelMap.class));
	}
}
