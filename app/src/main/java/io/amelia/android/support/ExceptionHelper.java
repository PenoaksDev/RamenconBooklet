package io.amelia.android.support;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

public class ExceptionHelper
{
	private static List<String> reportedExceptions = new ArrayList<>();

	public static void handleExceptionOnce( String id, Exception e )
	{
		e.printStackTrace();

		if ( !reportedExceptions.contains( id ) )
			Crashlytics.getInstance().core.logException( e );

		reportedExceptions.add( id );
	}

	private ExceptionHelper()
	{

	}
}
