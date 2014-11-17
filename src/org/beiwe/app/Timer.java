package org.beiwe.app;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

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
 * @author Eli, Dor */
public class Timer {
	private AlarmManager alarmManager;
	private BackgroundProcess backgroundProcess;
	private Context appContext;

	// Control Message Intents
	public static Intent accelerometerOffIntent;
	public static Intent accelerometerOnIntent;
	public static Intent bluetoothOffIntent;
	public static Intent bluetoothOnIntent;
	public static Intent dailySurveyIntent;
	public static Intent gpsOffIntent;
	public static Intent gpsOnIntent;
	public static Intent signoutIntent;
	public static Intent voiceRecordingIntent;
	public static Intent weeklySurveyIntent;
	public static Intent wifiLogIntent;
	// FIXME: add an upload datafiles/download new survey timer; call it networkTimer or something
	
	// Timer intents
	public static Intent accelerometerTimerIntent;
	public static Intent bluetoothTimerIntent;
	public static Intent GPSTimerIntent;
	public static Intent wifiLogTimerIntent;
	
	// Intent filters
	public IntentFilter getAccelerometerOffIntentFilter() { return new IntentFilter( accelerometerOffIntent.getAction() ); }
	public IntentFilter getAccelerometerOnIntentFilter() { return new IntentFilter( accelerometerOnIntent.getAction() ); }
	public IntentFilter getBluetoothOffIntentFilter() { return new IntentFilter( bluetoothOffIntent.getAction() ); }
	public IntentFilter getBluetoothOnIntentFilter() { return new IntentFilter( bluetoothOnIntent.getAction() ); }
	public IntentFilter getDailySurveyIntentFilter() { return new IntentFilter( dailySurveyIntent.getAction() ); }
	public IntentFilter getGPSIntentOffFilter() { return new IntentFilter( gpsOffIntent.getAction() ); }
	public IntentFilter getGPSIntentOnFilter() { return new IntentFilter( gpsOnIntent.getAction() ); }
	public IntentFilter getSignoutIntentFilter() { return new IntentFilter( signoutIntent.getAction() ); }
	public IntentFilter getVoiceRecordingIntentFilter() { return new IntentFilter( voiceRecordingIntent.getAction() ); }
	public IntentFilter getWeeklySurveyIntentFilter() { return new IntentFilter( weeklySurveyIntent.getAction() ); }
	public IntentFilter getWifiLogFilter() { return new IntentFilter( wifiLogTimerIntent.getAction() ); }	
	
	//The timer offset is a random value that is inserted into time calculations to make them occur at a variable time
	private final static long EXACT_TIMER_OFFSET = 2856000;
	//TODO: Eli. test.
	private static int getRandomTimeOffset(){
		Random random = new Random();
		return random.nextInt(1800000);  //this would result in a half hour window, right?
	}
	
	// Constructor
	public Timer( BackgroundProcess backgroundProcess ) {
		this.backgroundProcess = backgroundProcess;
		appContext = backgroundProcess.getApplicationContext();
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService( Context.ALARM_SERVICE ));
		
		// Set up intent on/off intents
		accelerometerOffIntent = setupIntent( appContext.getString(R.string.accelerometer_off) );
		accelerometerOnIntent = setupIntent( appContext.getString(R.string.accelerometer_on) );
		bluetoothOffIntent = setupIntent( appContext.getString(R.string.bluetooth_off) );
		bluetoothOnIntent = setupIntent( appContext.getString(R.string.accelerometer_on) );
		dailySurveyIntent = setupIntent( appContext.getString(R.string.daily_survey) );
		gpsOffIntent = setupIntent( appContext.getString(R.string.gps_off) );
		gpsOnIntent = setupIntent( appContext.getString(R.string.gps_on) );
		signoutIntent = setupIntent( appContext.getString(R.string.signout_intent) );
		voiceRecordingIntent = setupIntent( appContext.getString(R.string.voice_recording) );
		weeklySurveyIntent = setupIntent( appContext.getString(R.string.weekly_survey) );
		wifiLogTimerIntent = setupIntent( appContext.getString( R.string.wifi_timer ) );
		
