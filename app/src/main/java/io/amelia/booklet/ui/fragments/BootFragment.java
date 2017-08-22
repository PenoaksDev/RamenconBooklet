package io.amelia.booklet.ui.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.ramencon.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.amelia.android.configuration.ConfigurationSection;
import io.amelia.android.configuration.types.json.JsonConfiguration;
import io.amelia.booklet.App;
import io.amelia.booklet.data.BookletAdapter;
import io.amelia.booklet.models.ModelBooklet;
import io.amelia.booklet.ui.BootActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BootFragment extends Fragment
{
	public static BootFragment instance()
	{
		return new BootFragment();
	}
	private File dataFile = new File( App.cacheDir, "AppData.json" );
	private JsonConfiguration data = new JsonConfiguration();
	private ProgressDialog loading;
	private SwipeRefreshLayout refreshLayout;
	private ExpandableListView bookletListview;

	public BootFragment()
	{

	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "Ramencon Booklet" );

		View root = inflater.inflate( R.layout.fragment_boot, container, false );

		try
		{
			data = JsonConfiguration.loadConfiguration( dataFile );
		}
		catch ( Throwable t )
		{
			// Ignore
		}

		refreshLayout = ( SwipeRefreshLayout ) root.findViewById( R.id.booklet_refresher );
		refreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				refresh();
			}
		} );

		bookletListview = ( ExpandableListView ) root.findViewById( R.id.bootlet_listview );

		refresh();

		return root;
	}

	public void refresh()
	{
		loading = ProgressDialog.show( getActivity(), "Loading", "Retrieving App Data", true );
		final Finisher finisher = new Finisher();
		finisher.executeOnExecutor( App.getExecutorThreadPool() );

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url( App.REMOTE_CATALOG_URL ).build();
		client.newCall( request ).enqueue( new Callback()
		{
			@Override
			public void onFailure( Call call, IOException e )
			{
				e.printStackTrace();
				finisher.waiting = false;
				// startActivityForResult( new Intent( BootActivity.this, ErroredActivity.class ).putExtra( ErroredActivity.ERROR_MESSAGE, e.getClass().getSimpleName() + ": " + e.getMessage() ), -1 );
			}

			@Override
			public void onResponse( Call call, Response response ) throws IOException
			{
				try
				{
					data = JsonConfiguration.loadConfiguration( response.body().string() );
					// data.merge( JsonConfiguration.loadConfiguration( response.body().string() ) );
					data.save( dataFile );
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
				finisher.waiting = false;
			}
		} );
	}

	public void onListItemClick( ModelBooklet booklet )
	{
		( ( BootActivity ) getActivity() ).stacker.setFragment( BookletFragment.instance( booklet.bookletId ), true );
	}

	class Finisher extends AsyncTask<Void, Void, Void>
	{
		boolean waiting = true;

		@Override
		protected Void doInBackground( Void... params )
		{
			while ( waiting )
				try
				{
					Thread.sleep( 250 );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
			return null;
		}

		@Override
		protected void onPostExecute( Void aVoid )
		{
			super.onPostExecute( aVoid );

			loading.cancel();
			refreshLayout.setRefreshing( false );

			List<ModelBooklet> models = new ArrayList<>();
			for ( ConfigurationSection section : data.getConfigurationSections() )
				models.add( section.asObject( ModelBooklet.class ) );

			BookletAdapter bookletAdapter = new BookletAdapter( BootFragment.this, models );
			bookletListview.setAdapter( bookletAdapter );
		}
	}
}
