package com.zagaran.scrubs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zagaran.scrubs.CSVFileManager;

/**
 * Screen On/Off and Battery/Power State Listener
 * Listens for and records when the screen is turned on or off
 * @author Josh Zagorsky, May 2014
 */
public class PowerStateListener extends BroadcastReceiver {
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
		
		// Power connected/disconnected
		else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Log.i("ScreenOnOffListener", "Power connected");
			logFile.write("Power connected.\n");
		} 
		else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
			Log.i("ScreenOnOffListener", "Power disconnected"); 
			logFile.write("Power disconnected.\n");
		}
		
		// TODO: make this work (probably by making the Service run on boot of the device)
		// Probably need Manifest.xml intent-filters for android.intent.action.BOOT_COMPLETED
		// and also android.intent.action.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE
		// see here: http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
		
		// TODO: figure out what the problem is with this crash that happened after
		// I opened the app or plugged in the phone or something:
		// java.lang.RuntimeException: Unable to instantiate receiver com.zagaran.scrubs.PowerStateListener: java.lang.NullPointerException: you need to call startFileManager.		
		
		// Device turned on/off
		else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i("ScreenOnOffListener", "Device finished booting."); }
		else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			Log.i("ScreenOnOffListener", "Device is shutting down."); }
		else if (intent.getAction().equals(Intent.ACTION_REBOOT)) {
			Log.i("ScreenOnOffListener", "Device is rebooting."); }
	}
	
}
