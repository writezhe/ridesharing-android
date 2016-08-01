package org.beiwe.app.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/** Contains a single function to check whether wifi is active and functional.
 * @author Eli Jones, Joshua Zagorsky */

public class NetworkUtility {
	/**Return TRUE if WiFi is connected; FALSE otherwise
	 * @return boolean value of whether the wifi is on and network connectivity is available. */
	public static Boolean connectedToWifi(Context appContext) {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}
	
	public static Boolean connectedToWifiOrCellularData(Context appContext) {
		//TODO: Make this function work. Here's one possibility: http://stackoverflow.com/a/10009861
		return false;
	}
}