		// Set up timer intents
		accelerometerTimerIntent = setupIntent( appContext.getString(R.string.action_accelerometer_timer) );
		bluetoothTimerIntent = setupIntent( appContext.getString(R.string.action_bluetooth_timer) );
		GPSTimerIntent = setupIntent( appContext.getString(R.string.action_gps_timer) );
		wifiLogIntent = setupIntent( appContext.getString(R.string.action_wifi_log) );
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
	/** Single exact alarm for events that happen in pairs, e.g. [sensor on]-[sensor off]. */
	public void setupSingularExactAlarm( Long milliseconds, Intent timerIntent, Intent intentToBeBroadcast ) {
		Long nextTriggerTime = System.currentTimeMillis() + milliseconds;
		PendingIntent pendingTimerIntent = registerAlarm( intentToBeBroadcast, timerIntent );
		/* The alarmManager.setExact(*parameters*) operation makes exact alarms.  They are guaranteed
		 * to go off at the precise/exact time that you specify. */
		setAsExactAsPossible(alarmManager, AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent);
	}
	/** Single exact alarm for an event that happens once */
	public void setupSingularExactAlarm(Long milliseconds, Intent intentToBeBroadcast) {
		Long triggerTime = System.currentTimeMillis() + milliseconds;
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		setAsExactAsPossible(alarmManager, AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
	}
	
	
	/** Set a repeating, once-a-day alarm. Uses AlarmManager.setRepeating, which may not be precise
	 * @param hourOfDay in 24-hr time, when the alarm should fire. E.g., "19" means 7pm every day
	 * @param intentToBeBroadcast the intent to be broadcast when the alarm fires      */
	public void setupDailyRepeatingAlarm(int hourOfDay, Intent intentToBeBroadcast) {
		// TODO: Josh, purge existing alarms?
		// reply: Eli. I don't Think that is necessary.  Alarms can only have 1 of a given name, so even if the app is for some reason rescheduling the new alarm should replace the old alarm.
		Calendar date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, hourOfDay);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		long triggerAtMillis = date.getTimeInMillis(); // TODO: Josh use the below line for debugging
		//long triggerAtMillis = System.currentTimeMillis() - 5000L;
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		long oneDayInMillis = 24 * 60 * 60 * 1000L; // TODO: Josh use the line below for debugging
		//long oneDayInMillis = 20 * 1000L;
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, oneDayInMillis, pendingIntent);		
	}
	
	
	/** Set a repeating, once-a-week alarm. Uses AlarmManager.setRepeating, which may not be precise
	 * @param dayOfWeek Sunday = 1, Saturday = 7; or use Calendar.SUNDAY, Calendar.MONDAY, etc.
	 * @param hourOfDay in 24-hr time, when the alarm should fire. E.g., "19" means 7pm every day
	 * @param intentToBeBroadcast the intent to be broadcast when the alarm fires      */
	public void setupWeeklyRepeatingAlarm(int dayOfWeek, int hourOfDay, Intent intentToBeBroadcast) {
		Calendar date = new GregorianCalendar();
		date.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		date.set(Calendar.HOUR_OF_DAY, hourOfDay);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		long triggerAtMillis = date.getTimeInMillis(); // TODO: Josh, use the line below for debugging
		//long triggerAtMillis = System.currentTimeMillis() - 5000L;
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		long oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L; // TODO: Josh use the line below for debugging
		//long oneWeekInMillis = 42 * 1000L;
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, oneWeekInMillis, pendingIntent);		
	}
	
	
	/** setupExactHourlyAlarm creates an Exact Alarm that will go off at a specific hourly offset,
	 * based on the EXACT_TIMER_OFFSET defined above.
	 * setupExactHourlyAlarm is used for the Bluetooth timer, because the trigger needs to be synchronized
	 * between devices. */
	public void setupExactHourlyAlarm( Intent timerIntent, Intent intentToBeBroadcast ) {	
		long currentTime = System.currentTimeMillis();
		// current unix time (mod) 3,600,000 milliseconds = a next hour boundry, then add the EXACT_TIMER_OFFSET
		Long nextTriggerTime = currentTime - ( currentTime % (long) 3600000 ) + EXACT_TIMER_OFFSET;
		if (nextTriggerTime < currentTime) { nextTriggerTime += 3600000; }
		PendingIntent pendingTimerIntent = registerAlarm( intentToBeBroadcast, timerIntent );
		setAsExactAsPossible(alarmManager, AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent);
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
	
	
	/** Calls AlarmManager.set() for API < 19, and AlarmManager.setExact() for API 19+
	 * For an exact alarm, it seems you need to use .set() for API 18 and below, and
	 * .setExact() for API 19 (KitKat) and above. */
	private void setAsExactAsPossible(AlarmManager alarmManager, int type, long triggerAtMillis, PendingIntent operation) {
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion < android.os.Build.VERSION_CODES.KITKAT) {
			alarmManager.set(type, triggerAtMillis, operation);
		}
		else {
			alarmManager.setExact(type, triggerAtMillis, operation);
		}	
	}
}
