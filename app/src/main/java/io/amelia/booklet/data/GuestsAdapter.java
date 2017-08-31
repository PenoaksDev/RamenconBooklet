package io.amelia.booklet.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.amelia.R;
import io.amelia.android.data.ImageCache;
import io.amelia.android.support.ACRAHelper;
import io.amelia.booklet.ui.activity.ContentActivity;
import io.amelia.booklet.ui.fragment.GuestViewFragment;

public class GuestsAdapter extends BaseExpandableListAdapter
{
	private Context context;
	private LayoutInflater inflater = null;
	private List<GuestsGroupModel> list;

	public GuestsAdapter( Context context, List<GuestsGroupModel> list )
	{
		this.inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		this.context = context;

		assert list != null;
		this.list = list;
	}

	@Override
	public Object getChild( int groupPosition, int childPosition )
	{
		return childPosition;
	}

	@Override
	public long getChildId( int groupPosition, int childPosition )
	{
		return childPosition;
	}

	@Override
	public View getChildView( final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent )
	{
		View listItemView = convertView == null ? inflater.inflate( R.layout.fragment_guests_listitem, null ) : convertView;

		final GuestsGroupModel group = list.get( groupPosition );
		final GuestsModel guest = group.children.get( childPosition );

		// PLog.i("Group " + group.id + " // Guest " + guest.id);

		final ImageView iv_thumbnail = ( ImageView ) listItemView.findViewById( R.id.guest_thumbnail );
		final TextView tv_title = ( TextView ) listItemView.findViewById( R.id.guest_title );

		if ( guest.image == null )
			iv_thumbnail.setImageResource( R.drawable.noimagefound );
		else
		{
			iv_thumbnail.setImageResource( R.drawable.loading_image );
			ImageCache.cacheRemoteImage( context, "guest-" + guest.id, ImageCache.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/guests/" + group.id + "/" + guest.image, false, new ImageCache.ImageResolveTask.ImageFoundListener()
			{
				@Override
				public void error( Exception exception )
				{
					ACRAHelper.handleExceptionOnce( "loading_failure_" + group.id + "_" + guest.image, new RuntimeException( "Failed to load image from Google Firebase [images/guests/" + group.id + "/" + guest.image + "]", exception ) );
					iv_thumbnail.setImageResource( R.drawable.error );
				}

				@Override
				public void update( Bitmap bitmap )
				{
					iv_thumbnail.setImageBitmap( bitmap );
				}
			}, null );
		}

		tv_title.setText( guest.getTitle() );

		listItemView.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				ContentActivity.instance.stacker.setFragment( GuestViewFragment.instance( group.id, guest.id ), true, true );
			}
		} );

		return listItemView;
	}

	@Override
	public int getChildrenCount( int groupPosition )
	{
		List<GuestsModel> guests = list.get( groupPosition ).children;
		return guests == null ? 0 : guests.size();
	}

	@Override
	public Object getGroup( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public int getGroupCount()
	{
		return list.size();
	}

	@Override
	public long getGroupId( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent )
	{
		View group = convertView == null ? inflater.inflate( R.layout.fragment_guests_listheader, null ) : convertView;

		( ( TextView ) group.findViewById( R.id.group_title ) ).setText( list.get( groupPosition ).title );

		return group;
	}

	public GuestsGroupModel getListGroup( int groupPosition )
	{
		return list.get( groupPosition );
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable( int groupPosition, int childPosition )
	{
		return true;
	}
}
