package io.amelia.booklet.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.amelia.R;
import io.amelia.android.backport.function.BiConsumer;
import io.amelia.android.data.BoundData;
import io.amelia.android.files.FileBuilder;
import io.amelia.android.files.FileFutureBitmap;
import io.amelia.android.support.DateAndTime;
import io.amelia.android.support.ExceptionHelper;
import io.amelia.booklet.data.ContentFragment;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.data.WelcomeHandler;

public class WelcomeFragment extends ContentFragment<WelcomeHandler>
{
	private static WelcomeFragment instance = null;

	public static WelcomeFragment instance()
	{
		return instance;
	}

	public WelcomeFragment() throws IOException
	{
		super( WelcomeHandler.class );
		instance = this;
	}

	public void addToCalendar()
	{
		final Intent iCal = new Intent( Intent.ACTION_EDIT );
		iCal.setType( "vnd.android.cursor.item/event" );
		iCal.putExtra( CalendarContract.Events.TITLE, handler.title );
		iCal.putExtra( CalendarContract.Events.DESCRIPTION, handler.description );
		iCal.putExtra( CalendarContract.Events.EVENT_LOCATION, handler.where );
		iCal.putExtra( CalendarContract.EXTRA_EVENT_BEGIN_TIME, handler.whenFromDate.getTime() );
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( handler.whenToDate );
		calendar.add( Calendar.DATE, 1 );
		iCal.putExtra( CalendarContract.EXTRA_EVENT_END_TIME, calendar.getTime().getTime() );
		iCal.putExtra( CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID() );
		iCal.putExtra( CalendarContract.Events.ALL_DAY, true );

		if ( iCal.resolveActivity( getActivity().getPackageManager() ) != null )
			startActivity( iCal );
		else
			Toast.makeText( getActivity(), "Sorry, it appears you don't have a calendar app installed.", Toast.LENGTH_LONG ).show();
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
	{
		inflater.inflate( R.menu.welcome_options_menu, menu );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "About the Convention" );

		View root = inflater.inflate( R.layout.fragment_welcome, container, false );

		TextView tv_date = root.findViewById( R.id.welcome_date );
		tv_date.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				addToCalendar();
			}
		} );

		TextView tv_location = root.findViewById( R.id.welcome_where );
		tv_location.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				openMaps();
			}
		} );

		setHasOptionsMenu( true );

		return root;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch ( item.getItemId() )
		{
			case R.id.options_calendar:
				addToCalendar();
				return true;
			case R.id.options_map:
				openMaps();
				return true;
		}
		return super.onOptionsItemSelected( item );
	}

	public void openMaps()
	{
		Uri mapIntentUri = Uri.parse( "geo:0,0?q=" + handler.where );
		final Intent iMap = new Intent( Intent.ACTION_VIEW, mapIntentUri );
		iMap.setPackage( "com.google.android.apps.maps" );

		if ( iMap.resolveActivity( getActivity().getPackageManager() ) != null )
			startActivity( iMap );
		else
			Toast.makeText( getActivity(), "Sorry, it appears you don't have Google Maps installed.", Toast.LENGTH_LONG ).show();
	}

	@Override
	public void sectionHandle( BoundData data, boolean isRefresh )
	{
		getActivity().setTitle( "About " + handler.title );

		View root = getView();

		final ImageView imagerViewHeader = root.findViewById( R.id.welcome_header );

		imagerViewHeader.setImageResource( R.drawable.loading_image );
		try
		{
			new FileBuilder( "welcome-header-" + handler.id, FileFutureBitmap.class ).withLocalFile( new File( ContentManager.getActiveBooklet().getDataDirectory(), "header.png" ) ).withRemoteFile( FileBuilder.REMOTE_IMAGES_URL + handler.id + "/header.png" ).withExceptionHandler( new BiConsumer<String, Throwable>()
			{
				@Override
				public void accept( String id, Throwable exception )
				{
					ExceptionHelper.handleExceptionOnce( "welcome-header-" + handler.id, new RuntimeException( "Failed to load image from server [images/" + handler.id + "/header.png]", exception ) );
					imagerViewHeader.setImageResource( R.drawable.error );
				}
			} ).withImageView( imagerViewHeader ).request().start();
		}
		catch ( Exception exception )
		{
			ExceptionHelper.handleExceptionOnce( "welcome-header-" + handler.id, new RuntimeException( "Failed to load image from server [images/" + handler.id + "/header.png]", exception ) );
			imagerViewHeader.setImageResource( R.drawable.error );
		}

		TextView welcomeTitle = root.findViewById( R.id.welcome_title );
		welcomeTitle.setText( handler.title );

		Date now = new Date();

		TextView textViewDate = root.findViewById( R.id.welcome_date );
		textViewDate.setText( DateAndTime.formatDateRange( handler.whenFromDate, handler.whenToDate ) );
		// PLog.i( "Date Range Debug " + handler.whenFrom + " -- " + handler.whenTo + " = " + DateAndTime.formatDateRange( whenFrom, whenTo ) );

		TextView textViewBlurb = root.findViewById( R.id.welcome_blurb );

		try
		{
			if ( now.after( handler.whenFromDate ) && now.before( handler.whenToDate ) )
				textViewBlurb.setText( "Howdy, we hope you're enjoying the convention. :D" );
			else if ( now.before( handler.whenFromDate ) )
				textViewBlurb.setText( "Hurray, we look forward to seeing you soon!" );
			else if ( now.after( handler.whenToDate ) )
			{
				textViewBlurb.setText( "You don't have to go hone but can't stay here! :(" );
				textViewBlurb.setTextColor( Color.RED );
			}
		}
		catch ( Exception e )
		{

		}

		TextView textViewWhere = root.findViewById( R.id.welcome_where );
		textViewWhere.setText( handler.where );

		TextView textViewDescription = root.findViewById( R.id.welcome_description );
		textViewDescription.setText( handler.description );
	}
}