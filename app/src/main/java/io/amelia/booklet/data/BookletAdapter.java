package io.amelia.booklet.data;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.amelia.R;
import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.files.FileBuilder;
import io.amelia.android.support.DateAndTime;
import io.amelia.android.support.ExceptionHelper;

public class BookletAdapter extends BaseExpandableListAdapter
{
	public final ExpandableListView bookletListview;
	public final List<Container> booklets;
	private LayoutInflater inflater = null;

	public BookletAdapter( ExpandableListView bookletListview, List<Booklet> booklets )
	{
		this.bookletListview = bookletListview;
		this.inflater = ( LayoutInflater ) ContentManager.getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

		List<Container> origBooklets = new ArrayList<>();
		for ( Booklet booklet : booklets )
			origBooklets.add( new Container( origBooklets.size(), booklet ) );
		this.booklets = Collections.unmodifiableList( origBooklets );
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	public void computeVisibilities( String bookletId )
	{
		for ( Container container : booklets )
			if ( container.booklet.getId().equals( bookletId ) && container.childView != null && bookletListview.isGroupExpanded( container.inx ) )
				computeVisibilities( container.childView, container.booklet );
	}

	public void computeVisibilities( View view, Booklet booklet )
	{
		BookletState state = booklet.getState();

		Button buttonDownload = view.findViewById( R.id.download_download );
		Button buttonOpen = view.findViewById( R.id.download_open );
		Button buttonDelete = view.findViewById( R.id.download_delete );
		TextView textView = view.findViewById( R.id.message );

		textView.setText( "Booklet State: " + booklet.getState() );

		if ( state == BookletState.BUSY )
			view.setVisibility( View.GONE );
		else
		{
			view.setVisibility( View.VISIBLE );

			buttonDownload.setVisibility( state == BookletState.OUTDATED || state == BookletState.AVAILABLE ? View.VISIBLE : View.GONE );
			buttonDownload.setText( state == BookletState.OUTDATED ? "Update Booklet" : "Download Booklet" );
			buttonDownload.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View view )
				{
					booklet.goDownload();
				}
			} );

