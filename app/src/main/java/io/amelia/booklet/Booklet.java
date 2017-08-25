package io.amelia.booklet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.amelia.android.data.BoundData;
import io.amelia.android.log.PLog;
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.LibAndroid;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Booklet
{
	public static final String KEY_ID = "bookletId";
	public static final String KEY_PUBLISHED = "published";
	public static final String KEY_LAST_UPDATED = "lastUpdated"; // long
	public static final String KEY_OUTDATED = "outdated"; // boolean
	public static final String KEY_IMAGE = "image";
	public static final String KEY_TITLE = "title";
	public static final String KEY_WHEN_FROM = "whenFrom";
	public static final String KEY_WHEN_TO = "whenTo";
	public static final String KEY_WHERE = "where";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_FILES = "files"; // String Array

	static final List<Booklet> booklets = new ArrayList<>();

	public static void addBooklet( String bookletId )
	{
		// Check if booklet is not already loaded.
		if ( getBooklet( bookletId ) == null )
			booklets.add( new Booklet( bookletId ) );
	}

	public static Booklet getBooklet( String bookletId )
	{
		if ( bookletId != null )
			for ( Booklet booklet : booklets )
				if ( booklet.getId().equals( bookletId ) )
					return booklet;
		return null;
	}

	public static List<Booklet> getBooklets()
	{
		return Collections.unmodifiableList( booklets );
	}

	static void refreshBooklets()
	{
		for ( Booklet booklet : booklets )
			booklet.goCheckForUpdate();
	}

	/**
	 * Loads Booklets from file
	 */
	static void setup()
	{
		booklets.clear();

		for ( File file : ContentManager.getCacheDir().listFiles() )
		{
			try
			{
				if ( file.isDirectory() )
				{
					File dataFile = new File( file, "data.json" );
					if ( dataFile.isFile() )
						booklets.add( new Booklet( dataFile ) );
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
				ACRAHelper.handleExceptionOnce( "booklet-failure-" + file.getName(), new RuntimeException( "Failed to load booklet: " + file.getName(), e ) );
			}
		}
	}

	BoundData data;
	/*
	static Booklet updateOrCreate( Bundle bundle )
	{
		String bookletId = bundle.getString( KEY_ID );

		Booklet booklet = getBooklet( bookletId );

		try
		{
			if ( booklet == null )
			{
				booklet = new Booklet( bookletId );
				booklets.add( booklet );
			}

			booklet.processData( bundle );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			ACRAHelper.handleExceptionOnce( "booklet-failure-" + bookletId, new RuntimeException( "Failed to update booklet: " + bookletId, e ) );
		}

		return booklet;
	}
	*/ Exception lastException = null;
	private String id;
	private boolean inUse = false;

	Booklet( String id )
	{
		data = new BoundData();
		this.id = id;

		PLog.i( "Creating Booklet " + id );

		data.put( KEY_ID, id );
		data.put( KEY_LAST_UPDATED, 0 ); // Force Update

		goCheckForUpdate();
	}

	Booklet( File dataFile ) throws IOException
	{
		data = LibAndroid.readJsonToBoundData( dataFile )[0];
		id = data.getString( KEY_ID );

		PLog.i( "Loading Booklet " + id + " from file " + dataFile.getAbsolutePath() );

		// Compare directory to provided file. Should this auto-correct?
		if ( !getDataFile().getAbsolutePath().equals( dataFile.getAbsolutePath() ) )
			throw new IllegalArgumentException( "The internal booklet data file ID doesn't match the file path." );
	}

	public void clearFailure()
	{
		lastException = null;
	}

	public BoundData getData()
	{
		return data;
	}

	public String getDataDescription()
	{
		return data.getString( KEY_DESCRIPTION );
	}

	public File getDataDirectory()
	{
		File file = new File( ContentManager.getCacheDir(), "booklet-" + getId() );
		file.getParentFile().mkdirs();
		return file;
	}

	public File getDataFile()
	{
		return new File( getDataDirectory(), "data.json" );
	}

	public String getDataImage()
	{
		return data.getString( KEY_IMAGE );
	}

	public long getDataLastUpdated()
	{
		return data.getLong( KEY_LAST_UPDATED );
	}

	public String getDataTitle()
	{
		return data.getString( KEY_TITLE );
	}

	public String getId()
	{
		return data.getString( KEY_ID );
	}

	public Exception getLastException()
	{
		return lastException;
	}

	/**
	 * Determines an organic booklet state based on a number of queries.
	 */
	public BookletState getState()
	{
		if ( lastException != null )
			return BookletState.FAILURE;
		if ( inUse )
			return BookletState.BUSY;
		if ( isDownloaded() )
		{
			if ( data.getBoolean( KEY_OUTDATED, false ) )
				return BookletState.OUTDATED;
			return BookletState.READY;
		}
		return BookletState.AVAILABLE;
	}

	private void goCheckForUpdate()
	{
		setInUse( true );

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url( ContentManager.REMOTE_CATALOG_URL + id ).build();
		client.newCall( request ).enqueue( new Callback()
		{
			@Override
			public void onFailure( Call call, IOException e )
			{
				e.printStackTrace();
				ACRAHelper.handleExceptionOnce( "booklet-failure-check-update-" + id, e );
				showError();

				setInUse( false );
			}

			@Override
			public void onResponse( Call call, Response response ) throws IOException
			{
				try
				{
					BoundData responseBundle = LibAndroid.readJsonToBoundData( response.body().string() )[0];

					if ( responseBundle.getBoundData( "details" ).getInt( "code" ) == 0 )
					{
						showError();
						return;
					}

					BoundData bundle = responseBundle.getBoundData( "data" );

					if ( !bundle.containsKey( KEY_ID ) || !bundle.containsKey( KEY_LAST_UPDATED ) )
						throw new IllegalArgumentException( "Bundle is missing essential keys" );

					if ( !id.equals( bundle.getString( KEY_ID ) ) )
						throw new IllegalArgumentException( "Booklet Id Mismatch!" );

					if ( isDownloaded() )
						data.put( KEY_OUTDATED, bundle.getLong( KEY_LAST_UPDATED ) < bundle.getLong( KEY_LAST_UPDATED ) );
					else
					{
						data.putAll( bundle );
						data.put( KEY_LAST_UPDATED, 0 ); // Never Downloaded
					}

					// TODO An auto-update setting?
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}

				setInUse( false );
			}

			void showError()
			{
				if ( ContentManager.getDownloadActivity() != null )
					Snackbar.make( ContentManager.getDownloadActivity().getCurrentFocus(), "Booklet " + id + " failed to retrieve an update.", Snackbar.LENGTH_LONG ).show();
			}
		} );
	}

	/**
	 * Starts the booklet download.
	 * Doesn't check if already downloaded as it's used for both AVAILABLE and OUTDATED states.
	 */
	public void goDownload( View view )
	{
		try
		{
			new BookletDownloadTask( this, view );
		}
		catch ( BookletDownloadTask.UpdateAbortException e )
		{
			// Ignore
		}
	}

	void handleException( Exception e )
	{
		e.printStackTrace();

		lastException = e;

		if ( ContentManager.getDownloadActivity() != null )
			Snackbar.make( ContentManager.getDownloadActivity().getCurrentFocus(), e.getMessage(), Snackbar.LENGTH_LONG ).show();
	}

	/**
	 * Does a booklet file check, looks for specified JSON and Image files.
	 * If cached images are turned off, that part is skip'd.
	 */
	public boolean isDownloaded()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ContentManager.getApplicationContext() );
		if ( prefs.getBoolean( "download_images", false ) )
		{

		}

		// TODO

		return data.getLong( KEY_LAST_UPDATED, 0L ) > 0;
	}

	public void saveData()
	{
		try
		{
			File dataFile = getDataFile();
			dataFile.getParentFile().mkdirs();
			LibAndroid.writeBoundDataToJsonFile( data, dataFile );
		}
		catch ( IOException e )
		{
			handleException( e );
		}
	}

	public void setInUse( boolean inUse )
	{
		// Possibly tells the Download List to show a intermittent waiting background.
		this.inUse = inUse;
	}
}
