package io.amelia.booklet.data;

import android.content.Intent;
import android.support.design.widget.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.amelia.android.data.BoundData;
import io.amelia.android.files.FileBuilder;
import io.amelia.android.files.FileRequestHandler;
import io.amelia.android.files.FileResult;
import io.amelia.android.log.PLog;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.android.support.LibAndroid;
import io.amelia.android.support.LibIO;
import io.amelia.android.support.Objs;
import io.amelia.booklet.ui.activity.ContentActivity;
import io.amelia.booklet.ui.fragment.DownloadFragment;
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
	public static final String KEY_TITLE = "title";
	public static final String KEY_WHEN_FROM = "whenFrom";
	public static final String KEY_WHEN_TO = "whenTo";
	public static final String KEY_WHERE = "where";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_SECTIONS = "sections"; // String Array

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

		for ( File file : ContentManager.getCacheDirectory().listFiles() )
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
				ExceptionHelper.handleExceptionOnce( "booklet-failure-" + file.getName(), new RuntimeException( "Failed to load booklet: " + file.getName(), e ) );
			}
		}
	}

	private volatile BoundData data;
	private volatile List<Exception> exceptions = new ArrayList<>();
	private String id;
	private boolean inUse = false;
	private volatile Map<String, ContentHandler> sectionHandlerList = new HashMap<>();

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

	public void addException( Exception e )
	{
		e.printStackTrace();
		exceptions.add( e );
		triggerUpdate();

		ContentManager.getActivity().uiShowErrorDialog( e.getMessage() );
	}

	public void clearExceptions()
	{
		exceptions.clear();
		triggerUpdate();
	}

	public void clearImageCache()
	{
		File data = getDataDirectory();
		if ( data.exists() )
			for ( File file : LibIO.recursiveFiles( data, ".+\\.(jpg|jpeg|png|gif|bmp)+" ) )
			{
				file.delete();
				PLog.i( "Deleted Image Cache File: " + LibIO.relPath( file, ContentManager.getCacheDirectory() ) );
			}
	}

	public void delete()
	{
		boolean deleted = true;

		for ( File file : getSectionFiles( "data.json" ).values() )
			if ( file.delete() )
				PLog.i( "Deleted file: " + LibIO.relPath( file, ContentManager.getCacheDirectory() ) );
			else
			{
				deleted = false;
				PLog.i( "FAILED to delete file: " + LibIO.relPath( file, ContentManager.getCacheDirectory() ) );
			}

		clearImageCache();
		clearExceptions();

		if ( deleted )
			ContentManager.getActivity().uiShowSnakeBar( "Booklet " + getDataTitle() + " has been deleted!", Snackbar.LENGTH_LONG );
		else
			ContentManager.getActivity().uiShowSnakeBar( "Booklet " + getDataTitle() + " was not deleted!", Snackbar.LENGTH_LONG );

		triggerUpdate();
	}

	public BoundData getData()
	{
		return data;
	}

	void setData( BoundData data )
	{
		this.data = data;
	}

	public String getDataDescription()
	{
		return data.getString( KEY_DESCRIPTION );
	}

	public File getDataDirectory()
	{
		File file = new File( ContentManager.getCacheDirectory(), "booklet-" + getId() );
		file.getParentFile().mkdirs();
		return file;
	}

	public File getDataFile()
	{
		return new File( getDataDirectory(), "data.json" );
	}

	public long getDataLastUpdated()
	{
		return data.getLong( KEY_LAST_UPDATED );
	}

	public List<String> getDataSections()
	{
		List<String> list = new ArrayList<>();
		if ( data == null )
			return list;
		List<String> list1 = data.getStringList( KEY_SECTIONS );
		if ( list1 != null )
			list.addAll( list1 );
		list.add( "welcome" );
		return list;
	}

	public String getDataTitle()
	{
		return data.getString( KEY_TITLE );
	}

	public List<Exception> getExceptions()
	{
		return exceptions;
	}

	public String getId()
	{
		return data.getString( KEY_ID );
	}

	/**
	 * Loads the requested section data from file
	 *
	 * @param section Section id
	 * @return The BoundData on file
	 */
	public BoundData getSectionData( String section )
	{
		File localFile = getSectionFile( section + "/data.json" );
		try
		{
			BoundData[] result = LibAndroid.readJsonToBoundData( localFile );
			if ( result == null || result.length == 0 || result[0] == null )
				throw new IllegalStateException( "The booklet section " + section + " does not exist for booklet " + getId() + "!" );
			return result[0];
		}
		catch ( IOException e )
		{
			throw new RuntimeException( "We had a problem loading the section " + section + " from " + localFile.getAbsolutePath(), e );
		}
	}

	public File getSectionFile( String section )
	{
		return new File( getDataDirectory(), section );
	}

	public Map<String, File> getSectionFiles()
	{
		/*
		Typical Data Structure:
		| booklet-[bookletId]
		|	-> data.json
		|    -> header.png
		|	-> guests
		|		-> data.json
		|	-> guide
		|		-> data.json
		|	-> maps
		|		-> data.json
		|         -> map1.jpg
		|         -> map2.jpg
		*/

		return new HashMap<String, File>()
		{{
			for ( String section : getDataSections() )
				if ( !section.equals( "welcome" ) )
					put( section, getSectionFile( section ) );
		}};
	}

	public Map<String, File> getSectionFiles( String append )
	{
		return new HashMap<String, File>()
		{{
			for ( String section : getDataSections() )
				if ( !section.equals( "welcome" ) )
					put( section, new File( getSectionFile( section ), append ) );
		}};
	}

	public <T extends ContentHandler> T getSectionHandler( Class<T> sectionClass )
	{
		if ( sectionHandlerList.size() == 0 )
			updateSectionHandlers();

		for ( ContentHandler contentHandler : sectionHandlerList.values() )
			if ( sectionClass.isAssignableFrom( contentHandler.getClass() ) && ( Objs.isEmpty( contentHandler.getSectionKey() ) || getDataSections().contains( contentHandler.getSectionKey() ) ) )
			{
				if ( contentHandler.getLastUpdated() != getDataLastUpdated() )
					updateSectionHandler( contentHandler );
				return ( T ) contentHandler;
			}

		throw new IllegalStateException( "The section handler for key " + sectionClass.getSimpleName() + " does not exist! Does it exist in the Booklet manifest?" );
	}

	public List<ContentHandler> getSectionHandlers() throws IOException
	{
		updateSectionHandlers();
		return new ArrayList<>( sectionHandlerList.values() );
	}

	/**
	 * Determines an organic booklet state based on a number of queries.
	 */
	public BookletState getState()
	{
		if ( exceptions.size() > 0 )
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
				ExceptionHelper.handleExceptionOnce( "booklet-failure-check-update-" + id, e );
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

					if ( ContentManager.getAutoUpdate() )
						goDownload();
					else
						triggerUpdate();

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
				if ( ContentManager.getActivity() != null )
					Snackbar.make( ContentManager.getActivity().getCurrentFocus(), "Booklet " + id + " failed to retrieve an update.", Snackbar.LENGTH_LONG ).show();
			}
		} );
	}

	/**
	 * Starts the booklet download.
	 * Doesn't check if already downloaded as it's used for both AVAILABLE and OUTDATED states.
	 */
	public void goDownload()
	{
		if ( isInUse() )
			return;

		new BookletDownloadTask( this, false );
	}

	public void goDownloadAndOpen()
	{
		if ( isInUse() )
			return;

		new BookletDownloadTask( this, true );
	}

	public void goOpen()
	{
		BookletState state = getState();

		if ( state == BookletState.AVAILABLE || state == BookletState.OUTDATED || state == BookletState.READY )
		{
			ContentManager.setActiveBooklet( getId() );
			ContentManager.getActivity().startActivity( new Intent( ContentManager.getActivity(), ContentActivity.class ) );
		}
		else
			ContentManager.getActivity().uiShowSnakeBar( "That booklet is currently not available.", Snackbar.LENGTH_SHORT );
	}

	public boolean hasSection( String keyId )
	{
		return getDataSections().contains( keyId );
	}

	/**
	 * Does a booklet file check, looks for specified JSON and Image files.
	 */
	public boolean isDownloaded()
	{
		for ( File file : getSectionFiles( "data.json" ).values() )
			if ( !file.exists() )
			{
				PLog.i( "File " + LibIO.relPath( file, ContentManager.getCacheDirectory() ) + " does not exist!" );
				return false;
			}
			else
				PLog.i( "File " + LibIO.relPath( file, ContentManager.getCacheDirectory() ) + " exists!" );
		return data.getLong( KEY_LAST_UPDATED, 0L ) > 0;
	}

	public boolean isInUse()
	{
		return inUse;
	}

	public void setInUse( boolean inUse )
	{
		// Possibly tells the Download List to show a intermittent waiting background.
		this.inUse = inUse;

		if ( DownloadFragment.instance() != null )
			DownloadFragment.instance().uiSetBookletInUse( getId(), inUse );
	}

	public void preCacheImages()
	{
		// Do Not Pre-Cache
		if ( ContentManager.getImageCacheTimeout() == 0 )
			return;

		updateSectionHandlers();

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					FileRequestHandler fileRequestHandler = new FileRequestHandler().withProgressListener( new FileBuilder.ProgressListener()
					{
						@Override
						public void finish( FileResult result )
						{
							// PLog.i( "Image PreCache Finish: " + remoteImage );
						}

						@Override
						public void progress( FileResult result, long bytesTransferred, long totalByteCount, boolean indeterminate )
						{
							// PLog.i( "Image PreCache Progress (" + remoteImage + "): " + bytesTransferred + " of " + totalByteCount + " // " + result.isDone );
						}

						@Override
						public void start( FileResult result )
						{
							// PLog.i( "Image PreCache Start: " + remoteImage );
						}
					} );

					if ( sectionHandlerList.containsKey( "guide" ) )
					{
						GuideHandler handler = getSectionHandler( GuideHandler.class );
						for ( GuidePageModel guidePageModel : handler.guidePageModels )
						{
							final File dest = new File( getDataDirectory(), guidePageModel.getLocalImage() );
							if ( !dest.exists() )
								fileRequestHandler.enqueueBuilder( new FileBuilder( "guide-precache-" + guidePageModel.pageNo ).withLocalFile( dest ).withRemoteFile( guidePageModel.getRemoteImage() ) );
						}
					}

					if ( sectionHandlerList.containsKey( "maps" ) )
					{
						MapsHandler handler = getSectionHandler( MapsHandler.class );
						for ( MapsMapModel mapsMapModel : handler.mapsMapModels )
						{
							final File dest = new File( getDataDirectory(), mapsMapModel.getLocalImage() );
							if ( !dest.exists() )
								fileRequestHandler.enqueueBuilder( new FileBuilder( "map-precache-" + mapsMapModel.id ).withLocalFile( dest ).withRemoteFile( mapsMapModel.getRemoteImage() ) );
						}
					}

					fileRequestHandler.start( FileRequestHandler.FileRequestMode.SYNC );
				}
				catch ( Exception e )
				{
					ExceptionHelper.handleExceptionOnce( "image-precache-" + getId(), e );
				}
			}
		} ).start();
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
			addException( e );
		}
	}

	private void triggerUpdate()
	{
		DownloadFragment.instance().uiComputeVisibilities( getId() );
	}

	protected void updateSectionHandler( ContentHandler contentHandler )
	{
		// Empty/null section key returns the Booklet Data instead
		String sectionKey = contentHandler.getSectionKey();
		try
		{
			contentHandler.sectionHandle( Objs.isEmpty( sectionKey ) ? data : getSectionData( contentHandler.getSectionKey() ), getDataLastUpdated() );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( "Handler threw exception:", e );
		}
	}

	protected void updateSectionHandlers()
	{
		synchronized ( sectionHandlerList )
		{
			for ( String handler : getDataSections() )
			{
				if ( !sectionHandlerList.containsKey( handler ) )
					switch ( handler )
					{
						case "welcome":
							sectionHandlerList.put( "welcome", new WelcomeHandler().setBooklet( this ) );
							break;
						case "guests":
							sectionHandlerList.put( "guests", new GuestsHandler().setBooklet( this ) );
							break;
						case "vendors":
							sectionHandlerList.put( "vendors", new VendorsHandler().setBooklet( this ) );
							break;
						case "maps":
							sectionHandlerList.put( "maps", new MapsHandler().setBooklet( this ) );
							break;
						case "schedule":
							sectionHandlerList.put( "schedule", new ScheduleHandler().setBooklet( this ) );
							break;
						case "guide":
							sectionHandlerList.put( "guide", new GuideHandler().setBooklet( this ) );
							break;
						default:
							throw new IllegalArgumentException( "App does not implement the SectionHandler named " + handler + ", might need coding!" );
					}

				ContentHandler contentHandler = sectionHandlerList.get( handler );

				// Check if SectionHandler is deserving an update
				if ( contentHandler != null && contentHandler.getLastUpdated() != getDataLastUpdated() )
					updateSectionHandler( contentHandler );
			}
		}
	}
}
