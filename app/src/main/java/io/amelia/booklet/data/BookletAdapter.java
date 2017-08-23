package io.amelia.booklet.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramencon.R;

import java.text.SimpleDateFormat;
import java.util.List;

import io.amelia.android.data.ImageCache;
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.DateAndTime;
import io.amelia.booklet.Booklet;
import io.amelia.booklet.ui.activity.DownloadActivity;

public class BookletAdapter extends BaseExpandableListAdapter
{
	public List<Booklet> booklets;
	private LayoutInflater inflater = null;
	private DownloadActivity activity;

	public BookletAdapter( DownloadActivity activity, List<Booklet> booklets )
	{
		this.inflater = ( LayoutInflater ) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		this.activity = activity;

		this.booklets = booklets;
	}

	@Override
	public int getGroupCount()
	{
		return booklets.size();
	}

	@Override
	public int getChildrenCount( int groupPosition )
	{
		return 1;
	}

	@Override
	public Object getGroup( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public Object getChild( int groupPosition, int childPosition )
	{
		return groupPosition * childPosition;
	}

	@Override
	public long getGroupId( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public long getChildId( int groupPosition, int childPosition )
	{
		return childPosition;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public View getGroupView( final int position, boolean isExpanded, View convertView, final ViewGroup parent )
	{
		View rowView = convertView == null ? inflater.inflate( R.layout.activity_download_listitem, null ) : convertView;

		final Booklet booklet = booklets.get( position );

		try
		{
			final ImageView booklet_header = ( ImageView ) rowView.findViewById( R.id.booklet_header );
			TextView booklet_title = ( TextView ) rowView.findViewById( R.id.booklet_title );
			Button booket_info = ( Button ) rowView.findViewById( R.id.booklet_info );
			FrameLayout booklet_view = ( FrameLayout ) rowView.findViewById( R.id.booklet_view );

			if ( booklet.getDataImage() == null )
				booklet_header.setImageResource( R.drawable.noimagefound );
			else
			{
				booklet_header.setImageResource( R.drawable.loading_image );
				ImageCache.cacheRemoteImage( parent.getContext(), "booklet-" + booklet.getId(), ImageCache.REMOTE_IMAGES_URL + "booklet-headers/" + booklet.getDataImage(), false, new ImageCache.ImageResolveTask.ImageFoundListener()
				{
					@Override
					public void update( Bitmap bitmap )
					{
						booklet_header.setImageBitmap( bitmap );
					}

					@Override
					public void error( Exception exception )
					{
						ACRAHelper.handleExceptionOnce( "loading_failure_" + booklet.getId() + "_" + booklet.getDataImage(), new RuntimeException( "Failed to load image from server [images/booklet-headers/" + booklet.getDataImage() + "]", exception ) );
						booklet_header.setImageResource( R.drawable.error );
					}
				}, null );
			}

			booklet_title.setText( booklet.getDataTitle() );

			booket_info.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					ExpandableListView lv = ( ( ExpandableListView ) parent );
					if ( lv.isGroupExpanded( position ) )
						lv.collapseGroup( position );
					else
						lv.expandGroup( position );
				}
			} );

			final Runnable postTask = new Runnable()
			{
				@Override
				public void run()
				{
					activity.onListItemClick( booklet );
				}
			};

			booklet_view.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					activity.onListItemClick( booklet );
					// booklet.updateAndOpen( v, false, postTask );
				}
			} );

			booklet_view.setOnLongClickListener( new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick( View v )
				{
					/* Open Menu
					 * -> Open (If READY or OUTDATED)
					 * -> Delete (If READY or OUTDATED)
					 * -> Update (If OUTDATED)
					 * -> Download (If AVAILABLE)
					 */
					// booklet.updateAndOpen( v, true, postTask );
					booklet.goDownload( v );
					return true;
				}
			} );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			ACRAHelper.handleExceptionOnce( "booklet-adapter-" + booklet.getId(), new RuntimeException( "Failure in booklet: " + booklet.getDataTitle() + " (" + booklet.getId() + ").", e ) );
		}

		return rowView;
	}

	@Override
	public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent )
	{
		try
		{
			View childView = convertView == null ? inflater.inflate( R.layout.activity_download_listitem_child, null ) : convertView;

			final Booklet booklet = booklets.get( groupPosition );

			TextView booklet_date = ( TextView ) childView.findViewById( R.id.booklet_date );
			TextView booklet_location = ( TextView ) childView.findViewById( R.id.booklet_location );
			TextView booklet_description = ( TextView ) childView.findViewById( R.id.booklet_description );

			SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );
			booklet_date.setText( DateAndTime.formatDateRange( date.parse( booklet.getData().getString( Booklet.KEY_WHEN_FROM ) ), date.parse( booklet.getData().getString( Booklet.KEY_WHEN_TO ) ) ) );
			booklet_location.setText( booklet.getData().getString( Booklet.KEY_WHERE ) );
			booklet_description.setText( booklet.getDataDescription() );

			return childView;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			Snackbar.make( parent, "There was a problem displaying this booklet. The problem was reported to the developer.", Snackbar.LENGTH_LONG ).show();
			return new View( activity );
		}
	}

	@Override
	public boolean isChildSelectable( int groupPosition, int childPosition )
	{
		return false;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return booklets.isEmpty();
	}

	@Override
	public void onGroupExpanded( int groupPosition )
	{

	}

	@Override
	public void onGroupCollapsed( int groupPosition )
	{

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
}
