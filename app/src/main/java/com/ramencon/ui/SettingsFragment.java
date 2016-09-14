package com.ramencon.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramencon.R;
import com.ramencon.AppService;

public class SettingsFragment extends PreferenceFragment
{
	public static SettingsFragment instance()
	{
		return new SettingsFragment();
	}

	public SettingsFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_general);

		// SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		// if ( prefs.getString("pref_reminder_delays", null) == null)

		ListPreference mReminderDelays = (ListPreference) findPreference("pref_reminder_delays");

		CharSequence[] entitiesValues = new String[]{"1", "5", "10", "15", "30"};
		CharSequence[] entitiesTitle = new String[]{"1 Minute", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes"};

		mReminderDelays.setEntryValues(entitiesValues);
		mReminderDelays.setEntries(entitiesTitle);

		mReminderDelays.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				AppService.instance().cancelAllNotifications();



				return true;
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().setTitle("Settings");

		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
