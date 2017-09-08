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
import io.amelia.android.files.FileBuilderException;
import io.amelia.android.files.FileFutureBitmap;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.VendorsGroupModel;
import io.amelia.booklet.data.VendorsHandler;
import io.amelia.booklet.data.VendorsModel;
import io.amelia.booklet.ui.activity.ContentActivity;

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

	// private ImageView image;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View root = inflater.inflate( R.layout.fragment_guests_view, container, false );

		try
		{
			getActivity().setTitle( "View Vendor" );

			Bundle bundle = getArguments();

			assert bundle != null;

			VendorsGroupModel listGroup = ContentManager.getActiveBooklet().getSectionHandler( VendorsHandler.class ).getModel( bundle.getString( "groupId" ) );
			VendorsModel vendor = listGroup.getModel( bundle.getString( "vendorId" ) );

			ImageView image = root.findViewById( R.id.guest_view_image );

			TextView textViewTitle = root.findViewById( R.id.guest_view_title );
			textViewTitle.setText( vendor.getTitle() );

			WebView textViewDescription = root.findViewById( R.id.guest_view_description );
			textViewDescription.setBackgroundColor( root.getDrawingCacheBackgroundColor() );
			textViewDescription.loadData( "<p style=\"text-align: justified;\">" + vendor.getDescription() + "</p><p>" + vendor.getFormattedUrls() + "</p>", "text/html", "UTF-8" );

			try
			{
				FileBuilder fileBuilder = new FileBuilder( "vendor-view-image-" + vendor.id, FileFutureBitmap.class ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), "vendors/" + listGroup.id + "/" + vendor.image ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/vendors/" + listGroup.id + "/" + vendor.image ).withExceptionHandler( new BiConsumer<String, Throwable>()
				{
					@Override
					public void accept( String id, Throwable exception )
					{
						image.setImageResource( R.drawable.error );

						ExceptionHelper.handleExceptionOnce( "vendor-view-image-error-" + id, new RuntimeException( "Recoverable Exception", exception ) );
						Toast.makeText( getContext(), "We had a problem loading the vendor image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
					}
				} ).withImageView( image );

				if ( Objs.isEmpty( vendor.image ) )
					image.setImageResource( R.drawable.noimagefound );
				else
				{
					image.setImageResource( R.drawable.loading_image );
					fileBuilder.request().start();
				}

				image.setOnLongClickListener( new View.OnLongClickListener()
				{
					@Override
					public boolean onLongClick( View view )
					{
						try
						{
							fileBuilder.forceDownload( true ).request().start();
						}
						catch ( FileBuilderException e )
						{
							// Ignore
						}

						return true;
					}
				} );
			}
			catch ( Exception e )
			{
				image.setImageResource( R.drawable.error );
				ExceptionHelper.handleExceptionOnce( "guest-view-image-" + vendor.id, new RuntimeException( "Failure in vendor view: " + vendor.title + " (" + vendor.id + ").", e ) );
				Toast.makeText( getContext(), "We had a problem loading the vendor image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
			}
		}
		catch ( Exception e )
		{
			ExceptionHelper.handleExceptionOnce( "guest-view", e );
			Toast.makeText( getContext(), "Internal Error!", Toast.LENGTH_LONG ).show();
			( ( ContentActivity ) getActivity() ).stacker.popBackstack();
		}

		return root;
	}
}
