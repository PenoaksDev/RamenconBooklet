package io.amelia.booklet.data;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.lucasr.twowayview.TwoWayView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.amelia.R;
import io.amelia.android.data.BoundDataCallback;
import io.amelia.android.support.DateAndTime;
import io.amelia.booklet.data.filters.DefaultScheduleFilter;
import io.amelia.booklet.data.filters.NoScheduleFilter;
import io.amelia.booklet.ui.fragment.ScheduleFragment;

public class ScheduleDayAdapter extends BaseAdapter
{
	public static final String DATE_FORMAT = "MMMM dx yyyy";
	private BoundDataCallback alarmOnClick;
	private Context context;
	private List<Date> days;
	private LayoutInflater inflater;
	private TextView mDateDisplay;
	private TwoWayView mDayView;
	private ExpandableListView mListView;
	private ScheduleHandler mScheduleDataReceiver;
	private int selectedPosition;

	public ScheduleDayAdapter( Context context, BoundDataCallback alarmOnClick, List<Date> days, TextView mDateDisplay, ScheduleHandler mScheduleDataReceiver, ExpandableListView mListView, TwoWayView mDayView, int selectedPosition )
	{
		this.context = context;
		this.inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		this.alarmOnClick = alarmOnClick;
		this.days = days;
		this.mDateDisplay = mDateDisplay;
		this.mScheduleDataReceiver = mScheduleDataReceiver;
		this.mListView = mListView;
		this.mDayView = mDayView;
		this.selectedPosition = selectedPosition;
	}

	@Override
	public int getCount()
	{
		return days.size() + 2;
	}

	@Override
	public Object getItem( int position )
	{
		return position;
	}

	@Override
	public long getItemId( int position )
	{
		return position;
	}

	public int getSelectedPosition()
	{
		return selectedPosition;
	}

	@Override
	public View getView( final int position, View convertView, ViewGroup parent )
	{
		View view = inflater.inflate( R.layout.fragment_schedule_listitem_day, null );

		TextView dayName = view.findViewById( R.id.day_name );
		TextView dayNumber = view.findViewById( R.id.day_number );
		ImageView dayImage = view.findViewById( R.id.day_image );

		assert dayName != null;
		assert dayNumber != null;
		assert dayImage != null;

		if ( position == selectedPosition )
			view.setBackgroundColor( ResourcesCompat.getColor( context.getResources(), R.color.colorAccent, null ) );

		if ( position == 0 )
		{
			dayName.setText( "Favs" );
			dayImage.setImageDrawable( new IconDrawable( context, FontAwesomeIcons.fa_heart ).colorRes( R.color.colorWhite ).actionBarSize() );
			dayImage.setVisibility( View.VISIBLE );
			dayNumber.setVisibility( View.GONE );

			if ( position == selectedPosition && mDateDisplay != null )
				mDateDisplay.setText( "Favorite Events" );

			view.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					try
					{
						DefaultScheduleFilter filter = new DefaultScheduleFilter();
						filter.setHearted( DefaultScheduleFilter.TriStateList.SHOW );

						setSelectedPosition( position, mScheduleDataReceiver.filterRangeList( filter ), "Favorite Events" );
						ScheduleFragment.instance().lastCurrentFilter = filter;
					}
					catch ( ParseException e )
					{
						e.printStackTrace();
					}
				}
			} );
		}
		else if ( position == 1 )
		{
			dayName.setText( "All" );
			dayImage.setImageDrawable( new IconDrawable( context, FontAwesomeIcons.fa_bars ).colorRes( R.color.colorWhite ).actionBarSize() );
			dayImage.setVisibility( View.VISIBLE );
			dayNumber.setVisibility( View.GONE );

			if ( position == selectedPosition && mDateDisplay != null )
				mDateDisplay.setText( "All Events" );

			view.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					try
					{
						NoScheduleFilter filter = new NoScheduleFilter();
						setSelectedPosition( position, mScheduleDataReceiver.filterRangeList( filter ), "All Events" );
						ScheduleFragment.instance().lastCurrentFilter = filter;
					}
					catch ( ParseException e )
					{
						e.printStackTrace();
					}
				}
			} );
		}
		else
		{
			final Date day = days.get( position - 2 );

			dayName.setText( DateAndTime.now( "EEE", day ) );
			dayNumber.setText( DateAndTime.now( "d", day ) );

			if ( position == selectedPosition && mDateDisplay != null )
				mDateDisplay.setText( DateAndTime.now( DATE_FORMAT, day ) );

			view.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					try
					{
						DefaultScheduleFilter filter = new DefaultScheduleFilter();

						Calendar startCalendar = Calendar.getInstance();
						startCalendar.setTime( day );
						startCalendar.set( Calendar.HOUR, 6 );
						startCalendar.set( Calendar.MINUTE, 0 );
						startCalendar.set( Calendar.SECOND, 0 );
						startCalendar.set( Calendar.MILLISECOND, 0 );

						Calendar endCalendar = ( Calendar ) startCalendar.clone();
						endCalendar.add( Calendar.HOUR, 24 );

						filter.setMin( startCalendar.getTime() );
						filter.setMax( endCalendar.getTime() );

						setSelectedPosition( position, mScheduleDataReceiver.filterRangeList( filter ), DateAndTime.now( DATE_FORMAT, day ) );
						ScheduleFragment.instance().lastCurrentFilter = filter;
					}
					catch ( ParseException e )
					{
						e.printStackTrace();
					}
				}
			} );
		}

		return view;
	}

	public void setSelectedPosition( int position, List<ScheduleEventModel> data, String title ) throws ParseException
	{
		if ( mDayView.getChildAt( position ) == null )
			throw new IndexOutOfBoundsException();

		ScheduleDayAdapter.this.selectedPosition = position;

		mListView.setAdapter( new ScheduleAdapter( context, alarmOnClick, mScheduleDataReceiver.simpleDateFormat(), mScheduleDataReceiver.simpleTimeFormat(), mScheduleDataReceiver.locations, data ) );

		for ( int i = 0; i < mDayView.getChildCount(); i++ )
			if ( i != position )
				mDayView.getChildAt( i ).setBackgroundColor( 0x00000000 );

		mDayView.getChildAt( position ).setBackgroundColor( ResourcesCompat.getColor( context.getResources(), R.color.colorAccent, null ) );

		if ( mDateDisplay != null && title != null )
			mDateDisplay.setText( title );
	}
}
