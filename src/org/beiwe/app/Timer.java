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
	
	private static final String SIGN_OUT_TIMER = "Signout timer";
	private static final String ACCELEROMETER_TIMER = "accelerometer timer";
	private static final String BLUETOOTH_TIMER = "bluetooth timer";
	private static final String GPS_TIMER = "gps timer";
	
	// Intents
	public static final Intent signoutIntent = setupIntent( SIGN_OUT );
	
	public static final Intent accelerometerOffIntent = setupIntent( ACCELEROMETER_OFF );
	public static final Intent accelerometerOnIntent = setupIntent( ACCELEROMETER_ON );
	public static final Intent bluetoothOffIntent = setupIntent( BLUETOOTH_OFF );
	public static final Intent bluetoothOnIntent = setupIntent( BLUETOOTH_ON );
	public static final Intent GPSOffIntent = setupIntent( GPS_OFF );
	public static final Intent GPSOnIntent = setupIntent( GPS_ON);
	
	public static final Intent accelerometerTimerIntent = setupIntent( ACCELEROMETER_TIMER );
	public static final Intent bluetoothTimerIntent = setupIntent( BLUETOOTH_TIMER );
	public static final Intent GPSTimerIntent = setupIntent( GPS_TIMER );
	public static final Intent signOutTimerIntent = setupIntent( SIGN_OUT_TIMER );
	
	
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
		this.alarmManager = (AlarmManager)( backgroundProcess.getSystemService( Context.ALARM_SERVICE ));
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
	public void setupRepeatingAlarm(int milliseconds, Intent timerIntent, Intent intentToBeBroadcast) {
		long nextTriggerTime = System.currentTimeMillis() + milliseconds;
		
		//create a pending intent from the timerIntent, then pass all these intents to the repeatingAlarmReceiver,
		// then register the returned BroadcastReceiver with the BackgroundProcess, then set the actual alarm. 
		PendingIntent pendingTimerIntent = PendingIntent.getBroadcast(appContext, 0, timerIntent, 0);
		
		//TODO: this BroadcastReceiver was a global variable, changed it to a local variable.  Determine if this is bad...
		BroadcastReceiver broadcastReceiver = repeatingAlarmReceiver(milliseconds, pendingTimerIntent, intentToBeBroadcast, timerIntent);
		backgroundProcess.registerReceiver( broadcastReceiver, new IntentFilter( timerIntent.getAction() ) );
		alarmManager.set( AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent);
		Log.i("Timer", "repeating alarm started");
	}

	// TODO: make SetupNonRepeatingAlarm
	// TODO: make absoluteTimeAlarm (At 14:15, 15:15, etc...)
	
	private BroadcastReceiver repeatingAlarmReceiver
	(final int milliseconds, final PendingIntent pendingIntent, final Intent sendingIntent, final Intent originalTimerIntent) {		
		return new BroadcastReceiver() {
			@Override
			//TODO: I am fairly sure that the receivedIntent will match the originalTimerIntent, test.
			public void onReceive(Context appContext, Intent receivedIntent) {
				Toast.makeText(appContext, "Receive broadcast, reseting timer...", Toast.LENGTH_SHORT).show();
				backgroundProcess.unregisterReceiver(this);
				appContext.sendBroadcast( sendingIntent );
				setupRepeatingAlarm( milliseconds , originalTimerIntent, sendingIntent);
			} }; }
	
	
	
	private BroadcastReceiver singleAlarmReceiver
	(final int milliseconds, final PendingIntent pendingIntent, final Intent sendingIntent) {		
		return new BroadcastReceiver() {
			@Override
			//TODO: I am fairly sure that the receivedIntent will match the originalTimerIntent, test.
			public void onReceive(Context appContext, Intent receivedIntent) {
				Toast.makeText(appContext, "Receive broadcast, reseting timer...", Toast.LENGTH_SHORT).show();
				backgroundProcess.unregisterReceiver(this);
				appContext.sendBroadcast( sendingIntent );
			} }; }
}
