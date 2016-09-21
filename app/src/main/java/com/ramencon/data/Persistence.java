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

	private static JsonConfiguration root = new JsonConfiguration();
	private static final Map<String, UriChecker> uris = new ConcurrentHashMap();
	private static final File persistenceFile;

	private DatabaseError lastFirebaseError = null;

	public DatabaseError getLastFirebaseError()
	{
		return lastFirebaseError;
	}

	static
	{
		root.options().pathSeparator('/');

		persistenceFile = new File(RamenApp.cacheDir, "PersistenceData.json");

		try
		{
			if (persistenceFile.exists())
			{
				root.load(persistenceFile);
				PLog.i("Persistence file was loaded!");

				for (String key : root.getKeys())
					PLog.i("Key type: " + key + " --> " + root.get(key));
			}
			else
				PLog.i("Persistence file was missing!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private final FirebaseDatabase database = FirebaseDatabase.getInstance();
	private final Map<String, DatabaseReference> references = new ConcurrentHashMap<>();
	private boolean loading = false;

	public Persistence()
	{
		instance = this;

		root.addListener(this);

		for (String uri : uris.keySet())
		{
			DatabaseReference ref = database.getReference(uri);
			ref.addValueEventListener(this);
			references.put(uri, ref);
		}
	}

	public void destroy()
	{
		for (String uri : uris.keySet())
			if (references.containsKey(uri))
				references.remove(uri).removeEventListener(this);

		instance = null;
		root = new JsonConfiguration();
		root.options().pathSeparator('/');
	}

	public static void keepPersistent(String uri, UriChecker checker)
	{
		if (uri.startsWith("/"))
			uri = uri.substring(1);

		if (uri.isEmpty())
			throw new IllegalArgumentException("You can not keep the root uri persistent");

		for (String u : uris.keySet())
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

		uris.put(uri, checker);

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
			lastFirebaseError = null;
			for (DatabaseReference ref : references.values())
				ref.removeEventListener(this);
			references.clear();
			for (String uri : uris.keySet())
			{
				DatabaseReference ref = database.getReference(uri);
				ref.addValueEventListener(this);
				references.put(uri, ref);
			}
		}
	}

	public boolean check()
	{
		for (Map.Entry<String, UriChecker> entry : uris.entrySet())
			if (!entry.getValue().isLoaded(entry.getKey(), root) || root.get(entry.getKey()) == null)
				return false;

		return true;
	}

	public void save()
	{
		synchronized (root)
		{
			try
			{
				PLog.i("Root values: " + root.getValues(false));

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

		PLog.i("Data Change for [" + uri + " // " + section.getCurrentPath() + "]");

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
		lastFirebaseError = databaseError;
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
