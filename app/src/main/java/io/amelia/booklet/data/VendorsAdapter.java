package io.amelia.booklet.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import io.amelia.R;
import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.files.FileBuilder;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.booklet.ui.activity.ContentActivity;
import io.amelia.booklet.ui.fragment.VendorViewFragment;

public class VendorsAdapter extends BaseExpandableListAdapter
{
	private Context context;
	private LayoutInflater inflater = null;
	private List<VendorsGroupModel> list;

	public VendorsAdapter( Context context, List<VendorsGroupModel> list )
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

		final VendorsGroupModel group = list.get( groupPosition );
		final VendorsModel vendorsModel = group.children.get( childPosition );

		// PLog.i("Group " + group.id + " // Vendor " + vendor.id);

		final ImageView imageViewThumbnail = listItemView.findViewById( R.id.guest_thumbnail );
		final TextView textViewTitle = listItemView.findViewById( R.id.guest_title );

		if ( vendorsModel.image == null )
			imageViewThumbnail.setImageResource( R.drawable.noimagefound );
		else
		{
			try
			{
				imageViewThumbnail.setImageResource( R.drawable.loading_image );
				new FileBuilder( "vendor-" + vendorsModel.id ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), "vendors/" + group.id + "/" + vendorsModel.image ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/vendors/" + group.id + "/" + vendorsModel.image ).withExceptionHandler( new BiConsumer<String, Exception>()
				{
					@Override
					public void accept( String id, Exception exception )
					{
						ExceptionHelper.handleExceptionOnce( "loading_failure_" + group.id + "_" + vendorsModel.image, new RuntimeException( "Failed to load image from Google Firebase [images/vendors/" + group.id + "/" + vendorsModel.image + "]", exception ) );
						imageViewThumbnail.setImageResource( R.drawable.error );
					}
				} ).withImageView( imageViewThumbnail ).request().start();
			}
			catch ( Exception e )
			{
				ExceptionHelper.handleExceptionOnce( "vendor-" + vendorsModel.id, new RuntimeException( "Failure in vendor: " + vendorsModel.title + " (" + vendorsModel.id + ").", e ) );
			}
		}

		textViewTitle.setText( vendorsModel.getTitle() );

		listItemView.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				ContentActivity.instance.stacker.setFragment( VendorViewFragment.instance( group.id, vendorsModel.id ), true, true );
			}
		} );

		return listItemView;
	}

	@Override
	public int getChildrenCount( int groupPosition )
	{
		List<VendorsModel> vendorsModels = list.get( groupPosition ).children;
		return vendorsModels == null ? 0 : vendorsModels.size();
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

	public VendorsGroupModel getListGroup( int groupPosition )
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
