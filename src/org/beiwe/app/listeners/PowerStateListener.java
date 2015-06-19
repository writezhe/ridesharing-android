package org.beiwe.app.listeners;

import org.beiwe.app.storage.TextFileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Listens for power state changes.
 *  Screen On/Off, Power Connect/Disconnect, Airplane Mode.
 *  @author Josh Zagorsky, Eli Jones, May/June 2014 */
public class PowerStateListener extends BroadcastReceiver {
	
	public static String header = "timestamp, event";
	private static Boolean started = false;
	
	public static void start(){ started = true; }
	
	/** Handles the logging, includes a new line for the CSV files.
	 * This code is otherwise reused everywhere.*/
	private void makeLogStatement(String message) {
		Log.i("PowerStateListener", message);
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getPowerStateFile().writeEncrypted(javaTimeCode.toString() + TextFileManager.DELIMITER + message );
	}
	
	
	@Override
	public void onReceive(Context externalContext, Intent intent) {
		if (!started) { return; }
		
		// Screen on/off
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { makeLogStatement("Screen turned off"); }
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) { makeLogStatement("Screen turned on"); }
		
		// Power connected/disconnected
		if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) { makeLogStatement("Power connected"); }
		if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) { makeLogStatement("Power disconnected"); }
		
		// Shutdown/Restart
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) { makeLogStatement("Device shut down signal received"); }
		if (intent.getAction().equals(Intent.ACTION_REBOOT)) { makeLogStatement("Device reboot signal received"); }
	}
}