package io.amelia.booklet.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.data.BoundDataCallback;
import io.amelia.android.log.PLog;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.booklet.ui.fragment.ScheduleViewFragment;

public class ScheduleAdapter extends BaseExpandableListAdapter
{
	public static final SimpleDateFormat DISPLAY_FORMAT_DATE = new SimpleDateFormat( "MM/dd/yyyy" );
	public static final SimpleDateFormat DISPLAY_FORMAT_TIME12 = new SimpleDateFormat( "hh:mm a" );
	public static final SimpleDateFormat DISPLAY_FORMAT_TIME24 = new SimpleDateFormat( "HH:mm" );
	public SimpleDateFormat dateFormat;
	public List<ScheduleEventModel> events;
	public List<MapsLocationModel> locations;
	public SimpleDateFormat timeFormat;
	private BoundDataCallback alarmOnClick;
	private Context context;
	private LayoutInflater inflater = null;

	public ScheduleAdapter( Context context, BoundDataCallback alarmOnClick, SimpleDateFormat dateFormat, SimpleDateFormat timeFormat, List<MapsLocationModel> locations, List<ScheduleEventModel> events )
	{
		this.context = context;
		this.inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		this.alarmOnClick = alarmOnClick;
		this.dateFormat = dateFormat;
		this.timeFormat = timeFormat;
		this.locations = locations;
		this.events = events;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	@Override
	public Object getChild( int groupPosition, int childPosition )
	{
		return groupPosition * childPosition;
	}

	@Override
	public long getChildId( int groupPosition, int childPosition )
	{
		return childPosition;
	}

	@Override
	public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent )
	{
		try
		{
			View childView = convertView == null ? inflater.inflate( R.layout.fragment_schedule_listitem_child, null ) : convertView;

			TextView eventDescription = childView.findViewById( R.id.description );

			final ScheduleEventModel event = events.get( groupPosition );

			final ImageView imageButtonHeart = childView.findViewById( R.id.heart );

			if ( event.isHearted() )
			{
				imageButtonHeart.setSelected( true );
				imageButtonHeart.setColorFilter( context.getResources().getColor( R.color.colorAccent ) );
			}
			else
			{
				imageButtonHeart.setSelected( false );
				imageButtonHeart.setColorFilter( context.getResources().getColor( R.color.lighter_gray ) );
			}

			imageButtonHeart.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( final View v )
				{
					event.setHearted( !v.isSelected() );
					startAnimation( v, true );
				}
			} );

			final ImageView imageButtonAlarm = childView.findViewById( R.id.alarm );

			if ( event.hasEventPassed() )
				imageButtonAlarm.setVisibility( View.INVISIBLE );
			else
				imageButtonAlarm.setVisibility( View.VISIBLE );

			if ( event.hasEventId() )
			{
				imageButtonAlarm.setSelected( true );
				imageButtonAlarm.setColorFilter( context.getResources().getColor( R.color.colorAccent ) );
			}
			else
			{
				imageButtonAlarm.setSelected( false );
				imageButtonAlarm.setColorFilter( context.getResources().getColor( R.color.lighter_gray ) );
			}

			imageButtonAlarm.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View view )
				{
					BoundData boundData = new BoundData();
					boundData.put( "position", groupPosition );
					boundData.put( "selected", !view.isSelected() );
					alarmOnClick.call( boundData );
					startAnimation( view, false );
				}
			} );

			eventDescription.setText( event.getDescription() );
			// eventDescription.loadData("<p style=\"text-align: justified;\">" + event.getDescription() + "</p>", "text/html", "UTF-8");

			return childView;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			Toast.makeText( context, "There was a problem displaying this event. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
			return new View( context );
		}
	}

	@Override
	public int getChildrenCount( int groupPosition )
	{
		return 1;
	}

	@Override
	public long getCombinedChildId( long groupId, long childId )
	{
		return 0;
	}

	@Override
	public long getCombinedGroupId( long groupId )
	{
		return groupId;
	}

	@Override
	public Object getGroup( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public int getGroupCount()
	{
		return events.size();
	}

	@Override
	public long getGroupId( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public View getGroupView( final int position, boolean isExpanded, View convertView, ViewGroup parent )
	{
		View rowView = convertView == null ? inflater.inflate( R.layout.fragment_schedule_listitem, null ) : convertView;

		ScheduleEventModel event = events.get( position );

		try
		{
			TextView eventTitle = rowView.findViewById( R.id.title );
			TextView eventTime = rowView.findViewById( R.id.time );
			TextView eventLocation = rowView.findViewById( R.id.location );

			for ( MapsLocationModel loc : locations )
				if ( loc.id.equals( event.location ) )
					event.location = loc.id;

			ScheduleViewFragment.adapter = this;

			try
			{
				// Date date = dateFormat.parse(event.date);
				Date from = timeFormat.parse( event.time );
				Date to = new Date( from.getTime() + ( 60000 * ( event.duration == null ? 0 : Integer.parseInt( event.duration ) ) ) );

				boolean use24h = ContentManager.getPrefMilitaryTime();

				eventTitle.setText( event.getTitle() );

				String formatFrom = use24h ? DISPLAY_FORMAT_TIME24.format( from ) : DISPLAY_FORMAT_TIME12.format( from );
				String formatTo = use24h ? DISPLAY_FORMAT_TIME24.format( to ) : DISPLAY_FORMAT_TIME12.format( to );
				eventTime.setText( formatFrom + " — " + formatTo );
				eventLocation.setText( event.getLocation().title );
			}
			catch ( ParseException e )
			{
				e.printStackTrace();
			}

			/*
			rowView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					HomeActivity.instance.setFragment(ScheduleViewFragment.newInstance(position), true);
				}
			});
			*/
		}
		catch ( Exception e )
		{
			ExceptionHelper.handleExceptionOnce( "event-failure-" + event.id, new RuntimeException( "Failure in event: " + event.title + " (" + event.id + ").", e ) );
			PLog.e( e, "Failure in event: " + event.title + " (" + event.id + ")." );
		}

		return rowView;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable( int groupPosition, int childPosition )
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return events.isEmpty();
	}

	@Override
	public void onGroupCollapsed( int groupPosition )
	{

	}

	@Override
	public void onGroupExpanded( int groupPosition )
	{

	}

	public void startAnimation( final View v, final boolean op )
	{
		Animation pop = AnimationUtils.loadAnimation( context, R.anim.icon_fadeout );

		pop.setAnimationListener( new Animation.AnimationListener()
		{
			@Override
			public void onAnimationEnd( Animation animation )
			{
				v.setVisibility( View.INVISIBLE );

				v.setSelected( !v.isSelected() );

				if ( v instanceof ImageView )
					( ( ImageView ) v ).setColorFilter( context.getResources().getColor( v.isSelected() ? R.color.colorAccent : R.color.lighter_gray ) );

				Animation pop = AnimationUtils.loadAnimation( context, v.isSelected() ? ( op ? R.anim.icon_popout : R.anim.icon_shinkin ) : R.anim.icon_shinkin );

				pop.setAnimationListener( new Animation.AnimationListener()
				{
					@Override
					public void onAnimationEnd( Animation animation )
					{
						v.setVisibility( View.VISIBLE );
					}

					@Override
					public void onAnimationRepeat( Animation animation )
					{

					}

					@Override
					public void onAnimationStart( Animation animation )
					{

					}
				} );

				v.startAnimation( pop );
			}

			@Override
			public void onAnimationRepeat( Animation animation )
			{

			}

			@Override
			public void onAnimationStart( Animation animation )
			{

			}
		} );

		v.startAnimation( pop );
	}
}
