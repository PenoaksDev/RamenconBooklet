package com.ramencon.data.models;

public class ModelEvent
{
	public String id;
	public String title;
	public String date;
	public String time;
	public String duration;
	public String location;
	public String description;

	// private DatabaseReference ref = null;
	// private DataSnapshot data = null;

	public void resolveDataSnapshot()
	{
		//ref = SigninActivity.getUserReference().child("events/" + id);
		//ref.addValueEventListener(this);
	}

	public boolean isHearted()
	{
		return false;
		/*if ( data == null )
			return false;
		DataSnapshot child = data.child("hearted");
		return child == null || child.getValue() == null ? false : child.getValue(Boolean.class);*/
	}

	public void setHearted(boolean hearted)
	{
		//assert ref != null;
		//ref.child("hearted").setValue(hearted);
	}

	/*@Override
	public void onDataChange(DataSnapshot dataSnapshot)
	{
		data = dataSnapshot;
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		throw databaseError.toException();
	}*/
}
