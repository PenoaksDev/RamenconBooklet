package com.ramencon.data.maps;

import android.util.Log;

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
	public void onDataReceived(DataSnapshot dataSnapshot, boolean isUpdate)
	{
		if (isUpdate)
			return;

		maps.clear();

		for (DataSnapshot shot : dataSnapshot.getChildren())
			maps.add(shot.getValue(ModelMap.class));
	}

	@Override
	public void onDataError(DatabaseError databaseError)
	{

	}
}
