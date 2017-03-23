package com.ramencon.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.penoaks.booklet.DataPersistence;
import com.ramencon.R;

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
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		addPreferencesFromResource( R.xml.pref_general );

		// SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		// if ( prefs.getString("pref_reminder_delays", null) == null)

		ListPreference mReminderDelays = ( ListPreference ) findPreference( "pref_reminder_delays" );

		CharSequence[] entitiesValues = new String[] {"1", "5", "10", "15", "30"};
		CharSequence[] entitiesTitle = new String[] {"1 Minute", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes"};

		mReminderDelays.setEntryValues( entitiesValues );
		mReminderDelays.setEntries( entitiesTitle );

		mReminderDelays.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange( Preference preference, Object newValue )
			{
				HomeActivity.instance.service.restartAllNotifications( Long.parseLong( ( String ) newValue ) * 60 * 1000 );
				return true;
			}
		} );

		Preference mForceReset = findPreference( "pref_force_reset" );
		mForceReset.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick( Preference preference )
			{
				AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
				builder.setTitle( "App Data Reset" ).setMessage( "Are you sure you wish to factory reset the app?" ).setPositiveButton( "Yes Please!", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						HomeActivity.instance.signOut();

						HomeActivity.instance.stacker.clearFragments();

						DataPersistence.getInstance().factoryReset();

						Toast.makeText( getContext(), "App Successfully Factory Reset", Toast.LENGTH_LONG ).show();
						HomeActivity.instance.finish();
					}
				} ).setNegativeButton( "Opps, wrong button", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						dialog.dismiss();
					}
				} ).create().show();
				return true;
			}
		} );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "Settings" );

		return super.onCreateView( inflater, container, savedInstanceState );
	}
}
