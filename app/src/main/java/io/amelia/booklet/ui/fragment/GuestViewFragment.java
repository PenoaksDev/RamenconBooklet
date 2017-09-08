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
import io.amelia.booklet.data.GuestsGroupModel;
import io.amelia.booklet.data.GuestsHandler;
import io.amelia.booklet.data.GuestsModel;
import io.amelia.booklet.ui.activity.ContentActivity;

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
		View root = inflater.inflate( R.layout.fragment_guests_view, container, false );

		try
		{
			getActivity().setTitle( "View Guest" );

			Bundle bundle = getArguments();

			GuestsGroupModel listGroup = ContentManager.getActiveBooklet().getSectionHandler( GuestsHandler.class ).getModel( bundle.getString( "groupId" ) );
			GuestsModel guest = listGroup.getModel( bundle.getString( "guestId" ) );

			ImageView image = root.findViewById( R.id.guest_view_image );


			TextView textViewTitle = root.findViewById( R.id.guest_view_title );
			textViewTitle.setText( guest.getTitle() );

			WebView textViewDescription = root.findViewById( R.id.guest_view_description );
			textViewDescription.setBackgroundColor( root.getDrawingCacheBackgroundColor() );
			textViewDescription.loadData( "<p style=\"text-align: justified;\">" + guest.getDescription() + "</p><p>" + guest.getFormattedUrls() + "</p>", "text/html", "UTF-8" );

			try
			{
				FileBuilder fileBuilder = new FileBuilder( "guest-view-image-" + guest.id, FileFutureBitmap.class ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), "guests/" + listGroup.id + "/" + guest.image ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/guests/" + listGroup.id + "/" + guest.image ).withExceptionHandler( new BiConsumer<String, Throwable>()
				{
					@Override
					public void accept( String id, Throwable exception )
					{
						image.setImageResource( R.drawable.error );

						ExceptionHelper.handleExceptionOnce( "guest-view-image-error-" + id, new RuntimeException( "Recoverable Exception", exception ) );
						Toast.makeText( getContext(), "We had a problem loading the guest image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
					}
				} ).withImageView( image );

				if ( Objs.isEmpty( guest.image ) )
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
				ExceptionHelper.handleExceptionOnce( "guest-view-image-" + guest.id, new RuntimeException( "Failure in guest view: " + guest.title + " (" + guest.id + ").", e ) );
				Toast.makeText( getContext(), "We had a problem loading the guest image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
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

	@Override
	public void onResume()
	{
		super.onResume();
	}
}
