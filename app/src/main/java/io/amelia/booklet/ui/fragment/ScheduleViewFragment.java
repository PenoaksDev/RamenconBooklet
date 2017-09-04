package io.amelia.booklet.ui.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.amelia.R;
import io.amelia.booklet.data.ScheduleAdapter;
import io.amelia.booklet.data.ScheduleEventModel;

public class ScheduleViewFragment extends Fragment
{
	public static ScheduleAdapter adapter;

	public static ScheduleViewFragment instance( int id )
	{
		Bundle bundle = new Bundle();
		bundle.putInt( "event", id );

		ScheduleViewFragment frag = new ScheduleViewFragment();
		frag.setArguments( bundle );
		return frag;
	}

	public ScheduleViewFragment()
	{

	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "View Event" );

		View root = inflater.inflate( R.layout.fragment_schedule_view, container, false );

		TextView eventTitle = root.findViewById( R.id.event_view_title );
		TextView eventLocation = root.findViewById( R.id.event_view_location );
		TextView eventDate = root.findViewById( R.id.event_view_date );
		TextView eventTime = root.findViewById( R.id.event_view_time );
		TextView eventDescription = root.findViewById( R.id.event_view_description );

		if ( getArguments() == null )
			eventTitle.setText( "There was a problem! :(" );
		else
		{
			ScheduleEventModel event = adapter.events.get( getArguments().getInt( "event" ) );

			eventTitle.setText( event.title );
			eventLocation.setText( event.getLocation().title );
			eventDescription.setText( event.description );

			try
			{
				Date date = adapter.dateFormat.parse( "yyyy-MM-dd" );// event.date );
				Date from = adapter.timeFormat.parse( "hh:mm a" );// event.time );
				Date to = new Date( from.getTime() + ( 60000 * Integer.parseInt( event.duration ) ) );

				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( getActivity() );
				boolean use24h = pref.getBoolean( "pref_military", false );

				String format_from = use24h ? ScheduleAdapter.DISPLAY_FORMAT_TIME24.format( from ) : ScheduleAdapter.DISPLAY_FORMAT_TIME12.format( from );
				String format_to = use24h ? ScheduleAdapter.DISPLAY_FORMAT_TIME24.format( to ) : ScheduleAdapter.DISPLAY_FORMAT_TIME12.format( to );

				eventDate.setText( new SimpleDateFormat( "EEEE MM/dd" ).format( date ) );
				eventTime.setText( format_from + " — " + format_to );
			}
			catch ( ParseException e )
			{
				e.printStackTrace();
			}
		}

		return root;
	}
}
