package com.gorgon.mapview.utils;

import android.util.Log;

public class Helper {
	
	public static String TAG = "Simple_MapView";
	
	/**
	 * Prints debug message to console (only if DEBUG flag is true)
	 * @param msg - String to print
	 */
	public static void LOGD(String msg) {
		if(Preferences.DEBUG_MODE)
			Log.d(TAG, msg);
	}
	
	/**
	 * Prints error message to console
	 * @param msg
	 */
	public static void LOGE(String msg) {
		if(Preferences.DEBUG_MODE)
			Log.e(TAG, msg);
	}
	
	/**
	 * Prints exception handling message to console
	 * @param msg
	 */
	public static void LOGEx(String str, Exception ex) {
		if(Preferences.DEBUG_MODE)
			Log.e(TAG, "Caugth " + str + ": " + ex.toString());
	}
}
