package com.abysmel.exwebrtc.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.abysmel.exwebrtc.R;
import com.abysmel.exwebrtc.Utils.Logger;
import com.abysmel.exwebrtc.Utils.SharedPreferencesHelper;
import com.abysmel.exwebrtc.Utils.Util;
import com.abysmel.exwebrtc.constants.Constants;
import com.abysmel.exwebrtc.dao.Contact;
import com.abysmel.exwebrtc.dao.DataMine;
import com.abysmel.exwebrtc.service.SinchService;
import com.sinch.android.rtc.SinchError;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A login screen that offers login via email/password.
 *
 * Created by Melvin Lobo on 6/4/2016
 */
public class LoginActivity extends BaseActivity implements SinchService.StartFailedListener {

	//////////////////////////////////////// CLASS MEMBERS /////////////////////////////////////////
	/**
	 * The User Name
	 */
	@Bind( R.id.username )
	EditText mUserNameEditText;

	/**
	 * THe Progress View
	 */
	@Bind( R.id.login_progress )
	View mProgressView;

	/**
	 * THe block with the login information
	 */
	@Bind( R.id.login_form )
	View mLoginFormView;

	/**
	 * The sign in button
	 */
	@Bind( R.id.log_in_button)
	Button mEmailSignInButton;

	/**
	 * The entered user name
	 */
	String msUserName = null;

	//////////////////////////////////////// CLASS METHODS /////////////////////////////////////////

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_login );
		ButterKnife.bind( this );

		// Set up the sign in button
		mEmailSignInButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View view ) {
				attemptLogin();
			}
		} );

	}

	@Override
	protected void onServiceConnected() {
		getSinchServiceInterface().setStartListener(this);
	}

	@Override
	public void onStartFailed(SinchError error) {
		Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
		showProgress( false );
	}

	@Override
	public void onStarted() {
	}

	@Override
	protected void onPause() {
		showProgress( false );
		super.onPause();
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin() {
		/*
		 Reset errors.
		  */
		mUserNameEditText.setError( null );

		// Store values at the time of the login attempt.
		msUserName = mUserNameEditText.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for empty field
		if ( TextUtils.isEmpty( msUserName ) ) {
			mUserNameEditText.setError( getString( R.string.error_field_required ) );
			focusView = mUserNameEditText;
			cancel = true;
		}

		if ( cancel ) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else if( !Util.isNetworkConnectionAvailable()) {
			new SweetAlertDialog( this, SweetAlertDialog.ERROR_TYPE )
					.setTitleText( "Network Connection" )
					.setContentText( "No Network available. Please check if you are connected to the internet." )
					.setConfirmText( "OK" )
					.show();
		}
		else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			DataMine.getInstance().setUserName( msUserName );

			if (!getSinchServiceInterface().isStarted()) {
				getSinchServiceInterface().startClient(msUserName);
				showProgress( true );
			}

			/*
			Load Contacts
			 */
			new LoadContacts().execute(  );
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress( final boolean show ) {
		int shortAnimTime = getResources().getInteger( android.R.integer.config_shortAnimTime );

		mLoginFormView.setVisibility( show ? View.GONE : View.VISIBLE );
		mLoginFormView.animate().setDuration( shortAnimTime ).alpha(
				show ? 0 : 1 ).setListener( new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd( Animator animation ) {
				mLoginFormView.setVisibility( show ? View.GONE : View.VISIBLE );
			}
		} );

		mProgressView.setVisibility( show ? View.VISIBLE : View.GONE );
		mProgressView.animate().setDuration( shortAnimTime ).alpha(
				show ? 1 : 0 ).setListener( new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd( Animator animation ) {
				mProgressView.setVisibility( show ? View.VISIBLE : View.GONE );
			}
		} );
	}

	/**
	 * Launch the landing activity
	 *
	 * @author Melvin Lobo
	 */
	private void launchLandingActivity() {
		Intent intent = new Intent(this, LandingActivity.class);
		intent.putExtra( LandingActivity.USER, msUserName );
		startActivity( intent );
		finish();
	}

	/**
	 * Dummy class to simulate network login authorization
	 *
	 * @author Melvin Lobo
	 */
	public class LoadContacts extends AsyncTask<Void, Void, Void> {

		/**
		 * Override this method to perform a computation on a background thread. The
		 * specified parameters are the parameters passed to {@link #execute}
		 * by the caller of this task.
		 * <p>
		 * This method can call {@link #publishProgress} to publish updates
		 * on the UI thread.
		 *
		 * @param params The parameters of the task.
		 * @return A result, defined by the subclass of this task.
		 * @see #onPreExecute()
		 * @see #onPostExecute
		 * @see #publishProgress
		 */
		@Override
		protected Void doInBackground( Void... params ) {
			Set<String> mContactSet = SharedPreferencesHelper.getStringSet( SharedPreferencesHelper.KEY_CONTACTS, new HashSet<String>() );
			if(!mContactSet.isEmpty()) {
				for(String name : mContactSet) {
					Logger.d( "Store contact in preference. Adding to contact list - " + name );
					DataMine.getInstance().addContact( new Contact(name, Constants.STATUS_OFFLINE) );
				}
			}

			//Simulate netwoek delay
			try {
				Thread.sleep(2000);
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute( Void aVoid ) {
			super.onPostExecute( aVoid );
			showProgress( false );
			launchLandingActivity();
		}
	}
}

