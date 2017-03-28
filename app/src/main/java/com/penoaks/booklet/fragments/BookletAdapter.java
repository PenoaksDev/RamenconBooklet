package com.penoaks.booklet.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.penoaks.android.log.PLog;
import com.penoaks.booklet.models.ModelBooklet;
import com.penoaks.configuration.types.json.JsonConfiguration;
import com.ramencon.R;

import java.util.ArrayList;
import java.util.List;

public class BookletAdapter extends RecyclerView.Adapter<BookletAdapter.ViewHolder>
{
	private final BookletFragment fragment;
	private final List<BookletMenu> bookletMenus = new ArrayList<>();

	public BookletAdapter( BookletFragment fragment, ModelBooklet modelBooklet )
	{
		this.fragment = fragment;

		JsonConfiguration data = modelBooklet.getBookletData();

		if ( data.isConfigurationSection( "schedule" ) )
			bookletMenus.add( new BookletMenu( "nav_schedule", R.drawable.ic_menu_schedule, "Schedule" ) );

		if ( data.isConfigurationSection( "guests" ) )
			bookletMenus.add( new BookletMenu( "nav_guests_vendors", R.drawable.ic_menu_friends, "Guests and Vendors" ) );

		if ( data.isConfigurationSection( "maps" ) )
			bookletMenus.add( new BookletMenu( "nav_maps", R.drawable.ic_menu_map, "Maps and Locations" ) );

		PLog.i( "Data: " + data + " -- " + bookletMenus );
	}

	@Override
	public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
	{
		View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.fragment_booklet_listitem, parent, false );
		return new ViewHolder( view );
	}

	@Override
	public void onBindViewHolder( final ViewHolder holder, int position )
	{
		holder.mMenu = bookletMenus.get( position );
		holder.iImageView.setImageDrawable( fragment.getResources().getDrawable( holder.mMenu.icon ) );
		holder.mTextView.setText( holder.mMenu.title );

		holder.mView.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				fragment.onListItemClick( holder.mMenu );
			}
		} );
	}

	@Override
	public int getItemCount()
	{
		return bookletMenus.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		public final View mView;
		public final ImageView iImageView;
		public final TextView mTextView;
		public BookletMenu mMenu;

		public ViewHolder( View view )
		{
			super( view );
			mView = view;
			iImageView = ( ImageView ) view.findViewById( R.id.booklet_menu_image );
			mTextView = ( TextView ) view.findViewById( R.id.booklet_menu_text );
		}

		@Override
		public String toString()
		{
			return super.toString() + " '" + mTextView.getText() + "'";
		}
	}

	public class BookletMenu
	{
		final String id;
		final int icon;
		final String title;

		public BookletMenu( String id, int icon, String title )
		{
			this.id = id;
			this.icon = icon;
			this.title = title;
		}

		@Override
		public String toString()
		{
			return "{id " + id + ", icon " + icon + ", title: " + title + "}";
		}
	}
}
