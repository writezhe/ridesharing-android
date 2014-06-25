package com.zagaran.scrubs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.zagaran.scrubs.CSVFileManager;

/**
 * Screen On/Off, Power Connect/Disconnect, Device Boot/Reboot/Shutdown, Airplane Mode.
 * Listens for power state changes.
 * @author Josh Zagorsky, Eli Jones, May/June 2014 */

public class PowerStateListener extends BroadcastReceiver {
	String header = "time, event\n";
	CSVFileManager logFile = null;
	CSVFileManager powerStateLog = null;
	
	private Boolean checkForSDCardInstall(Context externalContext) throws NameNotFoundException{
		/** Checks whether the app is installed on the SD card; needs a context passed in 
		 *  Grab a pagkageManager (general info) -> get packageInfo (info about this package) ->
		 *  ApplicationInfo (information about this application instance).
		 *  http://stackoverflow.com/questions/5814474/how-can-i-find-out-if-my-app-is-installed-on-sd-card */
		PackageManager pkgManager = externalContext.getPackageManager();
		try {
			PackageInfo pkgInfo = pkgManager.getPackageInfo(externalContext.getPackageName(), 0);
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			//appInfo.flags is an int; docs say: "Flags associated with the application. Any combination of... [list_of_flags]."  
			// the following line returns true if the app is installed on an SD card.  
			return (appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE; }
		catch (NameNotFoundException e) {
			Log.i("PowerStateListener", "Things is broken in the check for installation on an SD card.");
			throw e; }
	}
	
	private void startBackgroundProcess(Context externalContext){
		/** does what it says, starts the background service running, also loads log files.
		 * called when SDcard available and device startup. */
		//this is the construction for starting a service on reboot.
		Intent intent_to_start_background_service = new Intent(externalContext, BackgroundProcess.class);
	    externalContext.startService(intent_to_start_background_service);
	    logFile = CSVFileManager.getDebugLogFile();
		powerStateLog = CSVFileManager.getPowerStateFile();
	}
	
	private void make_log_statement(String message) {
		/** Handles the logging, includes a new line for the CSV files.
		 * This code is otherwised reused everywhere.*/
		Log.i("PowerStateListener", message);
		Long javaTimeCode = System.currentTimeMillis();
		logFile.write(javaTimeCode.toString() + "," + message +"\n" ); 
//		powerStateLog.write(javaTimeCode.toString() + + "," + message + "\n");
	}
	
	@Override
	public void onReceive(Context externalContext, Intent intent) {
		
		// Device turned on
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			/** Check whether the app is installed on the SD card, if so we need to
			 *  stop and wait for the ACTION_EXTERNAL_APPLICATIONS_AVAILABLE intent. 
			 *  intent to be sent to us. */
			//if the app is Not on an sd card, start up the background process/service.
			try { if ( checkForSDCardInstall(externalContext) ) { return; } }
			catch (NameNotFoundException e) { e.printStackTrace(); }
			startBackgroundProcess(externalContext);
			make_log_statement("Device booted, background service started"); }
		
		if (intent.getAction().equals(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)) {
			/** Almost identical to the boot_completed code, but invert the logic.*/
			//If app is installed on the SD card, start the background process/service.
			try { if ( !checkForSDCardInstall(externalContext) ) { return; } }
			catch (NameNotFoundException e) { e.printStackTrace(); }
			startBackgroundProcess(externalContext);
			make_log_statement("SD card available, background service started."); }
		
		//these need to be checked whenever the service was started by the user opening the app. 
		if (logFile == null) { logFile = CSVFileManager.getDebugLogFile(); }
		if (powerStateLog == null) { powerStateLog = CSVFileManager.getPowerStateFile(); }
		
		//make a log of all receipts (for debugging)
		make_log_statement("the following intent was recieved by the PowerStateListener:" + intent.getAction().toString()+"\n");
		
		// Screen on/off
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { make_log_statement("Screen turned off"); }
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) { make_log_statement("Screen turned on"); }
		
		// Power connected/disconnected
		if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) { make_log_statement("Power connected"); }
		if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) { make_log_statement("Power disconnected"); }
		
		// Shutdown/Restart
		//TODO: investigate why this is not received
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) { make_log_statement("Device shut down signal received"); }
		if (intent.getAction().equals(Intent.ACTION_REBOOT)) { make_log_statement("Device reboot signal received"); }
		
		if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) { make_log_statement("Airplane mode"); }
	}
}
