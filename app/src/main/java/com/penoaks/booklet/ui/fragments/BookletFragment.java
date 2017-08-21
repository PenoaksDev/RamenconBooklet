package com.penoaks.booklet.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.penoaks.android.fragments.PersistentFragment;
import com.penoaks.booklet.models.ModelBooklet;
import com.penoaks.booklet.ui.BootActivity;
import com.ramencon.R;

public class BookletFragment extends Fragment implements PersistentFragment
{
	private ModelBooklet modelBooklet;

	public BookletFragment()
	{
	}

	public static BookletFragment instance( String bookletId )
	{
		BookletFragment fragment = new BookletFragment();
		Bundle args = new Bundle();
		args.putString( "bookletId", bookletId );
		fragment.setArguments( args );
		return fragment;
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		if ( getArguments() != null && modelBooklet == null )
			modelBooklet = ModelBooklet.getModel( getArguments().getString( "bookletId" ) );

		if ( modelBooklet == null )
			throw new IllegalStateException( "bookletId was not provided to the BookletFragment" );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.fragment_booklet, container, false );

		getActivity().setTitle( modelBooklet.title );

		if ( view instanceof RecyclerView )
		{
			Context context = view.getContext();
			RecyclerView recyclerView = ( RecyclerView ) view;
			recyclerView.setLayoutManager( new LinearLayoutManager( context ) );
			recyclerView.setAdapter( new BookletAdapter( this, modelBooklet ) );
		}
		return view;
	}

	@Override
	public void saveState( Bundle bundle )
	{
		bundle.putString( "bookletId", modelBooklet.bookletId );
	}

	@Override
	public void loadState( Bundle bundle )
	{
		modelBooklet = ModelBooklet.getModel( bundle.getString( "bookletId" ) );
	}

	@Override
	public void refreshState()
	{

	}

	public void onListItemClick( BookletAdapter.BookletMenu mMenu )
	{
		Snackbar.make( getView(), "Showing Menu Item " + mMenu.title, Snackbar.LENGTH_LONG ).show();

		( ( BootActivity ) getActivity() ).stacker.setFragment( ScheduleFragment.class, true );
	}
}
