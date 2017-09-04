package io.amelia.booklet.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.lucasr.twowayview.TwoWayView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.amelia.R;
import io.amelia.android.data.BoundDataCallback;
import io.amelia.android.support.DateAndTime;
import io.amelia.booklet.data.filters.DefaultScheduleFilter;
import io.amelia.booklet.ui.fragment.ScheduleFragment;

public class ScheduleDayAdapter extends BaseAdapter
{
	public static final String DATEFORMAT = "MMMM dx yyyy";
	private BoundDataCallback alarmOnClick;
	private Context context;
	private List<Date> days = new ArrayList<>();
	private LayoutInflater inflater = null;
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
		return days.size() + 1;
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
			view.setBackgroundColor( 0xFFff4081 );

		if ( position == 0 )
		{
			dayName.setText( "Favs" );
			dayImage.setImageResource( R.drawable.ic_heart );
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
						DefaultScheduleFilter filter = ScheduleFragment.instance().currentFilter;
						filter.reset();
						filter.setHearted( DefaultScheduleFilter.TriStateList.SHOW );

						setSelectedPosition( position, mScheduleDataReceiver.filterRangeList( filter ), "Favorite Events" );
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
			final Date day = days.get( position - 1 );

			dayName.setText( DateAndTime.now( "EEE", day ) );
			dayNumber.setText( DateAndTime.now( "d", day ) );

			if ( position == selectedPosition && mDateDisplay != null )
				mDateDisplay.setText( DateAndTime.now( DATEFORMAT, day ) );

			view.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					try
					{
						DefaultScheduleFilter filter = ScheduleFragment.instance().currentFilter;
						filter.reset();
						filter.setMin( day.getTime() );
						filter.setMax( day.getTime() + ( 1440 * 60 * 1000 ) ); // One Day

						setSelectedPosition( position, mScheduleDataReceiver.filterRangeList( filter ), DateAndTime.now( DATEFORMAT, day ) );
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

		mDayView.getChildAt( position ).setBackgroundColor( 0xFFff4081 );

		if ( mDateDisplay != null && title != null )
			mDateDisplay.setText( title );
	}
}