			buttonOpen.setVisibility( state == BookletState.READY ? View.VISIBLE : View.GONE );
			buttonOpen.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View view )
				{
					booklet.goOpen();
				}
			} );

			buttonDelete.setVisibility( state == BookletState.OUTDATED || state == BookletState.FAILURE || state == BookletState.READY ? View.VISIBLE : View.GONE );
			buttonDelete.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View view )
				{
					booklet.delete();
				}
			} );
		}
	}

	public View getBookletChildView( String bookletId )
	{
		for ( Container container : booklets )
			if ( container.booklet.getId().equals( bookletId ) )
				return container.childView;
		return null;
	}

	public View getBookletGroupView( String bookletId )
	{
		for ( Container container : booklets )
			if ( container.booklet.getId().equals( bookletId ) )
				return container.groupView;
		return null;
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
			Container container = booklets.get( groupPosition );
			container.childView = convertView;
			if ( container.childView == null )
				container.childView = inflater.inflate( R.layout.fragment_download_listitem_child, null );

			final Booklet booklet = container.booklet;
			View childView = container.childView;

			computeVisibilities( childView, booklet );

			return childView;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			Snackbar.make( parent, "There was a problem displaying this booklet. The problem was reported to the developer.", Snackbar.LENGTH_LONG ).show();
			return new View( ContentManager.getActivity() );
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
		return booklets.size();
	}

	@Override
	public long getGroupId( int groupPosition )
	{
		return groupPosition;
	}

	@Override
	public View getGroupView( final int position, boolean isExpanded, View convertView, final ViewGroup parent )
	{
		Container container = booklets.get( position );
		container.groupView = convertView;
		if ( container.groupView == null )
			container.groupView = inflater.inflate( R.layout.fragment_download_listitem, null );
		final Booklet booklet = container.booklet;
		View rowView = container.groupView;

		try
		{
			final ImageView bookletHeader = rowView.findViewById( R.id.booklet_header );
			bookletHeader.setImageResource( R.drawable.loading_image );
			new FileBuilder( "welcome-header-" + booklet.getId() ).withLocalFile( new File( booklet.getDataDirectory(), "header.png" ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + booklet.getId() + "/header.png" ).withCacheTimeout( ContentManager.getImageCacheTimeout() ).withExceptionHandler( new BiConsumer<String, Exception>()
			{
				@Override
				public void accept( String id, Exception exception )
				{
					ExceptionHelper.handleExceptionOnce( "welcome-header-" + booklet.getId(), new RuntimeException( "Failed to load image from server [images/" + booklet.getId() + "/header.png]", exception ) );
					bookletHeader.setImageResource( R.drawable.error );
				}
			} ).withImageView( bookletHeader ).request().start();

			TextView bookletTitle = rowView.findViewById( R.id.booklet_title );
			bookletTitle.setText( booklet.getDataTitle() );

			TextView bookletDate = rowView.findViewById( R.id.booklet_date );
			SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );
			bookletDate.setText( DateAndTime.formatDateRange( date.parse( booklet.getData().getString( Booklet.KEY_WHEN_FROM ) ), date.parse( booklet.getData().getString( Booklet.KEY_WHEN_TO ) ) ) );

			TextView bookletLocation = rowView.findViewById( R.id.booklet_where );
			bookletLocation.setText( booklet.getData().getString( Booklet.KEY_WHERE ) );

			LinearLayout bookletGroup = rowView.findViewById( R.id.booklet_group );
			bookletGroup.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					ImageView bookletCaret = rowView.findViewById( R.id.booklet_caret );
					ExpandableListView lv = ( ( ExpandableListView ) parent );
					if ( lv.isGroupExpanded( position ) )
					{
						lv.collapseGroup( position );
						bookletCaret.setImageResource( android.R.drawable.arrow_down_float );
					}
					else
					{
						lv.expandGroup( position );
						bookletCaret.setImageResource( android.R.drawable.arrow_up_float );
					}
				}
			} );
		}
		catch ( Exception e )
		{
			ExceptionHelper.handleExceptionOnce( "booklet-adapter-" + booklet.getId(), new RuntimeException( "Failure in booklet: " + booklet.getDataTitle() + " (" + booklet.getId() + ").", e ) );
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
		return booklets.isEmpty();
	}

	@Override
	public void onGroupCollapsed( int groupPosition )
	{

	}

	@Override
	public void onGroupExpanded( int groupPosition )
	{

	}

	public void showProgressBar( String bookletId, boolean show )
	{
		for ( Container container : booklets )
			if ( container.booklet.getId().equals( bookletId ) )
			{
				View groupView = container.groupView;
				if ( groupView != null )
				{
					ProgressBar progressBar = groupView.findViewById( R.id.booklet_progress );
					progressBar.setVisibility( show ? View.VISIBLE : View.GONE );
					progressBar.setIndeterminate( true );
				}

				return;
			}
	}

	public void updateProgressBar( String bookletId, Integer progressValue, Integer progressMax, Boolean isDone )
	{
		for ( Container container : booklets )
			if ( container.booklet.getId().equals( bookletId ) )
			{
				bookletListview.expandGroup( container.inx );

				View groupView = container.groupView;
				if ( groupView != null )
				{
					ProgressBar progressBar = groupView.findViewById( R.id.booklet_progress );
					progressBar.setMax( progressMax );
					progressBar.setProgress( progressValue );
					progressBar.setIndeterminate( isDone );
				}

				return;
			}
	}

	private class Container
	{
		final Booklet booklet;
		final int inx;
		View childView = null;
		View groupView = null;

		Container( int inx, Booklet booklet )
		{
			this.inx = inx;
			this.booklet = booklet;
		}
	}
}
