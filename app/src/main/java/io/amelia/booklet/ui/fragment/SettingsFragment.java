package io.amelia.booklet.ui.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.acra.ACRA;

import java.util.Map;

import io.amelia.R;
import io.amelia.android.support.Objs;
import io.amelia.booklet.data.ContentManager;
import io.amelia.booklet.ui.activity.BootActivity;

public class SettingsFragment extends PreferenceFragment
{
	private static final int REQUEST_CHOOSE_ACCOUNT = 0x00;

	public static SettingsFragment instance()
	{
		return new SettingsFragment();
	}

	public SettingsFragment()
	{
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		if ( requestCode == REQUEST_CHOOSE_ACCOUNT )
		{
			if ( resultCode == 0 || data == null )
				Toast.makeText( getContext(), "Account Selection Cancelled. No Changes Made.", Toast.LENGTH_SHORT ).show();
			else
			{
				String acct = data.getStringExtra( AccountManager.KEY_ACCOUNT_NAME );
				ContentManager.setPrefCalendarAccount( acct );

				findPreference( "pref_calendar_account" ).setSummary( "Selects which account to use when adding events to the Google Calendar. Current Account: " + ( acct == null ? "Unset" : acct ) );
			}
			return;
		}

		super.onActivityResult( requestCode, resultCode, data );
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		addPreferencesFromResource( R.xml.pref_general );

		ListPreference mReminderDelays = ( ListPreference ) findPreference( "pref_reminder_delays" );

		CharSequence[] reminderDelaysEntitiesValues = new String[] {"0", "1", "2", "5", "10", "15", "20", "30", "45"};
		CharSequence[] reminderDelaysEntitiesTitle = new String[] {"Disable", "1 Minute", "2 Minutes", "5 Minutes", "10 Minutes", "15 Minutes", "20 Minutes", "30 Minutes", "45 Minutes"};

		mReminderDelays.setEntryValues( reminderDelaysEntitiesValues );
		mReminderDelays.setEntries( reminderDelaysEntitiesTitle );

		ListPreference mImageCache = ( ListPreference ) findPreference( "pref_image_cache" );

		CharSequence[] imageCacheEntitiesValues = new String[] {"0", "15", "30", "60", "120", "240", "360", "720", "1440"};
		CharSequence[] imageCacheEntitiesTitle = new String[] {"No Cache", "15 Minutes", "30 Minutes", "1 Hour", "2 Hours", "4 Hours", "6 Hours", "12 Hours", "1 Day"};

		mImageCache.setEntryValues( imageCacheEntitiesValues );
		mImageCache.setEntries( imageCacheEntitiesTitle );

		Preference mDeleteImageCache = findPreference( "pref_delete_images" );
		mDeleteImageCache.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick( Preference preference )
			{
				ContentManager.clearImageCache();
				Toast.makeText( getContext(), "Image Cache Deleted", Toast.LENGTH_LONG ).show();
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
						getPreferenceManager().getSharedPreferences().edit().clear().apply();
						ContentManager.factoryAppReset();
						/* HomeActivity.instance.signOut(); */

						Toast.makeText( getContext(), "App Successfully Factory Reset", Toast.LENGTH_LONG ).show();

						( ( BootActivity ) getActivity() ).stacker.popBackstack();
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

		Preference mSendBugReport = findPreference( "pref_send_bug_report" );
		mSendBugReport.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick( Preference preference )
			{
				AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
				builder.setTitle( "Send Bug Report to Developer" );
				builder.setMessage( "Don't worry, no personal information will be transmitted besides what you enter here. Please enter your problem and be as descriptive as possible. Inclusion of steps on how to recreate the problem would be appreciated." );

				final EditText input = new EditText( getContext() );
				input.setLines( 6 );
				input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE );
				builder.setView( input );

				builder.setPositiveButton( "Send away!", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						if ( Objs.isEmpty( input.getText().toString() ) )
						{
							new AlertDialog.Builder( getContext() ).setTitle( "Error" ).setMessage( "Please enter a description and try again." ).setPositiveButton( "Okay, bob!", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick( DialogInterface dialogInterface, int i )
								{
									dialogInterface.dismiss();
									onPreferenceClick( preference );
								}
							} ).show();
							return;
						}

						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( getContext() );
						CustomEvent event = new CustomEvent( "BUG_REPORT" );
						for ( Map.Entry<String, ?> entry : pref.getAll().entrySet() )
						{
							// ACRA.getErrorReporter().putCustomData( entry.getKey(), Objs.castToString( entry.getValue() ) );
							event.putCustomAttribute( entry.getKey(), Objs.castToString( entry.getValue() ) );
						}
						Answers.getInstance().logCustom( event );

						ACRA.getErrorReporter().handleException( new RuntimeException( "Bug Report: " + input.getText().toString() ) );

						Toast.makeText( getContext(), "Thank you for the bug report! The developer will look into the problem as soon as possible.", Toast.LENGTH_LONG ).show();
					}
				} );
				builder.setNegativeButton( "Close", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						dialog.cancel();
					}
				} );
				builder.show();

				return true;
			}
		} );

		String acct = ContentManager.getPrefCalendarAccount();

		Preference mCalendarAccount = findPreference( "pref_calendar_account" );
		mCalendarAccount.setSummary( "Selects which account to use when adding events to the Google Calendar. Current Account: " + ( acct == null ? "Not Set!" : acct ) );
		mCalendarAccount.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick( Preference preference )
			{
				Dexter.withActivity( ContentManager.getActivity() ).withPermission( Manifest.permission.GET_ACCOUNTS ).withListener( new PermissionListener()
				{
					@Override
					public void onPermissionDenied( PermissionDeniedResponse response )
					{
						Toast.makeText( getContext(), "You must grant the Ramencon Booklet App the GET_ACCOUNTS permission.", Toast.LENGTH_LONG ).show();
					}

					@Override
					public void onPermissionGranted( PermissionGrantedResponse response )
					{
						Intent intent = AccountManager.newChooseAccountIntent( null, null, new String[] {"com.google"}, true, null, null, null, null );
						startActivityForResult( intent, REQUEST_CHOOSE_ACCOUNT );
					}

					@Override
					public void onPermissionRationaleShouldBeShown( PermissionRequest permission, PermissionToken token )
					{

					}
				} ).check();
				return true;
			}
		} );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		getActivity().setTitle( "App Settings" );

		return super.onCreateView( inflater, container, savedInstanceState );
	}
}
