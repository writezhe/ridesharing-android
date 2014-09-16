package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

/** The Timer class provides a meeans of setting various timers.  These are used by the BackgroundProcess
 * for devices that must be turned on/off, and timing the user to automatically logout after a period of time.
 * This class includes all the Intents and IntentFilters we for trigged broadcasts.
 * @author Eli, Dori */
public class Timer {
	private AlarmManager alarmManager;
	private BackgroundProcess backgroundProcess;
	private Context appContext;
	
	//public strings for matching to messages
	//TODO: Eli/Dori should we move these to the android Strings resource file? Deprecate
//	public static final String ACCELEROMETER_TURN_OFF = "Accelerometer OFF";
//	public static final String ACCELEROMETER_TURN_ON = "Accelerometer On";
//	public static final String BLUETOOTH_TURN_OFF = "Bluetooth OFF";
//	public static final String BLUETOOTH_TURN_ON = "Bluetooth On";
//	public static final String GPS_TURN_OFF = "GPS OFF";
//	public static final String GPS_TURN_ON = "GPS On";
//	public static final String SIGN_OUT = "Signout";
	
	// Intents - Fixed by Dori ;)
	public static Intent signoutIntent;
	public static Intent accelerometerOffIntent;
	public static Intent accelerometerOnIntent;
	public static Intent bluetoothOffIntent;
	public static Intent bluetoothOnIntent;
	public static Intent gpsOffIntent;
	public static Intent gpsOnIntent;
	
	// Timer intents - Also fixed by Dori ;)
	public static Intent accelerometerTimerIntent;
	public static Intent bluetoothTimerIntent;
	public static Intent GPSTimerIntent;
	public static Intent signOutTimerIntent;
	
	
	// Intent filters
	public IntentFilter getSignoutIntentFilter() { return new IntentFilter( signoutIntent.getAction() ); }
	public IntentFilter getAccelerometerOffIntentFilter() { return new IntentFilter( accelerometerOffIntent.getAction() ); }
	public IntentFilter getAccelerometerOnIntentFilter() { return new IntentFilter( accelerometerOnIntent.getAction() ); }
	public IntentFilter getBluetoothOffIntentFilter() { return new IntentFilter( bluetoothOffIntent.getAction() ); }
	public IntentFilter getBluetoothOnIntentFilter() { return new IntentFilter( bluetoothOnIntent.getAction() ); }
	public IntentFilter getGPSIntentOffFilter() { return new IntentFilter( gpsOffIntent.getAction() ); }
	public IntentFilter getGPSIntentOnFilter() { return new IntentFilter( gpsOnIntent.getAction() ); }
	
	private final static long EXACT_TIMER_OFFSET = 2856000;
	
	// Constructor
	public Timer( BackgroundProcess backgroundProcess ) {
		this.backgroundProcess = backgroundProcess;
		appContext = backgroundProcess.getApplicationContext();
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService( Context.ALARM_SERVICE ));
		
		// Set up intent on/off intents
		signoutIntent = setupIntent( appContext.getString(R.string.signout_intent) );
		accelerometerOffIntent = setupIntent( appContext.getString(R.string.accelerometer_off) );
		accelerometerOnIntent = setupIntent( appContext.getString(R.string.accelerometer_on) );
		bluetoothOffIntent = setupIntent( appContext.getString(R.string.bluetooth_off) );
		bluetoothOnIntent = setupIntent( appContext.getString(R.string.accelerometer_on) );
		gpsOffIntent = setupIntent( appContext.getString(R.string.gps_off) );
		gpsOnIntent = setupIntent( appContext.getString(R.string.gps_on));
		
		Log.i("Timer", signoutIntent.toString()); // POC
		
		// Set up timer intents
		accelerometerTimerIntent = setupIntent( appContext.getString(R.string.action_accelerometer_timer) );
		bluetoothTimerIntent = setupIntent( appContext.getString(R.string.action_bluetooth_timer) );
		GPSTimerIntent = setupIntent( appContext.getString(R.string.action_gps_timer) );
		signOutTimerIntent = setupIntent( appContext.getString(R.string.action_signout_timer) );
		
