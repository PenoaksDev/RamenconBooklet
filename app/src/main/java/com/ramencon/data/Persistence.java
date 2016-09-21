package com.ramencon.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.penoaks.helpers.FirebaseUtil;
import com.penoaks.log.PLog;
import com.penoaks.sepher.Configuration;
import com.penoaks.sepher.ConfigurationSection;
import com.penoaks.sepher.OnConfigurationListener;
import com.penoaks.sepher.types.json.JsonConfiguration;
import com.ramencon.RamenApp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Persistence implements ValueEventListener, OnConfigurationListener
{
	private static Persistence instance;

	public static boolean isInstigated()
	{
		return instance != null;
	}

	public static Persistence getInstance()
	{
		if (instance == null)
			instance = new Persistence();
		return instance;
	}

	private static final List<String> uris = new ArrayList<>();

	private final FirebaseDatabase database = FirebaseDatabase.getInstance();
	private final Map<String, DatabaseReference> references = new ConcurrentHashMap<>();
	private final JsonConfiguration root = new JsonConfiguration();
	private final File persistenceFile;
	private boolean loading = false;

	public Persistence()
	{
		instance = this;

		persistenceFile = new File(RamenApp.cacheDir, "PersistenceData.json");
		root.options().pathSeparator('/');

		try
		{
			if (persistenceFile.exists())
				root.load(persistenceFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		root.addListener(this);

		// Init DatabaseReferences
		for (String uri : uris)
		{
			DatabaseReference ref = database.getReference(uri);
			ref.addValueEventListener(this);
			references.put(uri, ref);
		}
	}

	public void destroy()
	{
		for (String uri : uris)
			if (references.containsKey(uri))
				references.remove(uri).removeEventListener(this);
		instance = null;
	}

	public static void keepPersistent(String uri, boolean uploadChanges)
	{
		if (!uri.startsWith("/"))
			uri = "/" + uri;

		for (String u : uris)
		{
			if (u.equals(uri) || uri.startsWith(u))
				return;
			if (u.startsWith(uri))
			{
				if (instance != null && instance.references.containsKey(u))
					instance.references.remove(u).removeEventListener(instance);
				uris.remove(u);
			}
		}

		uris.add(uri);

		if (instance != null)
		{
			DatabaseReference ref = instance.database.getReference(uri);
			ref.addValueEventListener(instance);
			instance.references.put(uri, ref);
		}
	}

	public ConfigurationSection get()
	{
		return get(null);
	}

	public ConfigurationSection get(String path)
	{
		if (path == null || path.isEmpty())
			return root;

		ConfigurationSection section = root.getConfigurationSection(path, true);

		if (section == null)
			section = root.createSection(path);

		assert section != null;

		return section;
	}

	public void renew()
	{
		synchronized (references)
		{
			for (DatabaseReference ref : references.values())
				ref.removeEventListener(this);
			references.clear();
			for (String uri : uris)
			{
				DatabaseReference ref = database.getReference(uri);
				ref.addValueEventListener(this);
				references.put(uri, ref);
			}
		}
	}

	public boolean check()
	{
		for (String uri : uris)
			if (root.get(uri, null) == null && root.getConfigurationSection(uri, true).getKeys().size() > 0)
				return false;
		return true;
	}

	public void save()
	{
		synchronized (root)
		{
			try
			{
				root.save(persistenceFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDataChange(DataSnapshot dataSnapshot)
	{
		loading = true;

		String uri = FirebaseUtil.getUri(dataSnapshot, true);
		ConfigurationSection section = root.getConfigurationSection(uri, true);

		FirebaseUtil.convertDataSnapshotToSection(dataSnapshot, section);

		save();

		loading = false;
	}

	// @Override
	public void onChildAdded(DataSnapshot dataSnapshot, String s)
	{
			/*PLog.i("Child Added");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
	}

	// @Override
	public void onChildChanged(DataSnapshot dataSnapshot, String s)
	{
			/*PLog.i("Child Changed");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
	}

	// @Override
	public void onChildRemoved(DataSnapshot dataSnapshot)
	{
			/*PLog.i("Child Removed");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
	}

	// @Override
	public void onChildMoved(DataSnapshot dataSnapshot, String s)
	{
			/*PLog.i("Child Moved");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
	}

	@Override
	public void onCancelled(DatabaseError databaseError)
	{
		PLog.e("Firebase Database Error: " + databaseError.getMessage() + " --> " + databaseError.getDetails());
	}

	@Override
	public void onSectionAdd(ConfigurationSection section)
	{

	}

	@Override
	public void onSectionRemove(ConfigurationSection parent, ConfigurationSection orphanedChild)
	{
		if (loading)
			return;

		database.getReference(orphanedChild.getCurrentPath()).removeValue();

		save();
	}

	@Override
	public void onSectionChange(ConfigurationSection parent, String affectedKey)
	{
		if (loading)
			return;

		database.getReference(parent.getCurrentPath() + "/" + affectedKey).setValue(parent.get(affectedKey));

		save();
	}

	public void factoryReset()
	{
		destroy();
		persistenceFile.delete();
	}
}
