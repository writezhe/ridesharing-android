package org.beiwe.app.listeners;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.storage.TextFileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**The BootListener is never actually instantiated elsewhere in the app.  It's job is to sit
 * and wait for either the boot broadcast or the SD (external) applications available.
 * @author Eli */
public class BootListener extends BroadcastReceiver {
	
	TextFileManager logFile = null;
	TextFileManager powerStateLog = null;
	
	/** Checks whether the app is installed on the SD card; needs a context passed in 
	 *  Grab a pagkageManager (general info) -> get packageInfo (info about this package) ->
	 *  ApplicationInfo (information about this application instance).
	 *  http://stackoverflow.com/questions/5814474/how-can-i-find-out-if-my-app-is-installed-on-sd-card */
	private Boolean checkForSDCardInstall(Context externalContext) throws NameNotFoundException{
		PackageManager pkgManager = externalContext.getPackageManager();
		try {
			PackageInfo pkgInfo = pkgManager.getPackageInfo(externalContext.getPackageName(), 0);
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			//appInfo.flags is an int; docs say: "Flags associated with the application. Any combination of... [list_of_flags]."  
			// the following line returns true if the app is installed on an SD card.  
			return (appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE; }
		catch (NameNotFoundException e) {
			Log.i("PowerStateListener", "something is broken in the check for installation on an SD card.");
			throw e; }
	}
	
	/** Does what it says, starts the background service running, also loads log files.
	 *  called when SDcard available and device startup. */	
	private void startBackgroundProcess(Context externalContext){
		//this is the construction for starting a service on reboot.
		Intent intent_to_start_background_service = new Intent(externalContext, BackgroundProcess.class);
	    externalContext.startService(intent_to_start_background_service);
	    Long javaTimeCode = System.currentTimeMillis();
		try { Thread.sleep(30000); }
		catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
		String message = "device reboot, app started.";
		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + TextFileManager.DELIMITER + message + "\n" ); 
		TextFileManager.getPowerStateFile().write(javaTimeCode.toString() + TextFileManager.DELIMITER + message + '\n');
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
			startBackgroundProcess(externalContext); }
		
		if (intent.getAction().equals(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)) {
			/** Almost identical to the boot_completed code, but invert the logic. */
			//If app is installed on the SD card, start the background process/service.
			try { if ( !checkForSDCardInstall(externalContext) ) { return; } }
			catch (NameNotFoundException e) { e.printStackTrace(); }
			startBackgroundProcess(externalContext); }
			
		//these need to be checked whenever the service was started by the user opening the app. 
		if (logFile == null) { logFile = TextFileManager.getDebugLogFile(); }
		if (powerStateLog == null) { powerStateLog = TextFileManager.getPowerStateFile(); }
	}
}
