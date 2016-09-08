package com.ramencon.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ramencon.R;

import java.util.Arrays;
import java.util.List;

public class SigninActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, FirebaseAuth.AuthStateListener
{
	public static final int GOOGLE_SIGNIN = 360;

	/**
	 * Organic first-time load, once false we've probably being called from another activity.
	 */
	private static boolean organicLast = true;
	private boolean organic = true;

	private GoogleSignInOptions mGoogleOptions;
	private GoogleApiClient mGoogleApi;
	private FirebaseAuth mAuth;

	private ProgressDialog mDialog;

	private Button loginGoogle;
	private Button loginTwitter;
	private Button loginFacebook;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);

		organic = organicLast;
		organicLast = false;

		mGoogleOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("496133665089-evn0i2maaasaugmqcilud7eb7u6dgakm.apps.googleusercontent.com").requestEmail().requestProfile().build();
		mGoogleApi = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleOptions).build();
		mAuth = FirebaseAuth.getInstance();

		CallbackManager callbackManager = CallbackManager.Factory.create();

		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>()
		{
			@Override
			public void onSuccess(LoginResult loginResult)
			{
				Toast.makeText(SigninActivity.this, "Login Success", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCancel()
			{
				Toast.makeText(SigninActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(FacebookException exception)
			{
				Toast.makeText(SigninActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
			}
		});

		loginFacebook = (Button) findViewById(R.id.signLoginFacebook);
		loginFacebook.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LoginManager.getInstance().logInWithReadPermissions(SigninActivity.this, Arrays.asList("public_profile", "user_friends"));
			}
		});

		loginTwitter = (Button) findViewById(R.id.signLoginTwitter);
		loginTwitter.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});

		loginGoogle = (Button) findViewById(R.id.signLoginGoogle);
		loginGoogle.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApi);
				startActivityForResult(signInIntent, GOOGLE_SIGNIN);
			}
		});

		Button loginAnonymous = (Button) findViewById(R.id.signLoginAnonymous);
		Button logout = (Button) findViewById(R.id.signLogout);
		Button skip = (Button) findViewById(R.id.signSkip);

		loginAnonymous.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mAuth.signInAnonymously();
			}
		});

		logout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mAuth.signOut();
				recreate();
			}
		});

		skip.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(SigninActivity.this, HomeActivity.class));
			}
		});

		if (mAuth.getCurrentUser() == null)
			signedOutMode();
		else if (mAuth.getCurrentUser() != null && organic)
			startActivity(new Intent(this, HomeActivity.class));
		else
			signedInMode();
	}

	public void signedOutMode()
	{
		((TextView) findViewById(R.id.signTextNewHere)).setText("New here? Please sign-in with one of the following:");
		((TextView) findViewById(R.id.signTextOrBrowse)).setText("or browse anonymously; we won't judge.");

		findViewById(R.id.signLoginAnonymous).setVisibility(View.VISIBLE);
		findViewById(R.id.signLogout).setVisibility(View.GONE);
		findViewById(R.id.signSkip).setVisibility(View.GONE);
		findViewById(R.id.signDescription).setVisibility(View.VISIBLE);

		loginGoogle.setText("  Google");
		loginGoogle.setEnabled(true);
		loginFacebook.setText("  Facebook");
		loginFacebook.setEnabled(true);
		loginTwitter.setText("  Twitter");
		loginTwitter.setEnabled(true);
	}

	public void signedInMode()
	{
		((TextView) findViewById(R.id.signTextNewHere)).setText("Hello! Would you like to link additional social accounts?");
		((TextView) findViewById(R.id.signTextOrBrowse)).setText("You can also sign-out or go back.");

		findViewById(R.id.signLoginAnonymous).setVisibility(View.GONE);
		findViewById(R.id.signLogout).setVisibility(View.VISIBLE);
		findViewById(R.id.signSkip).setVisibility(View.VISIBLE);
		findViewById(R.id.signDescription).setVisibility(View.GONE);

		FirebaseUser user = mAuth.getCurrentUser();
		List<String> providers = user.getProviders();

		loginGoogle.setText( providers.contains("google.com") ? "  Google (Already Linked)" : "  Google" );
		loginGoogle.setEnabled( !providers.contains("google.com") );
		// loginFacebook.setText( providers.contains("facebook") ? "  Facebook (Already Linked)" : "  Facebook" );
		// loginFacebook.setEnabled( !providers.contains("facebook") );
		// loginTwitter.setText( providers.contains("twitter") ? "  Twitter (Already Linked)" : "  Twitter" );
		// loginTwitter.setEnabled( !providers.contains("twitter") );

		for (String provider : user.getProviders())
			Log.i("APP", "Provider: " + provider);
	}

	public void showProgressDialog()
	{
		mDialog = new ProgressDialog(this);
		mDialog.setCancelable(false);
		mDialog.setIndeterminate(true);
		mDialog.setTitle("Authorizing");
		mDialog.setMessage("Please Wait...");
	}

	public void hideProgressDialog()
	{
		if (mDialog != null)
			mDialog.cancel();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == GOOGLE_SIGNIN)
		{
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess())
			{
				GoogleSignInAccount acct = result.getSignInAccount();

				Log.d("APP", "firebaseAuthWithGoogle:" + acct.getId());
				showProgressDialog();

				AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

				OnCompleteListener onCompleteListener = new OnCompleteListener<AuthResult>()
				{
					@Override
					public void onComplete(@NonNull Task<AuthResult> task)
					{
						Log.d("APP", "signInWithCredential:onComplete:" + task.isSuccessful());

						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						if (task.isSuccessful())
						{
							Log.w("APP", "User: " + task.getResult().getUser());
							Toast.makeText(SigninActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
						}
						else
						{
							Log.w("APP", "signInWithCredential", task.getException());
							Toast.makeText(SigninActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
						}

						hideProgressDialog();
					}
				};

				if (mAuth.getCurrentUser() == null)
					mAuth.signInWithCredential(credential).addOnCompleteListener(this, onCompleteListener);
				else
					mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, onCompleteListener);
			}
			else
			{
				Log.i("APP", "Google Result: " + result.getStatus());
				Toast.makeText(this, "Sign-in failed. " + result.getStatus(), Toast.LENGTH_SHORT).show();
			}
		}
		else
			Log.w("APP", String.format("Sign-in got an unknown requestCode [%s] and resultCode [%s].", requestCode, resultCode));
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{

	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
	{
		hideProgressDialog();

		Log.i("APP", "User: " + (firebaseAuth.getCurrentUser() == null ? "NULL" : firebaseAuth.getCurrentUser().getUid()));

		if (firebaseAuth.getCurrentUser() == null)
			signedOutMode();
		else if (firebaseAuth.getCurrentUser() != null)
		{
			if (organic)
				startActivity(new Intent(this, HomeActivity.class));
			else
				signedInMode();
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		mAuth = FirebaseAuth.getInstance();
		mAuth.addAuthStateListener(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		mAuth.removeAuthStateListener(this);
	}
}
