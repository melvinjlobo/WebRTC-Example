package com.abysmel.exwebrtc.Utils;

import android.util.Log;

import com.abysmel.exwebrtc.Application.ExWRTCApplication;
import com.abysmel.exwebrtc.constants.Constants;


/**
 * Created by Melvin Lobo on 6/4/2016.
 *
 * Class for conditional logging. Uses Android Log internally
 */
public class Logger {

	////////////////////////////////////// CLASS MEMBERS ///////////////////////////////////////////
	private static boolean mDevModeEnabled = Constants.isDevMode;

	////////////////////////////////////// CLASS METHODS ///////////////////////////////////////////
	public static void e(String msg) {
		e( ExWRTCApplication.TAG, msg);
	}

	public static void e(String tag, String msg) {
		if (mDevModeEnabled) Log.e(tag, msg);
	}

	public static void w(String msg) {
		w(ExWRTCApplication.TAG, msg);
	}

	public static void w(String tag, String msg) {
		if (mDevModeEnabled) Log.w(tag, msg);
	}

	public static void i(String msg) {
		i(ExWRTCApplication.TAG, msg);
	}

	public static void i(String tag, String msg) {
		if (mDevModeEnabled) Log.i(tag, msg);
	}

	public static void d(String msg) {
		d(ExWRTCApplication.TAG, msg);
	}

	public static void d(String tag, String msg) {
		if (mDevModeEnabled) Log.d(tag, msg);
	}

	public static void v(String msg) {
		v(ExWRTCApplication.TAG, msg);
	}

	public static void v(String tag, String msg) {
		if (mDevModeEnabled) Log.v(tag, msg);
	}

	public static void enable() {
		mDevModeEnabled = true;
	}

	public static void disable() {
		mDevModeEnabled = false;
	}

	public static boolean isEnabled() {
		return mDevModeEnabled;
	}
}
