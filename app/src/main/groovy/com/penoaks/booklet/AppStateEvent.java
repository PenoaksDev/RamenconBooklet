package com.penoaks.booklet;

public class AppStateEvent
{
	public AppStateEvent( AppStates state )
	{
		this.state = state;
	}

	public AppStates state;
	public boolean isHandled = false;

	public String getMessage()
	{
		if ( state == AppStates.PENDING )
			return "We are currently waiting for the application to finish downloading the application data.";

		if ( state == AppStates.NO_USER )
			return "We had an authentication problem. Are you connected to the internet?";

		if ( state == AppStates.PERSISTENCE_ERRORED )
		{
			return DataPersistence.getInstance().getFirstError();
		}

		if ( state == AppStates.PERSISTENCE_EXCEPTION )
		{
			Exception exception = DataPersistence.getInstance().getFirstException();
			return exception.getClass().getSimpleName() + ": " + exception.getMessage();

			/*if ( !UtilAndroid.haveNetworkConnection() )
				return "We had a problem connecting to the internet. Please check your connection.";
			return "We had a problem downloading the application data. Are you connected to the internet?";*/
		}

		if ( state == AppStates.SUCCESS )
			return "There is currently no problems detected and the application should be functioning as expected.";

		return "";
	}
}
