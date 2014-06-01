package com.zagaran.scrubs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zagaran.scrubs.FileManager;

/**
 * Screen On/Off Listener
 * Listens for and records when the screen is turned on or off
 * @author Josh Zagorsky, May 2014
 */
public class ScreenOnOffListener extends BroadcastReceiver {
	FileManager logFile = FileManager.getDebugLogFile();
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Screen on/off
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// TODO: write to a file to record this
			Log.i("ScreenOnOffListener", "Screen turned off"); }
		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.i("ScreenOnOffListener", "Screen turned on"); }
		
		// TODO: TEST THESE:
		// Power connected/disconnected
		else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Log.i("ScreenOnOffListener", "Power connected");
			logFile.write("Power connected");
		} 
		else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Log.i("ScreenOnOffListener", "Power disconnected"); 
			logFile.write("Power disconnected");
		}
		
		// Device turned on/off
		else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i("ScreenOnOffListener", "Device finished booting"); }
		else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Log.i("ScreenOnOffListener", "Device is shutting down"); }
		else if (intent.getAction().equals(Intent.ACTION_REBOOT)) {
			Log.i("ScreenOnOffListener", "Device is rebooting"); }
	}
	
}
