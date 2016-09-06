package com.ramencon.data.guests;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.penoaks.helpers.DataReceiver;
import com.ramencon.models.ModelGuest;

import java.util.List;

public class GuestDataReceiver implements DataReceiver
{
	public List<ModelGuest> guests;

	@Override
	public String getReferenceUri()
	{
		return "booklet-data/guests";
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{

	}

	@Override
	public void onDataReceived(DataSnapshot data)
	{
		GenericTypeIndicator<List<ModelGuest>> tGuests = new GenericTypeIndicator<List<ModelGuest>>()
		{
		};

		guests = data.getValue(tGuests);
	}
}
