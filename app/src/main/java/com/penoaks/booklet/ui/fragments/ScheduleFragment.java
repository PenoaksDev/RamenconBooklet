package com.penoaks.booklet.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.penoaks.android.utils.UtilDate;
import com.penoaks.configuration.ConfigurationSection;
import com.penoaks.android.fragments.PersistentFragment;
import com.ramencon.R;
import com.penoaks.android.data.DataAwareFragment;
import com.penoaks.booklet.data.models.ModelEvent;
import com.penoaks.booklet.data.models.ModelLocation;
import com.penoaks.booklet.data.schedule.ScheduleAdapter;
import com.penoaks.booklet.data.schedule.ScheduleDataReceiver;
import com.penoaks.booklet.data.schedule.ScheduleDayAdapter;
import com.penoaks.booklet.data.schedule.filters.DefaultScheduleFilter;

import org.acra.ACRA;
import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

public class ScheduleFragment extends DataAwareFragment<ScheduleDataReceiver> implements PersistentFragment, SwipeRefreshLayout.OnRefreshListener
{
	public static final long ONEDAY = 1440 * 60 * 1000;

	private static ScheduleFragment instance = null;

	public static ScheduleFragment instance()
	{
		return instance;
	}

	public DefaultScheduleFilter currentFilter = new DefaultScheduleFilter();
	private ScheduleDayAdapter scheduleDayAdapter;
	private ScheduleAdapter scheduleAdapter;
	private ExpandableListView mListView;

	private Bundle savedState = null;

	public ScheduleFragment()
	{
		instance = this;
		setReceiver( ScheduleDataReceiver.getInstance() );
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getActivity().setTitle( "Convention Schedule" );
		setHasOptionsMenu( true );
	}

	@Override
	protected void onDataUpdate( ConfigurationSection data )
	{

	}

	@Override
	public void onDataArrived( ConfigurationSection section, boolean isRefresh )
	{
		final View root = getView();

		SwipeRefreshLayout refresher = ( SwipeRefreshLayout ) root.findViewById( R.id.schedule_refresher );
		refresher.setOnRefreshListener( this );
		refresher.setColorSchemeColors( getResources().getColor( R.color.colorPrimary ), getResources().getColor( R.color.colorAccent ), getResources().getColor( R.color.lighter_gray ) );
		refresher.setRefreshing( false );

		try
		{
			assert receiver.schedule.size() > 0;

			final TreeSet<Date> days = receiver.sampleDays();

			TwoWayView mDayView = ( TwoWayView ) root.findViewById( R.id.daylist );
			TextView mDateDisplay = ( TextView ) root.findViewById( R.id.date_display );

			assert mDayView != null;
			assert mDateDisplay != null;
			assert days.size() > 0;

			mListView = ( ExpandableListView ) root.findViewById( R.id.schedule_listview );

			int selectedPosition = 1;
			TreeSet<ModelEvent> data;
			int positionVisible = 0;
			int positionOffset = 0;

			currentFilter.reset();

			if ( savedState == null || savedState.isEmpty() )
			{
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
					currentFilter.setMin( nowMatch.getTime() );
					currentFilter.setMax( nowMatch.getTime() + ONEDAY );
					selectedPosition = dayPosition;
				}
				else if ( receiver.hasHeartedEvents() )
				{
					currentFilter.setHearted( DefaultScheduleFilter.TriStateList.SHOW );
					selectedPosition = 0;
				}
				else if ( days.size() > 0 )
				{
					currentFilter.setMin( days.first().getTime() );
					currentFilter.setMax( days.first().getTime() + ONEDAY );
					selectedPosition = 1;
				}

				data = receiver.filterRange( currentFilter );

				if ( nowMatch != null )
				{
					// Find the most recent event and set the scroll position
					long nowDate = new Date().getTime();
					ModelEvent[] events = data.toArray( new ModelEvent[0] );
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
				try
				{
					currentFilter = DefaultScheduleFilter.getAdapter().fromJson( savedState.getString( "filter" ) );
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}

				selectedPosition = savedState.getInt( "selectedPosition", 0 );
				data = receiver.filterRange( currentFilter );

				positionVisible = savedState.getInt( "listViewPositionVisible", 0 );
				positionOffset = savedState.getInt( "listViewPositionOffset", 0 );

				savedState = null;
			}

			scheduleDayAdapter = new ScheduleDayAdapter( this, new ArrayList<>( days ), mDateDisplay, receiver, mListView, mDayView, selectedPosition );
			mDayView.setAdapter( scheduleDayAdapter );

			scheduleAdapter = new ScheduleAdapter( getActivity(), receiver.simpleDateFormat(), receiver.simpleTimeFormat(), receiver.locations, new ArrayList<>( data ) );
			mListView.setAdapter( scheduleAdapter );

			if ( positionVisible > 0 )
				mListView.setSelectionFromTop( positionVisible, positionOffset );
		}
		catch ( Exception e )
		{
			ACRA.getErrorReporter().handleException( new RuntimeException( "Unexpected Exception.", e ) );

			Toast.makeText( HomeActivity.instance, "We had a problem loading the schedule. The error has been reported.", Toast.LENGTH_LONG ).show();
		}
	}

