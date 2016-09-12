package com.penoaks.data;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.penoaks.sepher.ConfigurationSection;
import com.penoaks.sepher.types.json.JsonConfiguration;
import com.ramencon.RamenApp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Persistence
{
	private final String TAG = "Persistence";

	private AsyncHandler handler = new AsyncHandler();

	private final List<PersistenceContainer> containers = new CopyOnWriteArrayList<>();
	private FirebaseDatabase database = FirebaseDatabase.getInstance();
	private PersistenceState state = PersistenceState.PENDING;
	private JsonConfiguration parent = new JsonConfiguration();
	private File persistenceFile;

	public int currentTick = 0;
	public boolean isRunning = true;
	private static Persistence instance;

	public static Persistence getInstance()
	{
		return instance;
	}

	public Persistence(File cache)
	{
		instance = this;

		persistenceFile = new File(cache, "PersistenceData.json");
		parent.options().pathSeparator('/');

		if (persistenceFile.exists())
			try
			{
				parent.load(persistenceFile);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		handler.execute();
	}

	public PersistenceContainer getContainer(String uri)
	{
		uri = uri.startsWith("/") ? uri : "/" + uri;
		for (PersistenceContainer container : containers)
		{
			String containerUri = container.uri.startsWith("/") ? container.uri : "/" + container.uri;
			if (uri.startsWith(containerUri))
				return container;
		}
		return null;
	}

	public void destroy()
	{
		isRunning = false;
		for (PersistenceContainer container : containers)
			container.destroy();
		containers.clear();
	}

	public void keepPersistent(String uri)
	{
		for (PersistenceContainer container : containers)
		{
			if (container.uri.equals(uri) || uri.startsWith(container.uri))
				return;
			if (container.uri.startsWith(uri))
			{
				container.destroy();
				containers.remove(container);
			}
		}

		containers.add(new PersistenceContainer(uri, parent.has(uri) ? parent.getConfigurationSection(uri) : null));
	}

	/**
	 * Called from Async Tick Handler to handle persistence
	 */
	private void heartbeat()
	{
		for (PersistenceContainer container : containers)
		{
			if (container.needsUpdate())
			{
				container.update();
				return;
			}
		}

		state = PersistenceState.IDLE;
	}

	/**
	 * @return The persistence state
	 */
	public PersistenceState getState()
	{
		return state;
	}

	public ConfigurationSection get()
	{
		return get(null);
	}

	public ConfigurationSection get(String path)
	{
		return path == null || path.isEmpty() ? parent : parent.getConfigurationSection(path, true);
	}

	public boolean check()
	{
		// Check containers for data
		return true;
	}

	private enum PersistenceState
	{
		IDLE, PENDING
	}

	private void saveToFile(JsonConfiguration data)
	{
		// PLog.i("Saving JSON: %s", data.saveToString());

		synchronized (parent)
		{
			parent.set(data);

			try
			{
				parent.save(persistenceFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class PersistenceContainer implements ValueEventListener, ChildEventListener
	{
		private JsonConfiguration data;
		private DatabaseReference reference;
		private boolean markedForUpdate = false;
		private String uri;

		PersistenceContainer(String uri, ConfigurationSection section)
		{
			this.uri = uri;

			data = new JsonConfiguration();
			data.options().pathSeparator('/');

			if (section != null)
				data.set(section);

			reference = database.getReference(uri);
			reference.addValueEventListener(this);
			reference.addChildEventListener(this);
		}

		public void update()
		{
			markedForUpdate = false;
			saveToFile(data);
			data.resolveChanges();
		}

		public boolean needsUpdate()
		{
			return data.hasChanges() || markedForUpdate;
		}

		@Override
		public void onDataChange(DataSnapshot dataSnapshot)
		{
			String uri = getUri(dataSnapshot, true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);

			saveToFile(data);
		}

		public void convertDataSnapshotToSection(DataSnapshot dataSnapshot, ConfigurationSection section)
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

		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s)
		{
			/*PLog.i("Child Added");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s)
		{
			/*PLog.i("Child Changed");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot)
		{
			/*PLog.i("Child Removed");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);*/
		}

		@Override
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
			throw databaseError.toException();
		}

		public String getUri(DataSnapshot dataSnapshot)
		{
			return getUri(dataSnapshot, false);
		}

		public String getUri(DataSnapshot dataSnapshot, boolean parentsOnly)
		{
			return getUri(dataSnapshot, parentsOnly, "/");
		}

		public String getUri(DataSnapshot dataSnapshot, boolean parentsOnly, String glue)
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

		public void destroy()
		{
			reference.removeEventListener((ValueEventListener) this);
			reference.removeEventListener((ChildEventListener) this);
			saveToFile(data);
		}

		public void markForUpdate()
		{
			markedForUpdate = true;
		}
	}

	class AsyncHandler extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			long i = System.currentTimeMillis();

			long q = 0L;
			long j = 0L;
			for (; ; )
			{
				long k = System.currentTimeMillis();
				long l = k - i;

				if (l > 2000L && i - q >= 15000L)
				{
					Log.w(TAG, "Can't keep up! Did the system time change, or is the system overloaded?");
					l = 2000L;
					q = i;
				}

				if (l < 0L)
				{
					Log.w(TAG, "Time ran backwards! Did the system time change?");
					l = 0L;
				}

				j += l;
				i = k;

				while (j > 50L)
				{
					currentTick = (int) (System.currentTimeMillis() / 50);
					j -= 50L;

					Persistence.this.heartbeat();
				}

				if (!isRunning)
					break;

				// Log.i(TAG, "Persistence Tick Rate: " + currentTick);

				try
				{
					Thread.sleep(1L);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			return null;
		}
	}
}
