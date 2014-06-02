package com.zagaran.scrubs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zagaran.scrubs.CSVFileManager;

/**
 * Screen On/Off Listener
 * Listens for and records when the screen is turned on or off
 * @author Josh Zagorsky, May 2014
 */
public class ScreenOnOffListener extends BroadcastReceiver {
	CSVFileManager logFile = CSVFileManager.getDebugLogFile();
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//view all receipts
		logFile.write(intent.getAction()+"\n");
		
		// Screen on/off
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// TODO: write to a file to record this
			Log.i("ScreenOnOffListener", "Screen turned off");
			logFile.write("screen turned off.\n"); }
		
		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.i("ScreenOnOffListener", "Screen turned on"); 
			logFile.write("screen turned on.\n"); }
		
		// TODO: this does not appear to trigger anything
		// Power connected/disconnected
		else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Log.i("ScreenOnOffListener", "Power connected");
			logFile.write("Power connected.\n");
		} 
		else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Log.i("ScreenOnOffListener", "Power disconnected"); 
			logFile.write("Power disconnected.\n");
		}
		
		// Device turned on/off
		else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i("ScreenOnOffListener", "Device finished booting."); }
		else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Log.i("ScreenOnOffListener", "Device is shutting down."); }
		else if (intent.getAction().equals(Intent.ACTION_REBOOT)) {
			Log.i("ScreenOnOffListener", "Device is rebooting."); }
	}
	
}
