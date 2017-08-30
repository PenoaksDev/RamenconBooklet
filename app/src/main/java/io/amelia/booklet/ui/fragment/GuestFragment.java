package io.amelia.booklet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.io.IOException;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.fragments.PersistentFragment;
import io.amelia.booklet.data.ContentFragment;
import io.amelia.booklet.data.GuestsAdapter;
import io.amelia.booklet.data.GuestsHandler;

public class GuestFragment extends ContentFragment<GuestsHandler> implements PersistentFragment
{
	private static GuestFragment instance = null;

	public static GuestFragment instance()
	{
		return instance;
	}

	private Bundle savedState = null;

	public GuestFragment() throws IOException
	{
		super( GuestsHandler.class );
		instance = this;
	}

	@Override
	public void loadState( Bundle bundle )
	{
		this.savedState = bundle;
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getActivity().setTitle( "Guests and Vendors" );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		return inflater.inflate( R.layout.fragment_guests, container, false );
	}

	@Override
	public void refreshState()
	{
		savedState = new Bundle();
		saveState( savedState );
	}

	@Override
	public void saveState( Bundle bundle )
	{
		if ( getView() == null )
			return;

		ExpandableListView lv = getView().findViewById( R.id.guests_listview );

		if ( lv != null )
		{
			bundle.putParcelable( "listView", lv.onSaveInstanceState() );
			bundle.putInt( "listViewPositionVisible", lv.getFirstVisiblePosition() );
			bundle.putInt( "listViewPositionOffset", lv.getChildAt( 0 ) == null ? 0 : lv.getChildAt( 0 ).getTop() - lv.getPaddingTop() );
		}
	}

	@Override
	public void sectionHandle( BoundData data, boolean isRefresh )
	{
		final View root = getView();

		ExpandableListView lv = root.findViewById( R.id.guests_listview );
		lv.setAdapter( new GuestsAdapter( getContext(), handler.guests ) );

		if ( savedState != null && !savedState.isEmpty() )
		{
			lv.onRestoreInstanceState( savedState.getParcelable( "listView" ) );

			if ( savedState.getInt( "listViewPositionVisible", -1 ) > 0 )
				lv.setSelectionFromTop( savedState.getInt( "listViewPositionVisible", 0 ), savedState.getInt( "listViewPositionOffset", 0 ) );

			savedState = null;
		}
	}
}
