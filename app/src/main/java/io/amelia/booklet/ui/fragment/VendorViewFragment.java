package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.amelia.R;
import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.files.FileBuilder;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.VendorsGroupModel;
import io.amelia.booklet.data.VendorsHandler;
import io.amelia.booklet.data.VendorsModel;

public class VendorViewFragment extends Fragment
{
	public static Fragment instance( String groupId, String vendorId )
	{
		Bundle bundle = new Bundle();
		bundle.putString( "groupId", groupId );
		bundle.putString( "vendorId", vendorId );

		VendorViewFragment frag = new VendorViewFragment();
		frag.setArguments( bundle );
		return frag;
	}

	private ImageView image;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "View Vendor" );

		View root = inflater.inflate( R.layout.fragment_guests_view, container, false );

		Bundle bundle = getArguments();

		assert bundle != null;

		VendorsGroupModel listGroup = ContentManager.getActiveBooklet().getSectionHandler( VendorsHandler.class ).getModel( bundle.getString( "groupId" ) );
		VendorsModel vendor = listGroup.getModel( bundle.getString( "vendorId" ) );

		try
		{
			image = root.findViewById( R.id.guest_view_image );
			TextView textViewTitle = root.findViewById( R.id.guest_view_title );
			WebView textViewDescription = root.findViewById( R.id.guest_view_description );

			textViewTitle.setText( vendor.getTitle() );
			textViewDescription.setBackgroundColor( root.getDrawingCacheBackgroundColor() );
			textViewDescription.loadData( "<p style=\"text-align: justified;\">" + vendor.getDescription() + "</p>", "text/html", "UTF-8" );

			if ( vendor.image == null )
				image.setImageResource( R.drawable.noimagefound );
			else
			{
				image.setImageResource( R.drawable.loading_image );
				new FileBuilder( "vendor-view-image-" + vendor.id ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), "vendors/" + listGroup.id + "/" + vendor.image ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/vendors/" + listGroup.id + "/" + vendor.image ).withExceptionHandler( new BiConsumer<String, Exception>()
				{
					@Override
					public void accept( String id, Exception exception )
					{
						image.setImageResource( R.drawable.error );

						ExceptionHelper.handleExceptionOnce( "vendor-view-image-error-" + id, new RuntimeException( "Recoverable Exception", exception ) );
						Toast.makeText( getContext(), "We had a problem loading the vendor image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
					}
				} ).withImageView( image ).request().start();
			}
		}
		catch ( Exception e )
		{
			image.setImageResource( R.drawable.error );
			ExceptionHelper.handleExceptionOnce( "guest-view-image-" + vendor.id, new RuntimeException( "Failure in vendor view: " + vendor.title + " (" + vendor.id + ").", e ) );
			Toast.makeText( getContext(), "We had a problem loading the vendor image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
		}

		return root;
	}
}