		Log.i("Timer", signOutTimerIntent.toString()); // Yet another POC
	}

	// Setup custom intents to be sent to the listeners running in the background process
	private static Intent setupIntent( String action ){
		Intent newIntent = new Intent();
		newIntent.setAction( action );
		return newIntent; }
	
	/** A BroadcastReceiver that we register with the background service.
	 * BroadcastReceivers are registered along with an IntentFilter, and when an Intent matching that IntentFilter
	 * is broadcast, this code block's onReceive function will be called.  This onReceive function triggers
	 * our own intent to be Broadcast. 
	 * @param intentToBeBroadcast An Intent that will be broadcast if the onReceive function is triggered. 
	 * @return the BroadcastReceiver object */
	private BroadcastReceiver alarmReceiver (final Intent intentToBeBroadcast) {		
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context appContext, Intent receivedIntent) {
				backgroundProcess.unregisterReceiver(this);
				appContext.sendBroadcast( intentToBeBroadcast );
			} }; }
		
	/** Actions in Android are saved as values in locations. For example ACTION_CALL is actually saved as android.intent.action.CALL.
	 * When using an IntentFilter as an "action filter", we will use this convention, and start the timer using the action 
	 * "org.beiwe.app.START_TIMER". This will be registered in the background process, and will start the PendingIntent 
	 * (an intent waiting for something to call it) in the timer. */
	public void setupSingularFuzzyAlarm(Long milliseconds, Intent timerIntent, Intent intentToBeBroadcast) {
		long nextTriggerTime = SystemClock.elapsedRealtime() + milliseconds;
		PendingIntent pendingTimerIntent = registerAlarm(intentToBeBroadcast, timerIntent);
		/* The alarmManager.set(*parameters*) operation, as of API 19, makes "inexact" alarms.  They are guaranteed
		 * to go off, but not at the precise/exact time that you specify.  This is to improve battery life. */
		alarmManager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTriggerTime, pendingTimerIntent);
		Log.i("Timer", "singular fuzzy alarm started");
	}
	public void setupSingularExactAlarm( Long milliseconds, Intent timerIntent, Intent intentToBeBroadcast ) {
		Long nextTriggerTime = System.currentTimeMillis() + milliseconds;
		PendingIntent pendingTimerIntent = registerAlarm( intentToBeBroadcast, timerIntent );
		/* The alarmManager.setExact(*parameters*) operation makes exact alarms.  They are guaranteed
		 * to go off at the precise/exact time that you specify. */
		alarmManager.setExact( AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent );
	}
	
	/* setupExactHourlyAlarm creates an Exact Alarm that will go off at a specific hourly offset,
	 * based on the EXACT_TIMER_OFFSET defined above.
	 * setupExactHourlyAlarm is used for the Bluetooth timer, because the trigger needs to be synchronized
	 * between devices. */
	//TODO: Eli.  CURRENTLY DEBUGGING!  THIS DOES NOT SET ANYTHING TO AN HOURLY REPEAT, NEED TO UNCOMMENT LINE OF CODE.
	public void setupExactHourlyAlarm( Intent timerIntent, Intent intentToBeBroadcast ) {	
		long currentTime = System.currentTimeMillis();
		// current unix time (mod) 3,600,000 milliseconds = a next hour boundry, then add the EXACT_TIMER_OFFSET
		Long nextTriggerTime = currentTime - ( currentTime % (long) 3600000 ) + EXACT_TIMER_OFFSET;
		if (nextTriggerTime < currentTime) { nextTriggerTime += 3600000; }
//		Long nextTriggerTime = System.currentTimeMillis() + 5000;
		PendingIntent pendingTimerIntent = registerAlarm( intentToBeBroadcast, timerIntent );
		alarmManager.setExact( AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent );
	}
	
	/** This function handles the common elements for any alarm creation.
	 * @param intentToBeBroadcast is the intent that the Alarm will broadcast when it goes off.
	 * @param timerIntent registers the alarm under the correct alarm label.
	 * @return a PendingIntent of the timerIntent, which is needed to set an alarm.	 */
	private PendingIntent registerAlarm( Intent intentToBeBroadcast, Intent timerIntent) {
		PendingIntent pendingTimerIntent = PendingIntent.getBroadcast(appContext, 0, timerIntent, PendingIntent.FLAG_ONE_SHOT);
		BroadcastReceiver broadcastReceiver = alarmReceiver(intentToBeBroadcast);
		backgroundProcess.registerReceiver( broadcastReceiver, new IntentFilter( timerIntent.getAction() ) );
		return pendingTimerIntent;
	}
}