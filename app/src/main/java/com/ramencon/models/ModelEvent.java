package com.ramencon.models;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ramencon.ui.SigninActivity;

public class ModelEvent implements ChildEventListener, ValueEventListener
{
	public String id;
	public String title;
	public String date;
	public String time;
	public String duration;
	public String location;
	public String description;

	private DatabaseReference ref = null;
	private DataSnapshot data = null;

	public void resolveDataSnapshot()
	{
		ref = SigninActivity.getUserReference().child("events/" + id);
		ref.addValueEventListener(this);
		ref.addChildEventListener(this);
	}

	public boolean isHearted()
	{
		if ( data == null )
			return false;
		DataSnapshot child = data.child("hearted");
		return child == null || child.getValue() == null ? false : child.getValue(Boolean.class);
	}

	public void setHearted(boolean hearted)
	{
		assert ref != null;
		ref.child("hearted").setValue(hearted);
	}

	@Override
	public void onChildAdded(DataSnapshot dataSnapshot, String s)
	{
		data = dataSnapshot;
	}

	@Override
	public void onChildChanged(DataSnapshot dataSnapshot, String s)
	{
		data = dataSnapshot;
	}

	@Override
	public void onChildRemoved(DataSnapshot dataSnapshot)
	{
		data = dataSnapshot;
	}

	@Override
	public void onChildMoved(DataSnapshot dataSnapshot, String s)
	{
		data = dataSnapshot;
	}

	@Override
	public void onDataChange(DataSnapshot dataSnapshot)
	{
		data = dataSnapshot;
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		throw databaseError.toException();
	}
}
