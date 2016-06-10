package com.abysmel.exwebrtc.Application;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.abysmel.exwebrtc.Utils.SharedPreferencesHelper;

/**
 * Created by Melvin Lobo on 6/4/2016.
 */
public class ExWRTCApplication extends Application {

	//////////////////////////////////// CLASS MEMBERS /////////////////////////////////////////////
	/**
	 * TAG for debugging
	 */
	public static final String TAG = "ExWebRTCAPP";

	private static Context mApplicationContext = null;

	//////////////////////////////////// CLASS METHODS /////////////////////////////////////////////
	@Override
	public void onCreate() {

		super.onCreate();
		setGlobalContext(); // Set the application context

		//Initialize the shared preferences
		new SharedPreferencesHelper.Builder()
				.setContext(mApplicationContext)
				.setMode( ContextWrapper.MODE_PRIVATE)
				.setSharedPreferenceName(getPackageName())
				.setUseDefaultSharedPreference(true)
				.build();
	}

	/**
	 * Set the application context for later use
	 *
	 * @author Melvin Lobo
	 */
	public void setGlobalContext() {
		mApplicationContext = getApplicationContext();
	}

	/**
	 * Get the application context stored with the application class
	 *
	 * @author Melvin Lobo
	 */
	public static Context getGlobalContext() {
		return mApplicationContext;
	}
}
