package com.ramencon.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ramencon.R;
import com.ramencon.data.ImageCache;
import com.ramencon.data.models.ModelMap;
import com.ramencon.ui.widget.TouchImageView;

import org.acra.ACRA;

import java.util.List;

public class MapsChildFragment extends Fragment implements ImageCache.ImageResolveTask.ImageFoundListener, ImageCache.ImageResolveTask.ImageProgressListener
{
	public static List<ModelMap> maps;

	private ProgressBar progressBar = null;
	private TouchImageView image;
	private ModelMap map;
	private boolean isRefresh;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		assert getArguments() != null;
		map = maps.get( getArguments().getInt( "index" ) );
		isRefresh = getArguments().getBoolean( "isRefresh", false );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.fragment_maps_child, container, false );

		image = ( TouchImageView ) view.findViewById( R.id.maps_image );
		progressBar = ( ProgressBar ) view.findViewById( R.id.progress_bar_map );
		progressBar.setVisibility( View.VISIBLE );

		image.setMaxZoom( 4 );
		image.setScaleType( ImageView.ScaleType.MATRIX );

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if ( map.image == null )
			image.setImageResource( R.drawable.noimagefound );
		else
		{
			image.setImageResource( R.drawable.loading_image );
			ImageCache.cacheRemoteImage( "map-" + map.id, ImageCache.REMOTE_IMAGES_URL + "images/maps/" + map.image, isRefresh, this, this );
		}
	}

	@Override
	public void update( Bitmap bitmap )
	{
		image.setImageBitmap( bitmap );
	}

	@Override
	public void error( Exception exception )
	{
		if ( image != null )
			image.setImageResource( R.drawable.error );

		ACRA.getErrorReporter().handleException( new RuntimeException( "Recoverable Exception", exception ) );
		Toast.makeText( getContext(), "We had a problem loading the map. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
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
	public void progress( long bytesTransferred, long totalByteCount )
	{
		if ( progressBar != null )
		{
			progressBar.setMax( ( int ) totalByteCount );
			progressBar.setProgress( ( int ) bytesTransferred );
		}
	}

	@Override
	public void finish()
	{
		if ( progressBar != null )
			progressBar.setVisibility( View.GONE );
	}
}
