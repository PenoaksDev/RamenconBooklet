package io.amelia.android.support;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.List;

public class ACRAHelper
{
	private static List<String> reportedExceptions = new ArrayList<>();

	public static void handleExceptionOnce( String id, Exception e )
	{
		e.printStackTrace();

		if ( !reportedExceptions.contains( id ) )
			ACRA.getErrorReporter().handleException( e );

		reportedExceptions.add( id );
	}

	private ACRAHelper()
	{

	}
}
