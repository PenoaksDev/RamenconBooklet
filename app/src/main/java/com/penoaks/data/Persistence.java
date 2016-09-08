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
import com.penoaks.log.PLog;
import com.penoaks.sepher.ConfigurationSection;
import com.penoaks.sepher.types.json.JsonConfiguration;

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

	public Persistence(File cache)
	{
		persistenceFile = new File(cache, "PersistenceData.json");

		handler.execute();
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

		PLog.i("Keeping uri persistent: " + uri);

		containers.add(new PersistenceContainer(uri));
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

	private enum PersistenceState
	{
		IDLE, PENDING
	}

	private void saveToFile(JsonConfiguration data)
	{
		PLog.i("Saving JSON: \n" + data.saveToString());

		parent.overwrite(data);

		try
		{
			parent.save(persistenceFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private class PersistenceContainer implements ValueEventListener, ChildEventListener
	{
		private JsonConfiguration data;
		private DatabaseReference reference;
		private String uri;

		PersistenceContainer(String uri)
		{
			this.uri = uri;

			data = new JsonConfiguration();
			data.options().pathSeparator('/');

			reference = database.getReference(uri);
			reference.addValueEventListener(this);
			reference.addChildEventListener(this);
		}

		public void update()
		{
			saveToFile(data);
			data.resolveChanges();
		}

		public boolean needsUpdate()
		{
			return data.hasChanges();
		}

		@Override
		public void onDataChange(DataSnapshot dataSnapshot)
		{
			String uri = getUri(dataSnapshot.getRef(), true);

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
			PLog.i("Child Added");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s)
		{
			PLog.i("Child Changed");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot)
		{
			PLog.i("Child Removed");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);
		}

		@Override
		public void onChildMoved(DataSnapshot dataSnapshot, String s)
		{
			PLog.i("Child Moved");

			String uri = getUri(dataSnapshot.getRef(), true);

			Object section = data.get(uri);
			ConfigurationSection cs = section == null || !(section instanceof ConfigurationSection) ? data.createSection(uri) : (ConfigurationSection) section;
			convertDataSnapshotToSection(dataSnapshot, cs);
		}

		@Override
		public void onCancelled(DatabaseError databaseError)
		{
			throw databaseError.toException();
		}

		public String getUri(DatabaseReference ref)
		{
			return getUri(ref, false);
		}

		public String getUri(DatabaseReference ref, boolean parentsOnly)
		{
			return getUri(ref, parentsOnly, "/");
		}

		public String getUri(DatabaseReference ref, boolean parentsOnly, String glue)
		{
			List<String> parts = new ArrayList<>();

			if (!parentsOnly)
				parts.add(ref.getKey());

			if (ref.getParent() != null)
				do
				{
					ref = ref.getParent();
					parts.add(ref.getKey());
				} while (ref.getParent() != null && ref.getParent().getKey() != null);

			Collections.reverse(parts);
			return TextUtils.join(glue, parts);
		}

		public void destroy()
		{
			reference.removeEventListener((ValueEventListener) this);
			reference.removeEventListener((ChildEventListener) this);
			saveToFile(data);
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
					Log.w(TAG, "Can't keep up! Did the system time change, or the task is overloaded?");
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
