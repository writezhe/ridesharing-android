package org.beiwe.app.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;


/** Contains a single function to check whether wifi is active and functional.
 * @author Eli Jones, Joshua Zagorsky */

public class NetworkUtility {
	/**Return TRUE if WiFi is connected; FALSE otherwise
	 * @return boolean value of whether the wifi is on and network connectivity is available. */
	@SuppressWarnings("deprecation")
	public static Boolean getWifiState(Context appContext ) {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo;
		//Android 6 adds support for multiple network connections of the same type, so the get by type command is deprecated.
		if (android.os.Build.VERSION.SDK_INT < 23) { //do android < 6
			networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return networkInfo.isConnected() && networkInfo.isAvailable();
		} else { //do android >= 6
			//FIXME: test.
			Network[] networks = connManager.getAllNetworks();
			if (networks == null) { return false; } //No network connectivity at all.
			for (Network network : connManager.getAllNetworks() ) {
				networkInfo = connManager.getNetworkInfo(network);
				if ( networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected() && networkInfo.isAvailable()) {
					return true; //return true if there is a wifi connection.
				}		
			}
			return false;
		}
	}
}

