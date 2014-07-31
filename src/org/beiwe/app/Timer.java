package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class Timer {
	Context appContext;
	BroadcastReceiver broadcastReceiver;
	AlarmManager alarmManager;
	BackgroundProcess backgroundProcess;

	private Intent signoutIntent = null;
	private Intent GPSIntent = null;
	private Intent powerStateIntent = null;
	private Intent accelerometerIntent = null;
	private Intent bluetoothIntent = null;


	// All the intent actions are located here
	public static final String SIGN_OUT = "Signout";
	public static final String GPS = "GPS";
	public static final String POWER_STATE = "PowerState";
	public static final String ACCELEROMETER = "Accelerometer";
	public static final String BLUETOOTH = "Bluetooth";

	// Intent filters
	public IntentFilter getSignoutIntentFilter() { return new IntentFilter( signoutIntent.getAction() ); 	}
	public IntentFilter getGPSIntentFilter() { return new IntentFilter( GPSIntent.getAction() ); 	}
	public IntentFilter getPowerStateIntentFilter() { return new IntentFilter( powerStateIntent.getAction() ); 	}
	public IntentFilter getAccelerometerIntentFilter() { return new IntentFilter( accelerometerIntent.getAction() ); 	}
	public IntentFilter getBluetoothIntentFilter() { return new IntentFilter( bluetoothIntent.getAction() ); 	}

	// Intent getters
	public Intent getSignoutIntent() { return signoutIntent; }
	public Intent getGPSIntent() { return GPSIntent; }
	public Intent getPowerStateIntent() { return powerStateIntent; }
	public Intent getAccelerometerIntent() { return accelerometerIntent; }
	public Intent getBluetoothIntent() { return bluetoothIntent; }

	// Constructor
	public Timer(BackgroundProcess backgroundProcess) {
		this.backgroundProcess = backgroundProcess;
		appContext = backgroundProcess.getApplicationContext();
		
		// Setting up custom intents for the filters in the background service
		signoutIntent = setupCustomIntent(SIGN_OUT);
		GPSIntent = setupCustomIntent(GPS);
		powerStateIntent = setupCustomIntent(POWER_STATE);
		accelerometerIntent  = setupCustomIntent(ACCELEROMETER);
		bluetoothIntent = setupCustomIntent(BLUETOOTH);
	}

	// Setup custom intents to be sent to the listeners running in the background process
	private Intent setupCustomIntent(String action){
		Intent newIntent = new Intent();
		newIntent.setAction(action);
		return newIntent; }

	/*
	 * Actions in Android are saved as values in locations. For example ACTION_CALL is actually saved as android.intent.action.CALL.
	 * When using an IntentFilter as an "action filter", we will use this convention, and start the timer using the action 
	 * "org.beiwe.app.START_TIMER". This will be registered in the background process, and will start the PendingIntent 
	 * (an intent waiting for something to call it) in the timer.
	 */
	public void setupAlarm(int milliseconds, Intent customIntent, boolean repeating) {

		//create the intent, and then the pending intent from it		
		Intent eliIntentSteve = new Intent();
		eliIntentSteve.setAction("org.beiwe.app.steve"); // Eli's intent
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, eliIntentSteve, 0);
		//create and then register the broadcastreceiver with the background process
		broadcastReceiver = broadcastReceiverCreator(milliseconds, pendingIntent, customIntent, repeating);
		backgroundProcess.registerReceiver( broadcastReceiver, new IntentFilter( eliIntentSteve.getAction() ) );

		//and then start the alarm manager to actually trigger that intent it X seconds

		// These two lines actually wake up the broadcast receiver to check for incoming intents
		// http://developer.android.com/reference/android/app/AlarmManager.html#ELAPSED_REALTIME
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService(Context.ALARM_SERVICE ));
		long nextTriggerTime = System.currentTimeMillis() + milliseconds;
		alarmManager.set( AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingIntent); 
		Log.i("alarm manager", "alarm started");
	}

	// TODO: make SetupNonRepeatingAlarm
	// TODO: make absoluteTimeAlarm (At 14:15, 15:15, etc...)

	public BroadcastReceiver broadcastReceiverCreator(final int milliseconds, final PendingIntent pendingIntent,
			final Intent customIntent, final boolean repeating) {		

		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context appContext, Intent intent) {
				Toast.makeText(appContext, "Receive broadcast, reseting timer...", Toast.LENGTH_SHORT).show();
				backgroundProcess.unregisterReceiver(this);

				appContext.sendBroadcast( customIntent );
				if (repeating) {setupAlarm( milliseconds , customIntent, true); }	}
		}; }
}