	@Override
	public void onRefresh()
	{
		savedState = new Bundle();
		saveState( savedState );

		super.refreshData();
	}

	@Override
	public void saveState( Bundle bundle )
	{
		if ( getView() == null )
			return;

		ExpandableListView lv = ( ExpandableListView ) getView().findViewById( R.id.schedule_listview );

		bundle.putString( "filter", DefaultScheduleFilter.getAdapter().toJson( currentFilter ) );

		if ( lv != null )
		{
			bundle.putInt( "listViewPositionVisible", lv.getFirstVisiblePosition() );
			bundle.putInt( "listViewPositionOffset", lv.getChildAt( 0 ) == null ? 0 : lv.getChildAt( 0 ).getTop() - lv.getPaddingTop() );
		}

		if ( scheduleDayAdapter != null )
			bundle.putInt( "selectedPosition", scheduleDayAdapter.getSelectedPosition() );
	}

	@Override
	public void loadState( Bundle bundle )
	{
		this.savedState = bundle;
	}

	@Override
	public void refreshState()
	{
		onRefresh();
	}

	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_schedule, container, false );
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
					TreeSet<Date> days = receiver.sampleDays();
					long nowTime = new Date().getTime();

					assert days.size() > 0;


					// Show earliest event one day ahead of time
					if ( days.first().getTime() - ONEDAY > nowTime )
					{
						Toast.makeText( getContext(), "Sorry, Ramencon has not started yet... err, hurray... Ramencon is starting soon!", Toast.LENGTH_LONG ).show();
						return true;
					}

					if ( days.last().getTime() + ONEDAY < nowTime )
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

					currentFilter.reset();

					if ( nowMatch != null )
					{
						currentFilter.setMin( nowMatch.getTime() );
						currentFilter.setMax( nowMatch.getTime() + ONEDAY );
					}

					TreeSet<ModelEvent> data = receiver.filterRange( currentFilter );

					int positionVisible = -1;

					long nowDate = new Date().getTime();
					ModelEvent[] events = data.toArray( new ModelEvent[0] );
					for ( int i = 0; i < events.length; i++ )
					{
						Long l = events[i].getEndTime();
						if ( l > nowDate )
						{
							positionVisible = i;
							break;
						}
					}

					scheduleDayAdapter.setSelectedPosition( dayPosition, new ArrayList<>( data ), UtilDate.now( ScheduleDayAdapter.DATEFORMAT, nowMatch ) );

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

	public ModelLocation getLocation( String locId )
	{
		for ( ModelLocation location : receiver.locations )
			if ( location.id.equals( locId ) )
				return location;
		return null;
	}
}
