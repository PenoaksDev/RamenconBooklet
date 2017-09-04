package io.amelia.booklet.data;

import android.app.ProgressDialog;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;

import java.io.IOException;

public abstract class ContentFragment<R extends ContentHandler> extends Fragment implements ContentHandler.ContentListener
{
	protected R handler = null;
	private ProgressDialog mDialog;

	protected ContentFragment( Class<R> handlerClass ) throws IOException
	{
		handler = ContentManager.getActiveBooklet().getSectionHandler( handlerClass );
	}

	public R getHandler()
	{
		return handler;
	}

	void loadStart()
	{
		mDialog = new ProgressDialog( getActivity() );
		mDialog.setMessage( "Loading Data..." );
		mDialog.setCancelable( false );
		mDialog.show();
	}

	void loadStop()
	{
		if ( mDialog != null )
			mDialog.cancel();
	}

	@Override
	@CallSuper
	public void onStart()
	{
		super.onStart();

		handler.attachListener( getClass().getSimpleName(), this );
	}

	@Override
	@CallSuper
	public void onStop()
	{
		super.onStop();

		handler.detachListener( getClass().getSimpleName() );
	}

	public final void refreshData() throws IOException
	{
		handler.forceRefresh();
	}
}
