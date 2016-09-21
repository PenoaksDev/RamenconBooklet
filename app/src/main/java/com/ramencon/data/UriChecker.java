package com.ramencon.data;

import com.penoaks.sepher.ConfigurationSection;

public interface UriChecker
{
	boolean isLoaded(String uri, ConfigurationSection root);
}
