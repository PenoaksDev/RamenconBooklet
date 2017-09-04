package io.amelia.booklet.ui.fragment;

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

import org.acra.ACRA;

import io.amelia.R;
import io.amelia.android.data.ImageCache;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.GuestsHandler;
import io.amelia.booklet.data.VendorsGroupModel;
import io.amelia.booklet.data.VendorsHandler;
import io.amelia.booklet.data.VendorsModel;

public class VendorViewFragment extends Fragment implements ImageCache.ImageFoundListener
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
	public void error( Exception exception )
	{
		if ( image != null )
			image.setImageResource( R.drawable.error );

		ACRA.getErrorReporter().handleException( new RuntimeException( "Recoverable Exception", exception ) );
		Toast.makeText( getContext(), "We had a problem loading the vendor image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "View Vendor" );

		View root = inflater.inflate( R.layout.fragment_guests_view, container, false );

		Bundle bundle = getArguments();

		assert bundle != null;

		VendorsGroupModel listGroup = ContentManager.getActiveBooklet().getSectionHandler( VendorsHandler.class ).getModel( bundle.getString( "groupId" ) );
		VendorsModel vendor = listGroup.getModel( bundle.getString( "vendorId" ) );

		image = root.findViewById( R.id.guest_view_image );
		TextView tv_title = root.findViewById( R.id.guest_view_title );
		// TextView tv_desc =  root.findViewById(R.id.guest_view_description);
		WebView tv_desc = root.findViewById( R.id.guest_view_description );

		tv_title.setText( vendor.getTitle() );
		//tv_desc.setText(vendor.getDescription());
		tv_desc.setBackgroundColor( root.getDrawingCacheBackgroundColor() );
		tv_desc.loadData( "<p style=\"text-align: justified;\">" + vendor.getDescription() + "</p>", "text/html", "UTF-8" );

		if ( vendor.image == null )
			image.setImageResource( R.drawable.noimagefound );
		else
		{
			image.setImageResource( R.drawable.loading_image );
			ImageCache.cacheRemoteImage( getActivity(), "vendor-" + vendor.id, ImageCache.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/vendors/" + listGroup.id + "/" + vendor.image, "vendors/" + listGroup.id + "/" + vendor.image, null, false, this, null );
		}

		return root;
	}

	@Override
	public void update( Bitmap bitmap )
	{
		if ( image != null )
			image.setImageBitmap( bitmap );
	}
}
