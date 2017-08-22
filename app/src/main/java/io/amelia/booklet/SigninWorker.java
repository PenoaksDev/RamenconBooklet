package io.amelia.booklet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import io.amelia.android.log.PLog;

public class SigninWorker implements FirebaseAuth.AuthStateListener, OnCompleteListener<AuthResult>
{
	public static final int GOOGLE_SIGNIN = 360;

	private static SigninWorker instance = null;

	public static SigninWorker getInstance( SigninParent parent )
	{
		if ( instance == null )
			instance = new SigninWorker( parent );
		else
		{
			instance.parent = parent;
			FirebaseAuth.getInstance().addAuthStateListener( instance );
		}
		return instance;
	}

	private SigninIntent lastIntent = SigninIntent.FIRST_BOOT;
	private GoogleSignInOptions mGoogleOptions;
	private GoogleApiClient mGoogleApi;
	private ProgressDialog mDialog;
	private FirebaseAuth mAuth;
	private SigninParent parent;
	private AuthCredential pendingGoogleCredentual;

	private SigninWorker( SigninParent parent )
	{
		this.parent = parent;

		mGoogleOptions = new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN ).requestIdToken( "496133665089-evn0i2maaasaugmqcilud7eb7u6dgakm.apps.googleusercontent.com" ).requestEmail().requestProfile().build();
		mGoogleApi = new GoogleApiClient.Builder( parent.getActivity() ).addApi( Auth.GOOGLE_SIGN_IN_API, mGoogleOptions ).build();
		mAuth = FirebaseAuth.getInstance();

		mAuth.addAuthStateListener( this );
		onAuthStateChanged( mAuth );
	}

	private void handleGoogleCredential()
	{
		if ( mAuth.getCurrentUser() == null )
		{
			PLog.i( "signInWithCredential" );
			mAuth.signInWithCredential( pendingGoogleCredentual ).addOnCompleteListener( parent.getActivity(), this );
		}
		else
		{
			PLog.i( "linkWithCredential" );
			mAuth.getCurrentUser().linkWithCredential( pendingGoogleCredentual ).addOnCompleteListener( parent.getActivity(), this );
		}
	}

	public void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		if ( requestCode == GOOGLE_SIGNIN )
		{
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent( data );
			if ( result.isSuccess() )
			{
				GoogleSignInAccount acct = result.getSignInAccount();

				showProgressDialog();

				pendingGoogleCredentual = GoogleAuthProvider.getCredential( acct.getIdToken(), null );

				handleGoogleCredential();
			}
			else
			{
				Log.i( "APP", "Google Result: " + result.getStatus() );
				Toast.makeText( parent.getActivity(), "Sorry, google sign-in failed. :( " + result.getStatus(), Toast.LENGTH_SHORT ).show();
			}
		}
		else
			Log.w( "APP", String.format( "Sign-in got an unknown requestCode [%s] and resultCode [%s].", requestCode, resultCode ) );
	}

	@Override
	public void onComplete( @NonNull Task<AuthResult> task )
	{
		// On Google Signin to Firebase

		Log.d( "APP", "signInWithCredential:onComplete:" + task.isSuccessful() );

		// If sign in fails, display a message to the user. If sign in succeeds
		// the auth state listener will be notified and logic to handle the
		// signed in user can be handled in the listener.
		if ( task.isSuccessful() )
		{
			pendingGoogleCredentual = null;

			FirebaseUser user = task.getResult().getUser();
			Log.w( "APP", "User: " + user );

			String welcome = user.getDisplayName() == null ? "" : "Welcome! " + user.getDisplayName();
			Toast.makeText( parent.getActivity(), "Authentication success. " + welcome, Toast.LENGTH_SHORT ).show();
		}
		else
		{
			Exception e = task.getException();
			String msg = null;

			// FirebaseAuthInvalidUserException: The user's credential is no longer valid. The user must sign in again.
			if ( e instanceof FirebaseAuthInvalidUserException )
			{
				msg = e.getMessage() + " Try to clear the app data.";
			}
			// FirebaseAuthUserCollisionException: This credential is already associated with a different user account.
			else if ( e instanceof FirebaseAuthUserCollisionException )
				mAuth.signOut();
			else
			{
				pendingGoogleCredentual = null;

				Log.w( "APP", "signInWithCredential", e );
				msg = "Authentication failed. " + e.getMessage();
			}

			if ( msg != null )
			{
				PLog.i( msg );
				Toast.makeText( parent.getActivity(), msg, Toast.LENGTH_SHORT ).show();
			}
		}

		hideProgressDialog();
	}

	@Override
	public void onAuthStateChanged( FirebaseAuth firebaseAuth )
	{
		if ( firebaseAuth.getCurrentUser() == null )
		{
			PLog.i( "User is null" );

			if ( lastIntent == SigninIntent.GOOGLE_SIGNIN )
				signinWithGoogle();
			else
				signinAnonymously();
		}
		else if ( firebaseAuth.getCurrentUser() != null && lastIntent != SigninIntent.SIGNED_IN )
		{
			PLog.i( "Got login " + firebaseAuth.getCurrentUser().getUid() );

			// Persistence.registerUri( "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() );

			lastIntent = SigninIntent.SIGNED_IN;
		}

		parent.authStateChanged( firebaseAuth.getCurrentUser() );
	}

	public void showProgressDialog()
	{
		mDialog = new ProgressDialog( parent.getActivity() );
		mDialog.setCancelable( false );
		mDialog.setIndeterminate( true );
		mDialog.setTitle( "Authorizing" );
		mDialog.setMessage( "Please Wait..." );
	}

	public void hideProgressDialog()
	{
		if ( mDialog != null )
			mDialog.cancel();
	}

	public void signinAnonymously()
	{
		PLog.i( "signInAnonymously()" );

		lastIntent = SigninIntent.ANONYMOUSLY_SIGNIN;

		mAuth.signInAnonymously();

		pendingGoogleCredentual = null;
	}

	public void signinWithGoogle()
	{
		PLog.i( "signinWithGoogle()" );

		lastIntent = SigninIntent.GOOGLE_SIGNIN;

		if ( pendingGoogleCredentual == null )
		{
			Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleApi );
			parent.getActivity().startActivityForResult( signInIntent, GOOGLE_SIGNIN );
		}
		else
			handleGoogleCredential();
	}

	public void signOut()
	{
		lastIntent = SigninIntent.SIGNED_OUT;

		mAuth.signOut();

		pendingGoogleCredentual = null;
	}

	public void stop()
	{
		FirebaseAuth.getInstance().removeAuthStateListener( this );
	}

	enum SigninIntent
	{
		FIRST_BOOT, ANONYMOUSLY_SIGNIN, SIGNED_IN, SIGNED_OUT, GOOGLE_SIGNIN
	}

	public interface SigninParent
	{
		void authStateChanged( FirebaseUser currentUser );

		Activity getActivity();
	}
}
