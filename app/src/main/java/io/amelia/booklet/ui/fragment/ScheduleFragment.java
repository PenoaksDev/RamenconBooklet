package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
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

import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.fragments.PersistentFragment;
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.DateAndTime;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.ContentFragment;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.MapsLocationModel;
import io.amelia.booklet.data.ScheduleAdapter;
import io.amelia.booklet.data.ScheduleDayAdapter;
import io.amelia.booklet.data.ScheduleEventModel;
import io.amelia.booklet.data.ScheduleHandler;
import io.amelia.booklet.data.filters.DefaultScheduleFilter;
import io.amelia.booklet.ui.activity.BaseActivity;

public class ScheduleFragment extends ContentFragment<ScheduleHandler> implements PersistentFragment
{
	public static final long ONE_DAY = 1440 * 60 * 1000;

	private static ScheduleFragment instance = null;

	public static ScheduleFragment instance()
	{
		return instance;
	}

	public DefaultScheduleFilter currentFilter = new DefaultScheduleFilter();
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
		for ( MapsLocationModel location : handler.locations )
			if ( location.id.equals( locId ) )
				return location;
		return null;
	}

	@Override
	public void loadState( Bundle bundle )
	{
		this.savedState = bundle;
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getActivity().setTitle( "Convention Schedule" );
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

					currentFilter.reset();

					if ( nowMatch != null )
					{
						currentFilter.setMin( nowMatch.getTime() );
						currentFilter.setMax( nowMatch.getTime() + ONE_DAY );
					}

					TreeSet<ScheduleEventModel> data = handler.filterRange( currentFilter );

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

					scheduleDayAdapter.setSelectedPosition( dayPosition, new ArrayList<>( data ), DateAndTime.now( ScheduleDayAdapter.DATEFORMAT, nowMatch ) );

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
					currentFilter.setMax( nowMatch.getTime() + ONE_DAY );
					selectedPosition = dayPosition;
				}
				else if ( handler.hasHeartedEvents() )
				{
					currentFilter.setHearted( DefaultScheduleFilter.TriStateList.SHOW );
					selectedPosition = 0;
				}
				else if ( days.size() > 0 )
				{
					currentFilter.setMin( days.first().getTime() );
					currentFilter.setMax( days.first().getTime() + ONE_DAY );
					selectedPosition = 1;
				}

				data = handler.filterRange( currentFilter );

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
				try
				{
					currentFilter = DefaultScheduleFilter.getAdapter().fromJson( savedState.getString( "filter" ) );
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}

				selectedPosition = savedState.getInt( "selectedPosition", 0 );
				data = handler.filterRange( currentFilter );

				positionVisible = savedState.getInt( "listViewPositionVisible", 0 );
				positionOffset = savedState.getInt( "listViewPositionOffset", 0 );

				savedState = null;
			}

			scheduleDayAdapter = new ScheduleDayAdapter( this, new ArrayList<>( days ), mDateDisplay, handler, mListView, mDayView, selectedPosition );
			mDayView.setAdapter( scheduleDayAdapter );

			scheduleAdapter = new ScheduleAdapter( getActivity(), handler.simpleDateFormat(), handler.simpleTimeFormat(), handler.locations, new ArrayList<>( data ) );
			mListView.setAdapter( scheduleAdapter );

			if ( positionVisible > 0 )
				mListView.setSelectionFromTop( positionVisible, positionOffset );
		}
		catch ( Exception e )
		{
			ACRAHelper.handleExceptionOnce( "booklet-" + ContentManager.getActiveBooklet().getId() + "-loading", new RuntimeException( "Unexpected Exception.", e ) );
			( ( BaseActivity ) getActivity() ).uiShowErrorDialog( "We had a problem loading the schedule. The problem has been reported to the developer. Please try deleting and re-downloading the booklet." );
		}
	}
}
