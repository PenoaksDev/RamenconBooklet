package com.penoaks.booklet.data.schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.penoaks.android.utils.UtilDate;
import com.ramencon.R;
import com.penoaks.booklet.data.models.ModelEvent;
import com.penoaks.booklet.data.schedule.filters.DefaultScheduleFilter;
import com.penoaks.booklet.ui.fragments.ScheduleFragment;

import org.lucasr.twowayview.TwoWayView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduleDayAdapter extends BaseAdapter
{
	public static final String DATEFORMAT = "MMMM dx yyyy";

	private LayoutInflater inflater = null;
	private List<Date> days = new ArrayList<>();
	private TextView mDateDisplay;
	private ScheduleDataReceiver mScheduleDataReceiver;
	private ExpandableListView mListView;
	private TwoWayView mDayView;
	private int selectedPosition;

	public ScheduleDayAdapter( ScheduleFragment parent, List<Date> days, TextView mDateDisplay, ScheduleDataReceiver mScheduleDataReceiver, ExpandableListView mListView, TwoWayView mDayView, int selectedPosition )
	{
		this.inflater = ( LayoutInflater ) parent.getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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

	public void setSelectedPosition( int position, List<ModelEvent> data, String title ) throws ParseException
	{
		if ( mDayView.getChildAt( position ) == null )
			throw new IndexOutOfBoundsException();

		ScheduleDayAdapter.this.selectedPosition = position;

		mListView.setAdapter( new ScheduleAdapter( HomeActivity.instance, mScheduleDataReceiver.simpleDateFormat(), mScheduleDataReceiver.simpleTimeFormat(), mScheduleDataReceiver.locations, data ) );

		for ( int i = 0; i < mDayView.getChildCount(); i++ )
			if ( i != position )
				mDayView.getChildAt( i ).setBackgroundColor( 0x00000000 );

		mDayView.getChildAt( position ).setBackgroundColor( 0xFFff4081 );

		if ( mDateDisplay != null && title != null )
			mDateDisplay.setText( title );
	}

	@Override
	public View getView( final int position, View convertView, ViewGroup parent )
	{
		View view = inflater.inflate( R.layout.fragment_schedule_listitem_day, null );

		TextView dayName = ( TextView ) view.findViewById( R.id.day_name );
		TextView dayNumber = ( TextView ) view.findViewById( R.id.day_number );
		ImageView dayImage = ( ImageView ) view.findViewById( R.id.day_image );

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

			dayName.setText( UtilDate.now( "EEE", day ) );
			dayNumber.setText( UtilDate.now( "d", day ) );

			if ( position == selectedPosition && mDateDisplay != null )
				mDateDisplay.setText( UtilDate.now( DATEFORMAT, day ) );

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

						setSelectedPosition( position, mScheduleDataReceiver.filterRangeList( filter ), UtilDate.now( DATEFORMAT, day ) );
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

	public int getSelectedPosition()
	{
		return selectedPosition;
	}
}
