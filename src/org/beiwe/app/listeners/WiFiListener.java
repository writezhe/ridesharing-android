package org.beiwe.app.listeners;

import java.util.List;

import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

/**WifiListener
 * WifiListener houses a single public function, scanWifi.  This function grabs the mac
 * addresses of local wifi beacons and writes them to the wifiLog.  It only gets the data
 * if wifi is enabled.
 * @author Eli */
public class WifiListener {
	static WifiManager wifiManager;
	
	public static String header = "timestamp, MAC";
	
	/** WifiListener requires an application context in order to access 
	 * the devices wifi info.  
	 * @param appContext */
	private WifiListener (Context appContext){
		wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
	}
	
	public static void initialize( Context context ) { new WifiListener( context ); } 
	
	//#######################################################################################
	//#############################  WIFI STATE #############################################
	//#######################################################################################
	
	/** checks the state of the devices wifi, returns True if it is on and able to provide
	 * us with SSID broadcast data.
	 * @return boolean of whether we can gather wifi data. */
	private static boolean checkState() {
		int state = wifiManager.getWifiState();
		if (WifiManager.WIFI_MODE_FULL == state ||
			WifiManager.WIFI_MODE_FULL_HIGH_PERF == state ||
			WifiManager.WIFI_MODE_SCAN_ONLY == state ||
			WifiManager.WIFI_STATE_ENABLED == state) {
			return true; }
		return false;
	}
	
	/** Writes to the wifiLog file all mac addresses of local wifi beacons. */
	public static void scanWifi(){
		if ( checkState() ) {
			List<ScanResult> scanResults = wifiManager.getScanResults();		
			Log.i("length", "" + scanResults.size() );
			for (ScanResult result : scanResults){
				TextFileManager.getWifiLogFile().write(result.BSSID);
				Log.i("wifi", result.BSSID);
			}
		}
	}
}