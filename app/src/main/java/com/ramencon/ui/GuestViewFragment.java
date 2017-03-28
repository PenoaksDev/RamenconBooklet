package com.ramencon.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ramencon.R;
import com.ramencon.data.ImageCache;
import com.ramencon.data.guests.GuestDataReceiver;
import com.ramencon.data.models.ModelGroup;
import com.ramencon.data.models.ModelGuest;

import org.acra.ACRA;

public class GuestViewFragment extends Fragment implements ImageCache.ImageResolveTask.ImageFoundListener
{
	private ImageView image;

	public static Fragment instance( String groupId, String guestId )
	{
		Bundle bundle = new Bundle();
		bundle.putString( "groupId", groupId );
		bundle.putString( "guestId", guestId );

		GuestViewFragment frag = new GuestViewFragment();
		frag.setArguments( bundle );
		return frag;
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "View Guest" );

		View root = inflater.inflate( R.layout.fragment_guest_view, container, false );

		Bundle bundle = getArguments();

		assert bundle != null;

		ModelGroup listGroup = GuestDataReceiver.getInstance().getModel( bundle.getString( "groupId" ) );
		ModelGuest guest = listGroup.getModel( bundle.getString( "guestId" ) );

		image = ( ImageView ) root.findViewById( R.id.guest_view_image );
		TextView tv_title = ( TextView ) root.findViewById( R.id.guest_view_title );
		// TextView tv_desc = (TextView) root.findViewById(R.id.guest_view_description);
		WebView tv_desc = ( WebView ) root.findViewById( R.id.guest_view_description );

		tv_title.setText( guest.getTitle() );
		//tv_desc.setText(guest.getDescription());
		tv_desc.setBackgroundColor( root.getDrawingCacheBackgroundColor() );
		tv_desc.loadData( "<p style=\"text-align: justified;\">" + guest.getDescription() + "</p>", "text/html", "UTF-8" );

		if ( guest.image == null )
			image.setImageResource( R.drawable.noimagefound );
		else
		{
			image.setImageResource( R.drawable.loading_image );
			ImageCache.cacheRemoteImage( getActivity(), "guest-" + guest.id, ImageCache.REMOTE_IMAGES_URL + "guests/" + listGroup.id + "/" + guest.image, false, this, null );
		}

		return root;
	}

	@Override
	public void update( Bitmap bitmap )
	{
		if ( image != null )
			image.setImageBitmap( bitmap );
	}

	@Override
	public void error( Exception exception )
	{
		if ( image != null )
			image.setImageResource( R.drawable.error );

		ACRA.getErrorReporter().handleException( new RuntimeException( "Recoverable Exception", exception ) );
		Toast.makeText( getContext(), "We had a problem loading the guest image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
	}
}
