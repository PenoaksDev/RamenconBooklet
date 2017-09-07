package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import io.amelia.R;
import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.files.FileBuilder;
import io.amelia.android.files.FileRequestHandler;
import io.amelia.android.files.FileResult;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.android.support.Objs;
import io.amelia.android.ui.widget.TouchImageView;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.GuidePageModel;

public class GuideChildFragment extends Fragment
{
	private GuidePageModel guidePageModel;
	private FileRequestHandler handler = null;
	// private TouchImageView image;
	private boolean isRefresh;
	// private ProgressBar progressBar = null;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		assert getArguments() != null;
		guidePageModel = GuideFragment.instance().getHandler().guidePageModels.get( getArguments().getInt( "index" ) );
		isRefresh = getArguments().getBoolean( "isRefresh", false );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_guide_child, container, false );
	}

	@Override
	public void onStart()
	{
		super.onStart();

		TouchImageView image = getView().findViewById( R.id.guide_image );
		image.setMaxZoom( 4 );
		image.setScaleType( ImageView.ScaleType.MATRIX );

		ProgressBar progressBar = getView().findViewById( R.id.guide_progressbar );
		progressBar.setVisibility( View.VISIBLE );

		if ( Objs.isEmpty( guidePageModel.image ) )
			image.setImageResource( R.drawable.noimagefound );
		else
		{
			image.setImageResource( R.drawable.loading_image );
			try
			{
				handler = new FileBuilder( "guide-image-" + guidePageModel.pageNo ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), guidePageModel.getLocalImage() ) ).withRemoteFile( guidePageModel.getRemoteImage() ).withExceptionHandler( new BiConsumer<String, Throwable>()
				{
					@Override
					public void accept( String id, Throwable exception )
					{
						image.setImageResource( R.drawable.error );

						ExceptionHelper.handleExceptionOnce( "guest-view-image-error-" + id, new RuntimeException( "Recoverable Exception", exception ) );
						Toast.makeText( getContext(), "We had a problem loading the guest image. The problem was reported to the developer.", Toast.LENGTH_LONG ).show();
					}
				} ).withProgressListener( new FileBuilder.ProgressListener()
				{
					@Override
					public void finish( FileResult result )
					{
						if ( progressBar != null )
							progressBar.setVisibility( View.GONE );
					}

					@Override
					public void progress( FileResult result, long bytesTransferred, long totalByteCount, boolean indeterminate )
					{
						if ( progressBar != null )
						{
							progressBar.setMax( ( int ) totalByteCount );
							progressBar.setProgress( ( int ) bytesTransferred );
							progressBar.setIndeterminate( indeterminate );
						}
					}

					@Override
					public void start( FileResult result )
					{
						if ( progressBar != null )
						{
							progressBar.setProgress( 0 );
							progressBar.setVisibility( View.VISIBLE );
						}
					}
				} ).withImageView( image ).forceDownload( isRefresh ).request().start();
			}
			catch ( FileBuilder.FileBuilderException e )
			{
				ExceptionHelper.handleExceptionOnce( "guide-page-" + guidePageModel.pageNo, new RuntimeException( "Failure in guide: " + guidePageModel.title + " // " + guidePageModel.image + ".", e ) );
			}
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if ( handler != null )
		{
			handler.cancelAll();
			handler = null;
		}
	}
}
