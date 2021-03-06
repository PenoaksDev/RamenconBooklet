package io.amelia.booklet.ui.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.data.BoundDataCallback;
import io.amelia.android.fragments.PersistentFragment;
import io.amelia.android.support.DateAndTime;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.android.support.LibAndroid;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.ContentFragment;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.MapsLocationModel;
import io.amelia.booklet.data.ScheduleAdapter;
import io.amelia.booklet.data.ScheduleDayAdapter;
import io.amelia.booklet.data.ScheduleEventModel;
import io.amelia.booklet.data.ScheduleHandler;
import io.amelia.booklet.data.filters.DefaultScheduleFilter;
import io.amelia.booklet.data.filters.ScheduleFilter;

public class ScheduleFragment extends ContentFragment<ScheduleHandler> implements PersistentFragment
{
	public static final long ONE_DAY = 1440 * 60 * 1000;
	private static final int REQUEST_CHOOSE_ACCOUNT = 0x00;
	private static ScheduleFragment instance = null;

	public static ScheduleFragment instance()
	{
		return instance;
	}

	public ScheduleFilter lastCurrentFilter = null;
	private ExpandableListView mListView;
	private Bundle savedState = null;
	private ScheduleAdapter scheduleAdapter;
	private ScheduleDayAdapter scheduleDayAdapter;

	public ScheduleFragment() throws IOException
	{
		super( ScheduleHandler.class );
		instance = this;
	}

	public MapsLocationModel getLocation( String locId )
	{
		List<String> locations = new ArrayList<>();
		for ( MapsLocationModel location : handler.locations )
		{
			if ( location.id.equals( locId ) )
				return location;
			locations.add( location.id );
		}
		throw new IllegalArgumentException( "The locationId '" + locId + "' was not found from the list " + locations );
	}

