package io.amelia.booklet.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.amelia.R;
import io.amelia.android.data.BoundData;
import io.amelia.android.data.ImageCache;
import io.amelia.android.log.PLog;
import io.amelia.android.support.ACRAHelper;
import io.amelia.android.support.DateAndTime;
import io.amelia.booklet.data.ContentFragment;
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

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "About the Convention" );

		View root = inflater.inflate( R.layout.fragment_welcome, container, false );

		/*
		try
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			final Intent iCal = new Intent(Intent.ACTION_EDIT);
			iCal.setType("vnd.android.cursor.item/event");
			iCal.putExtra(CalendarContract.Events.TITLE, "Ramencon");
			iCal.putExtra(CalendarContract.Events.DESCRIPTION, "Ramencon is an anime convention in Northwest Indiana, built by anime fans, for anime fans. Much of what you experience at the con has been suggested by all of you! We want everyone to have the best time possible and be comfortable enough to do everything you want in one weekend.");
			iCal.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, format.parse("2016-09-23"));
			iCal.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, format.parse("2016-09-25"));
			iCal.putExtra(CalendarContract.Events.ALL_DAY, true);

			TextView tv_date = (TextView) root.findViewById(R.id.welcome_date);
			tv_date.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (iCal.resolveActivity(getActivity().getPackageManager()) != null)
						startActivity(iCal);
					else
						Toast.makeText(getActivity(), "Sorry, it appears you don't have a calendar app installed.", Toast.LENGTH_LONG).show();
				}
			});
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		*/

		Uri mapIntentUri = Uri.parse( "gwo:0,0?q=800 E 81st Ave Merrillville, IN 46410" );
		final Intent iMap = new Intent( Intent.ACTION_VIEW, mapIntentUri );
		iMap.setPackage( "com.google.android.apps.maps" );

		TextView tv_location = root.findViewById( R.id.welcome_where );
		tv_location.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				if ( iMap.resolveActivity( getActivity().getPackageManager() ) != null )
					startActivity( iMap );
				else
					Toast.makeText( getActivity(), "Sorry, it appears you don't have Google Maps installed.", Toast.LENGTH_LONG ).show();
			}
		} );

		/*
		WebView web = (WebView) root.findViewById(R.id.htmlWelcome);

		if (CacheManager.instance().fileWelcome().exists())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(CacheManager.instance().fileWelcome());

				StringBuffer fileContent = new StringBuffer("");

				byte[] buffer = new byte[1024];

				int n;
				while ((n = fis.read(buffer)) != -1)
					fileContent.append(new String(buffer, 0, n));

				web.loadData(fileContent.toString(), "text/html", "utf-8");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (fis != null)
					try
					{
						fis.close();
					}
					catch (IOException ignore)
					{

					}
			}
		}
		else
			web.loadData("The Welcome Data File is missing!", "text/html", "utf-8");
			*/

		return root;
	}

	@Override
	public void sectionHandle( BoundData data, boolean isRefresh )
	{
		getActivity().setTitle( "About " + handler.title );

		View root = getView();

		final ImageView welcomeHeader = root.findViewById( R.id.welcome_header );

		if ( handler.image == null )
			welcomeHeader.setImageResource( R.drawable.noimagefound );
		else
		{
			welcomeHeader.setImageResource( R.drawable.loading_image );
			ImageCache.cacheRemoteImage( getContext(), "welcome-header-" + handler.image, ImageCache.REMOTE_IMAGES_URL + "booklet-headers/" + handler.image, false, new ImageCache.ImageResolveTask.ImageFoundListener()
			{
				@Override
				public void error( Exception exception )
				{
					ACRAHelper.handleExceptionOnce( "welcome-header" + handler.id + "-" + handler.image, new RuntimeException( "Failed to load image from server [images/booklet-headers/" + handler.image + "]", exception ) );
					welcomeHeader.setImageResource( R.drawable.error );
				}

				@Override
				public void update( Bitmap bitmap )
				{
					welcomeHeader.setImageBitmap( bitmap );
				}
			}, null );
		}

		TextView welcomeTitle = root.findViewById( R.id.welcome_title );
		welcomeTitle.setText( handler.title );

		try
		{
			SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
			Date whenFrom = format.parse( handler.whenFrom );
			Date whenTo = format.parse( handler.whenTo );
			Date now = new Date();

			TextView welcomeDate = root.findViewById( R.id.welcome_date );
			welcomeDate.setText( DateAndTime.formatDateRange( whenFrom, whenTo ) );
			PLog.i( "Date Range Debug " + handler.whenFrom + " -- " + handler.whenTo + " = " + DateAndTime.formatDateRange( whenFrom, whenTo ) );

			TextView welcomeBlurb = root.findViewById( R.id.welcome_blurb );
			if ( now.after( whenFrom ) && now.before( whenTo ) )
				welcomeBlurb.setText( "Howdy, we hope your enjoying the convention. :D" );
			else if ( now.before( whenFrom ) )
				welcomeBlurb.setText( "Hurray, we look forward to seeing you soon!" );
			else if ( now.after( whenTo ) )
			{
				welcomeBlurb.setTextColor( Color.RED );
				welcomeBlurb.setText( "Bummer, this convention was in the past. :( We hope it was an awesome year!" );
			}
			else
				welcomeBlurb.setText( "" );
		}
		catch ( ParseException e )
		{
			e.printStackTrace();
		}

		TextView welcomeWhere = root.findViewById( R.id.welcome_where );
		welcomeWhere.setText( handler.where );

		TextView welcomeDescrtion = root.findViewById( R.id.welcome_description );
		welcomeDescrtion.setText( handler.description );
	}
}