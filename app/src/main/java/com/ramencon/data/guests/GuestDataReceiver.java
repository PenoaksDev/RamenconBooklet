package com.ramencon.data.guests;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.penoaks.helpers.DataReceiver;
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
	public void onDataReceived(DataSnapshot data, boolean isUpdate)
	{
		if (isUpdate)
			return;

		GenericTypeIndicator<List<ModelGuest>> tGuests = new GenericTypeIndicator<List<ModelGuest>>()
		{
		};

		guests = new ArrayList<>();

		for (DataSnapshot child : data.getChildren())
			guests.add(new ListGroup(child.child("id").getValue(String.class), child.child("title").getValue(String.class), child.child("data") == null || !child.child("data").hasChildren() ? new ArrayList<ModelGuest>() : child.child("data").getValue(tGuests)));
	}

	@Override
	public void onDataError(DatabaseError databaseError)
	{

	}
}
