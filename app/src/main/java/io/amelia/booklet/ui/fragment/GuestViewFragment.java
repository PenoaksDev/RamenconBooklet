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
import io.amelia.booklet.data.GuestsGroupModel;
import io.amelia.booklet.data.GuestsHandler;
import io.amelia.booklet.data.GuestsModel;

public class GuestViewFragment extends Fragment
{
	public static Fragment instance( String groupId, String guestId )
	{
		Bundle bundle = new Bundle();
		bundle.putString( "groupId", groupId );
		bundle.putString( "guestId", guestId );

		GuestViewFragment frag = new GuestViewFragment();
		frag.setArguments( bundle );
		return frag;
	}

	// private ImageView image;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "View Guest" );

		View root = inflater.inflate( R.layout.fragment_guests_view, container, false );

		Bundle bundle = getArguments();

		GuestsGroupModel listGroup = ContentManager.getActiveBooklet().getSectionHandler( GuestsHandler.class ).getModel( bundle.getString( "groupId" ) );
		GuestsModel guest = listGroup.getModel( bundle.getString( "guestId" ) );

		ImageView image = root.findViewById( R.id.guest_view_image );

		try
		{
			TextView textViewTitle = root.findViewById( R.id.guest_view_title );
			textViewTitle.setText( guest.getTitle() );

			WebView textViewDescription = root.findViewById( R.id.guest_view_description );
			textViewDescription.setBackgroundColor( root.getDrawingCacheBackgroundColor() );
			textViewDescription.loadData( "<p style=\"text-align: justified;\">" + guest.getDescription() + "</p>", "text/html", "UTF-8" );

			if ( guest.image == null )
				image.setImageResource( R.drawable.noimagefound );
			else
			{
				image.setImageResource( R.drawable.loading_image );
				new FileBuilder( "guest-view-image-" + guest.id ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), "guests/" + listGroup.id + "/" + guest.image ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/guests/" + listGroup.id + "/" + guest.image ).withExceptionHandler( new BiConsumer<String, Exception>()
				{
					@Override
					public void accept( String id, Exception exception )
					{
						image.setImageResource( R.drawable.error );

						ExceptionHelper.handleExceptionOnce( "guest-view-image-error-" + id, new RuntimeException( "Recoverable Exception", exception ) );
						Toast.makeText( getContext(), "We had a problem loading the guest image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
					}
				} ).withImageView( image ).request().start();
			}
		}
		catch ( Exception e )
		{
			image.setImageResource( R.drawable.error );
			ExceptionHelper.handleExceptionOnce( "guest-view-image-" + guest.id, new RuntimeException( "Failure in guest view: " + guest.title + " (" + guest.id + ").", e ) );
			Toast.makeText( getContext(), "We had a problem loading the guest image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
		}

		return root;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
}