	@Override
	public void loadState( Bundle bundle )
	{
		this.savedState = bundle;
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		if ( requestCode == REQUEST_CHOOSE_ACCOUNT )
		{
			if ( resultCode == 0 || data == null )
				Toast.makeText( getContext(), "Account Selection Cancelled. No Changes Were Made.", Toast.LENGTH_SHORT ).show();
			else
			{
				String acct = data.getStringExtra( AccountManager.KEY_ACCOUNT_NAME );
				ContentManager.setPrefCalendarAccount( acct );

				Toast.makeText( getContext(), "Google Calendar account was set to " + acct, Toast.LENGTH_SHORT ).show();
			}
			return;
		}

		super.onActivityResult( requestCode, resultCode, data );
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getActivity().setTitle( "Schedule" );
		setHasOptionsMenu( true );
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
	{
		menu.add( "Show Next Events" ).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick( MenuItem item )
			{
				try
				{
					TreeSet<Date> days = handler.sampleDays();
					long nowTime = new Date().getTime();

					if ( days.size() == 0 )
						return true;

					// Show earliest event one day ahead of time
					if ( days.first().getTime() - ONE_DAY > nowTime )
					{
						Toast.makeText( getContext(), "Ramencon has not started yet... err, hurray... Ramencon is starting soon!", Toast.LENGTH_LONG ).show();
						return true;
					}

					if ( days.last().getTime() + ONE_DAY < nowTime )
					{
						Toast.makeText( getContext(), "Sorry, Ramencon is over. :( We hope you had a great year!", Toast.LENGTH_LONG ).show();
						return true;
					}

					SimpleDateFormat sdf = new SimpleDateFormat( "MM d yyyy" );
					String now = sdf.format( new Date() );

					Date nowMatch = days.first();
					int dayPosition = 0;
					for ( Date day : days )
					{
						dayPosition++;
						if ( sdf.format( day ).equals( now ) )
						{
							nowMatch = day;
							break;
						}
					}

					DefaultScheduleFilter filter = new DefaultScheduleFilter();

					if ( nowMatch != null )
					{
						filter.setMinLong( nowMatch.getTime() );
						filter.setMaxLong( nowMatch.getTime() + ONE_DAY );
					}

					TreeSet<ScheduleEventModel> data = handler.filterRange( filter );
					lastCurrentFilter = filter;

					int positionVisible = -1;

					long nowDate = new Date().getTime();
					ScheduleEventModel[] events = data.toArray( new ScheduleEventModel[0] );
					for ( int i = 0; i < events.length; i++ )
					{
						Long l = events[i].getEndTime();
						if ( l > nowDate )
						{
							positionVisible = i;
							break;
						}
					}

					scheduleDayAdapter.setSelectedPosition( dayPosition, new ArrayList<>( data ), DateAndTime.now( ScheduleDayAdapter.DATE_FORMAT, nowMatch ) );

					if ( positionVisible > 0 )
						mListView.setSelectionFromTop( positionVisible, 0 );

					return true;
				}
				catch ( ParseException e )
				{
					e.printStackTrace();
					Toast.makeText( getContext(), "Sorry, there was a problem!", Toast.LENGTH_LONG ).show();
				}

				return true;
			}
		} );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_schedule, container, false );
	}

	@Override
	public void refreshState()
	{
		savedState = new Bundle();
		saveState( savedState );
	}

	@Override
	public void saveState( Bundle bundle )
	{
		if ( getView() == null )
			return;

		ExpandableListView lv = getView().findViewById( R.id.schedule_listview );

		if ( lastCurrentFilter != null )
			bundle.putString( "filter", LibAndroid.writeBoundDataToJson( lastCurrentFilter.encode() ) );

		if ( lv != null )
		{
			bundle.putInt( "listViewPositionVisible", lv.getFirstVisiblePosition() );
			bundle.putInt( "listViewPositionOffset", lv.getChildAt( 0 ) == null ? 0 : lv.getChildAt( 0 ).getTop() - lv.getPaddingTop() );
		}

		if ( scheduleDayAdapter != null )
			bundle.putInt( "selectedPosition", scheduleDayAdapter.getSelectedPosition() );
	}

	@Override
	public void sectionHandle( BoundData section, boolean isRefresh )
	{
		final View root = getView();

		try
		{
			Objs.notNull( handler.schedule );
			Objs.notNull( handler.locations );

			final TreeSet<Date> days = handler.sampleDays();

			TwoWayView mDayView = root.findViewById( R.id.daylist );
			TextView mDateDisplay = root.findViewById( R.id.date_display );

			Objs.notNull( mDayView );
			Objs.notNull( mDateDisplay );

			mListView = root.findViewById( R.id.schedule_listview );

			int selectedPosition = 1;
			TreeSet<ScheduleEventModel> data;
			int positionVisible = 0;
			int positionOffset = 0;

			if ( savedState == null || savedState.isEmpty() )
			{
				DefaultScheduleFilter filter = new DefaultScheduleFilter();

				SimpleDateFormat sdf = new SimpleDateFormat( "MM d yyyy" );
				String now = sdf.format( new Date() );

				Date nowMatch = null;
				int dayPosition = 0;
				for ( Date day : days )
				{
					dayPosition++;
					if ( sdf.format( day ).equals( now ) )
					{
						nowMatch = day;
						break;
					}
				}

				if ( nowMatch != null )
				{
					filter.setMinLong( nowMatch.getTime() );
					filter.setMaxLong( nowMatch.getTime() + ONE_DAY );
					selectedPosition = dayPosition;
				}
				else if ( handler.hasHeartedEvents() )
				{
					filter.setHearted( DefaultScheduleFilter.TriStateList.SHOW );
					selectedPosition = 0;
				}
				else if ( days.size() > 0 )
				{
					filter.setMinLong( days.first().getTime() );
					filter.setMaxLong( days.first().getTime() + ONE_DAY );
					selectedPosition = 1;
				}

				data = handler.filterRange( filter );
				lastCurrentFilter = filter;

				if ( nowMatch != null )
				{
					// Find the most recent event and set the scroll position
					long nowDate = new Date().getTime();
					ScheduleEventModel[] events = data.toArray( new ScheduleEventModel[0] );
					for ( int i = 0; i < events.length; i++ )
					{
						Long l = events[i].getEndTime();
						if ( l > nowDate )
						{
							positionVisible = i;
							break;
						}
					}
				}
			}
			else
			{
				ScheduleFilter filter;
				try
				{
					filter = ScheduleFilter.decode( LibAndroid.readJsonToBoundData( savedState.getString( "filter" ) )[0] );
				}
				catch ( Exception e )
				{
					ExceptionHelper.handleExceptionOnce( "decode-schedule-filter", e );
					filter = new DefaultScheduleFilter();
				}

				// TODO selected position should be auto-predicted based on the filter. Hate to have different results.
				selectedPosition = savedState.getInt( "selectedPosition", 0 );
				data = handler.filterRange( filter );
				lastCurrentFilter = filter;

				positionVisible = savedState.getInt( "listViewPositionVisible", 0 );
				positionOffset = savedState.getInt( "listViewPositionOffset", 0 );

				savedState = null;
			}

			final List<ScheduleEventModel> modelList = new ArrayList<>( data );

			BoundDataCallback alarmOnClick = new BoundDataCallback()
			{
				@Override
				public boolean call( BoundData data )
				{
					String eventId = data.getString( "eventId" );
					final ScheduleEventModel eventModel = handler.getModel( eventId );

					if ( eventModel == null )
						throw new IllegalStateException( "We had a problem finding event " + eventId + " in list " + modelList + "!" );

					if ( Objs.isEmpty( ContentManager.getPrefCalendarAccount() ) )
					{
						showAccountChooser();
						return false;
					}

					Dexter.withActivity( ContentManager.getActivity() ).withPermissions( Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR ).withListener( new MultiplePermissionsListener()
					{
						@Override
						public void onPermissionRationaleShouldBeShown( List<PermissionRequest> permissions, PermissionToken token )
						{

						}

						@SuppressLint( "MissingPermission" )
						@Override
						public void onPermissionsChecked( MultiplePermissionsReport report )
						{
							if ( report.isAnyPermissionPermanentlyDenied() )
							{
								Toast.makeText( ContentManager.getActivity(), "You must grant the Ramencon Booklet App the READ_CALENDAR and WRITE_CALENDAR permission.", Toast.LENGTH_LONG ).show();
								return;
							}

							try
							{
								int calenderId = -1;
								String calendarAccount = ContentManager.getPrefCalendarAccount();
								String[] projection = new String[] {CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME};
								ContentResolver contentResolver = ContentManager.getActivity().getContentResolver();
								Cursor cursor = contentResolver.query( Uri.parse( "content://com.android.calendar/calendars" ), projection, CalendarContract.Calendars.ACCOUNT_NAME + "=? and (" + CalendarContract.Calendars.NAME + "=? or " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + "=?)", new String[] {calendarAccount, calendarAccount, calendarAccount}, null );

								if ( cursor.moveToFirst() )
									if ( cursor.getString( 1 ).equals( calendarAccount ) )
										calenderId = cursor.getInt( 0 );

								contentResolver = ContentManager.getActivity().getContentResolver();

								if ( eventModel.hasEventId() )
								{
									Uri deleteUri = ContentUris.withAppendedId( CalendarContract.Events.CONTENT_URI, eventModel.getEventId() );
									contentResolver.delete( deleteUri, null, null );

									eventModel.setEventId( -1 );

									Toast.makeText( ContentManager.getActivity(), "Event reminder removed from your Google Calendar.", Toast.LENGTH_LONG ).show();
								}
								else
								{
									ContentValues contentValues = new ContentValues();
									contentValues.put( CalendarContract.Events.CALENDAR_ID, calenderId );
									contentValues.put( CalendarContract.Events.TITLE, eventModel.getTitle() );
									contentValues.put( CalendarContract.Events.DESCRIPTION, eventModel.getDescription() );
									contentValues.put( CalendarContract.Events.EVENT_LOCATION, eventModel.getLocation().title + " at " + ContentManager.getActiveBooklet().getDataTitle() );
									contentValues.put( CalendarContract.Events.DTSTART, eventModel.getStartTime() );
									contentValues.put( CalendarContract.Events.DTEND, eventModel.getEndTime() );
									contentValues.put( CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID() );
									contentValues.put( CalendarContract.Events.HAS_ALARM, 1 );

									Uri uri = contentResolver.insert( Uri.parse( "content://com.android.calendar/events" ), contentValues );

									long eventId = Long.parseLong( uri.getLastPathSegment() );

									ContentValues values = new ContentValues();

									int eventReminderDelay = ContentManager.getPrefEventReminderDelay();
									if ( eventReminderDelay > 0 )
									{
										// contentResolver.delete( ContentUris.withAppendedId( CalendarContract.Reminders.CONTENT_URI, eventId ), null, null );

										values.put( CalendarContract.Reminders.MINUTES, eventReminderDelay );
										values.put( CalendarContract.Reminders.EVENT_ID, eventId );
										values.put( CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALARM );

										contentResolver.insert( CalendarContract.Reminders.CONTENT_URI, values );
									}

									eventModel.setEventId( eventId );

									Toast.makeText( ContentManager.getActivity(), "Event reminder added to your Google Calendar.", Toast.LENGTH_LONG ).show();
								}
							}
							catch ( Exception e )
							{
								ExceptionHelper.handleExceptionOnce( "event-" + eventModel.id + "-alarm", e );
							}
						}
					} ).check();

					return true;
				}
			};

			scheduleDayAdapter = new ScheduleDayAdapter( getContext(), alarmOnClick, new ArrayList<>( days ), mDateDisplay, handler, mListView, mDayView, selectedPosition );
			mDayView.setAdapter( scheduleDayAdapter );

			scheduleAdapter = new ScheduleAdapter( getContext(), alarmOnClick, handler.simpleDateFormat(), handler.simpleTimeFormat(), handler.locations, modelList );
			mListView.setAdapter( scheduleAdapter );

			if ( positionVisible > 0 )
				mListView.setSelectionFromTop( positionVisible, positionOffset );
		}
		catch ( Exception e )
		{
			ExceptionHelper.handleExceptionOnce( "booklet-" + ContentManager.getActiveBooklet().getId() + "-loading", new RuntimeException( "Unexpected Exception.", e ) );
			ContentManager.getActivity().uiShowErrorDialog( "We had a problem loading the schedule. The problem has been reported to the developer. Please try deleting and re-downloading the booklet." );
		}
	}

	public void showAccountChooser()
	{
		Dexter.withActivity( ContentManager.getActivity() ).withPermission( Manifest.permission.GET_ACCOUNTS ).withListener( new PermissionListener()
		{
			@Override
			public void onPermissionDenied( PermissionDeniedResponse response )
			{
				Toast.makeText( getContext(), "You must grant the Ramencon Booklet App the GET_ACCOUNTS permission.", Toast.LENGTH_LONG ).show();
			}

			@Override
			public void onPermissionGranted( PermissionGrantedResponse response )
			{
				Intent intent = AccountManager.newChooseAccountIntent( null, null, new String[] {"com.google"}, true, null, null, null, null );
				startActivityForResult( intent, REQUEST_CHOOSE_ACCOUNT );
			}

			@Override
			public void onPermissionRationaleShouldBeShown( PermissionRequest permission, PermissionToken token )
			{

			}
		} ).check();
	}
}
