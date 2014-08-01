package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

/** The Timer class provides a meeans of setting various timers.  These are used by the BackgroundProcess
 * for devices that must be turned on/off, and timing the user to automatically logout after a period of time.
 * This class includes all the Intents and IntentFilters we for trigged broadcasts.
 * @author Eli, Dori */
public class Timer {
	private AlarmManager alarmManager;
	private BackgroundProcess backgroundProcess;
	private BroadcastReceiver broadcastReceiver;
	private Context appContext;
	
	//public strings for matching to messages
	//TODO: should we move these to the android Strings resource file?
	public static final String ACCELEROMETER_OFF = "Accelerometer_OFF";
	public static final String ACCELEROMETER_ON = "Accelerometer On";
	public static final String BLUETOOTH_OFF = "Bluetooth_OFF";
	public static final String BLUETOOTH_ON = "Bluetooth On";
	public static final String GPS_OFF = "GPS_OFF";
	public static final String GPS_ON = "GPS On";
	public static final String SIGN_OUT = "Signout";
	
	// Intents
	public static final Intent signoutIntent = setupIntent( SIGN_OUT );
	public static final Intent accelerometerOffIntent = setupIntent( ACCELEROMETER_OFF );
	public static final Intent accelerometerOnIntent = setupIntent( ACCELEROMETER_ON );
	public static final Intent bluetoothOffIntent = setupIntent( BLUETOOTH_OFF );
	public static final Intent bluetoothOnIntent = setupIntent( BLUETOOTH_ON );
	public static final Intent GPSOffIntent = setupIntent( GPS_OFF );
	public static final Intent GPSOnIntent = setupIntent( GPS_ON);
	
	// Intent filters
	public IntentFilter getSignoutIntentFilter() { return new IntentFilter( signoutIntent.getAction() ); }
	public IntentFilter getAccelerometerOffIntentFilter() { return new IntentFilter( accelerometerOffIntent.getAction() ); }
	public IntentFilter getAccelerometerOnIntentFilter() { return new IntentFilter( accelerometerOnIntent.getAction() ); }
	public IntentFilter getBluetoothOffIntentFilter() { return new IntentFilter( bluetoothOffIntent.getAction() ); }
	public IntentFilter getBluetoothOnIntentFilter() { return new IntentFilter( bluetoothOnIntent.getAction() ); }
	public IntentFilter getGPSIntentOffFilter() { return new IntentFilter( GPSOffIntent.getAction() ); }
	public IntentFilter getGPSIntentOnFilter() { return new IntentFilter( GPSOnIntent.getAction() ); }
	
	// Constructor
	public Timer(BackgroundProcess backgroundProcess) {
		this.backgroundProcess = backgroundProcess;
		this.appContext = backgroundProcess.getApplicationContext();
	}

	// Setup custom intents to be sent to the listeners running in the background process
	private static Intent setupIntent(String action){
		Intent newIntent = new Intent();
		newIntent.setAction(action);
		return newIntent; }

	/**Actions in Android are saved as values in locations. For example ACTION_CALL is actually saved as android.intent.action.CALL.
	 * When using an IntentFilter as an "action filter", we will use this convention, and start the timer using the action 
	 * "org.beiwe.app.START_TIMER". This will be registered in the background process, and will start the PendingIntent 
	 * (an intent waiting for something to call it) in the timer. */
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
