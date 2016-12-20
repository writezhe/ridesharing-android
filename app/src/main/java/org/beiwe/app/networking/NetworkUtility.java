package org.beiwe.app.networking;

import org.beiwe.app.storage.PersistentData;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;


/** Contains a single function to check whether wifi is active and functional.
 * @author Eli Jones, Joshua Zagorsky */

//TODO: Eli. low priority. Investigate the class local option that android studio is prompting for
public class NetworkUtility {
	
	/**Return TRUE if WiFi is connected; FALSE otherwise.
	 * Android 6 adds support for multiple network connections of the same type and the older get-network-by-type command is deprecated.
	 * We need to handle both cases.
	 * @return boolean value of whether the wifi is on and network connectivity is available. */
	public static Boolean canUpload( Context appContext ) {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// If the device has had the upload over cellular flag set to true we run this logic
		if ( PersistentData.getAllowUploadOverCellularData() ) {
			Log.i("WIFICHECK", "ALLOW OVER CELLULAR!!!!");
			NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
			if ( activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable() ) {
				return true; }
//			else { return false; } // hookay, we can disable this, and run through the rest of the logic. This behavior should be safer if the android networking stack is... weird.
		}
		
		//If the user is restricted to wifi uploads we do this logic, we have android +- 6 code paths.
		//do android < 6
		if (android.os.Build.VERSION.SDK_INT < 23) { return oldTimeyWiFiConnectivityCheck(connManager); }
		//do android >= 6
		return newFangledWiFiConnectivityCheck(connManager);
	}
	
	
	@SuppressWarnings("deprecation")
	/** This is the function for running pre-Android 6 wifi connectivity checks.
	 *  This code is separated so that the @SuppressWarnings("deprecation") decorator 
	 *  does not cause headaches if something else is deprecated in the future. */
	private static Boolean oldTimeyWiFiConnectivityCheck( ConnectivityManager connManager ){
		Log.i("WIFICHECK", "oldTimeyWiFiConnectivityCheck");
		NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return networkInfo.isConnected() && networkInfo.isAvailable();
	}
	
	
	/** This is the function for running Android 6+ wifi connectivity checks. */
	private static Boolean newFangledWiFiConnectivityCheck( ConnectivityManager connManager ){
		Log.i("WIFICHECK", "newFangledWiFiConnectivityCheck");
		Network[] networks = connManager.getAllNetworks();
		if (networks == null) { //No network connectivity at all,
			return false; }     //so return no connectivity.
		
		for (Network network : networks ) {
			NetworkInfo networkInfo = connManager.getNetworkInfo(network);
			if ( networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected() && networkInfo.isAvailable()) {
				return true; } //return true if there is a connected and available wifi connection! 
		}
		return false; //there were no wifi-type network connections active and available, return false.
	}
	
}

