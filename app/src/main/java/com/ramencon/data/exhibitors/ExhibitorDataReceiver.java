package com.ramencon.data.exhibitors;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.penoaks.helpers.DataReceiver;
import com.ramencon.models.ModelExhibitor;

import java.util.List;

public class ExhibitorDataReceiver implements DataReceiver
{
	public List<ModelExhibitor> exhibitors;

	@Override
	public String getReferenceUri()
	{
		return "booklet-data/exhibitors";
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{

	}

	@Override
	public void onDataReceived(DataSnapshot data)
	{
		GenericTypeIndicator<List<ModelExhibitor>> tExhibitors = new GenericTypeIndicator<List<ModelExhibitor>>()
		{
		};

		exhibitors = data.child("exhibitors").getValue(tExhibitors);
	}
}
