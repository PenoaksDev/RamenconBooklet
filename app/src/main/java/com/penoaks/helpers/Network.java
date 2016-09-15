package com.penoaks.helpers;

import java.net.InetAddress;

public class Network
{
	private Network()
	{

	}

	public static boolean internetAvailable()
	{
		try
		{
			InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
			return !ipAddr.equals("");
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
