package com.penoaks.helpers;

import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.penoaks.sepher.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirebaseUtil
{
	private FirebaseUtil()
	{

	}

	public static String getUri(DataSnapshot dataSnapshot)
	{
		return getUri(dataSnapshot, false);
	}

	public static String getUri(DataSnapshot dataSnapshot, boolean parentsOnly)
	{
		return getUri(dataSnapshot, parentsOnly, "/");
	}

	public static String getUri(DataSnapshot dataSnapshot, boolean parentsOnly, String glue)
	{
		List<String> parts = new ArrayList<>();
		DatabaseReference ref = dataSnapshot.getRef();

		if (!parentsOnly)
			parts.add(ref.getKey());

		while ((ref = ref.getParent()) != null)
			if (ref.getKey() != null)
				parts.add(ref.getKey());

		Collections.reverse(parts);
		return TextUtils.join(glue, parts);
	}

	public static void convertDataSnapshotToSection(DataSnapshot dataSnapshot, ConfigurationSection section)
	{
		if (dataSnapshot.hasChildren())
		{
			ConfigurationSection childSection = section.createSection(dataSnapshot.getKey());
			for (DataSnapshot snapshot : dataSnapshot.getChildren())
				convertDataSnapshotToSection(snapshot, childSection);
		}
		else
			section.set(dataSnapshot.getKey(), dataSnapshot.getValue());
	}
}