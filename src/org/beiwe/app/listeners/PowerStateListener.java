package org.beiwe.app.listeners;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.storage.TextFileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Listens for power state changes.
 *  Screen On/Off, Power Connect/Disconnect, Airplane Mode.
 *  @author Josh Zagorsky, Eli Jones, May/June 2014 */
public class PowerStateListener extends BroadcastReceiver {
	
	public static String header = "time, event\n";
	private BackgroundProcess backgroundProcess;
	
	/** Handles the logging, includes a new line for the CSV files.
	 * This code is otherwised reused everywhere.*/
	private void make_log_statement(String message) {
		Log.i("PowerStateListener", message);
		Long javaTimeCode = System.currentTimeMillis();
//		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + "," + message +"\n" ); 
		TextFileManager.getPowerStateFile().write(javaTimeCode.toString() + TextFileManager.delimiter + message + '\n');
	}
	
	/** In order to acces the functions of the background Process we need to create a new constructor,
	 * one that takes a BackgroundProcess. 
	 * @param backgroundProcess */
	
	public void finish_instantiation( BackgroundProcess backgroundProcess ) {
		this.backgroundProcess = backgroundProcess;
	}
	
	@Override
	public void onReceive(Context externalContext, Intent intent) {
		//make a log of all receipts (for debugging)
		make_log_statement("the following intent was recieved by the PowerStateListener:" + intent.getAction().toString());
		
		// Screen on/off
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { make_log_statement("Screen turned off"); }
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) { make_log_statement("Screen turned on"); }
		
		// Power connected/disconnected
		if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) { make_log_statement("Power connected"); }
		if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) { make_log_statement("Power disconnected"); }
		
		// Shutdown/Restart
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) { make_log_statement("Device shut down signal received"); }
		if (intent.getAction().equals(Intent.ACTION_REBOOT)) { make_log_statement("Device reboot signal received"); }
		
		//Order is not guaranteed for the airplane mode change intent, so we have to call the 
		//BackgroundProcess doAirplaneModeThings().  This function is idempotent and synchronized.
		if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
			//FIXME: this is breaking on the call to the background process
			Log.i("something", "anything");
			backgroundProcess.doAirplaneModeThings();  
			Log.i("anything", "something");
		}
	}
}