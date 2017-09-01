package io.amelia.booklet.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.acra.ACRA;

import java.util.List;

import io.amelia.R;
import io.amelia.android.data.ImageCache;
import io.amelia.android.ui.widget.TouchImageView;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.GuidePageModel;

public class GuideChildFragment extends Fragment implements ImageCache.ImageFoundListener, ImageCache.ImageProgressListener
{
	public static List<GuidePageModel> guideMapModels;
	private GuidePageModel guidePageModel;
	private TouchImageView image;
	private boolean isRefresh;
	private ProgressBar progressBar = null;

	@Override
	public void error( Exception exception )
	{
		if ( image != null )
			image.setImageResource( R.drawable.error );

		ACRA.getErrorReporter().handleException( new RuntimeException( "Recoverable Exception", exception ) );
		Toast.makeText( getContext(), "We had a problem loading the guide. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
	}

	@Override
	public void finish()
	{
		if ( progressBar != null )
			progressBar.setVisibility( View.GONE );
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		assert getArguments() != null;
		guidePageModel = guideMapModels.get( getArguments().getInt( "index" ) );
		isRefresh = getArguments().getBoolean( "isRefresh", false );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.fragment_guide_child, container, false );

		image = view.findViewById( R.id.guide_image );
		progressBar = view.findViewById( R.id.guide_progressbar );
		progressBar.setVisibility( View.VISIBLE );

		image.setMaxZoom( 4 );
		image.setScaleType( ImageView.ScaleType.MATRIX );

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if ( guidePageModel.image == null )
			image.setImageResource( R.drawable.noimagefound );
		else
		{
			image.setImageResource( R.drawable.loading_image );
			ImageCache.cacheRemoteImage( getActivity(), "guide-" + guidePageModel.pageNo, ImageCache.REMOTE_IMAGES_URL + ContentManager.getActiveBooklet().getId() + "/guide/" + guidePageModel.image, "guide/" + guidePageModel.image, isRefresh, this, this );
		}
	}

	@Override
	public void progress( long bytesTransferred, long totalByteCount )
	{
		if ( progressBar != null )
		{
			progressBar.setMax( ( int ) totalByteCount );
			progressBar.setProgress( ( int ) bytesTransferred );
		}
	}

	@Override
	public void start()
	{
		if ( progressBar != null )
		{
			progressBar.setProgress( 0 );
			progressBar.setVisibility( View.VISIBLE );
		}
	}

	@Override
	public void update( Bitmap bitmap )
	{
		image.setImageBitmap( bitmap );
	}
}
