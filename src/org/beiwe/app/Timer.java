package org.beiwe.app;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
	private Context appContext;

	// TODO postproduction: change these to non-debug values
	// How frequently it attempts to upload data files
	public static final long UPLOAD_DATA_FILES_PERIOD = 60 * 1000L;
	// How frequently new data files are created
	public static final long CREATE_NEW_DATA_FILES_PERIOD = 15 * 60 * 1000L;
	// How frequently it tries to download new surveys
	public static final long CHECK_FOR_NEW_SURVEYS_PERIOD = 24 * 60 * 60 * 1000L;
	// How frequently the WiFi log runs
	public static final long WIFI_LOG_PERIOD = 5 * 60 * 1000L;
	// How long Bluetooth is on for, each time Bluetooth is turned on
	public static final long BLUETOOTH_ON_DURATION = 1 * 60 * 1000L;
	// How long full Bluetooth on/off cycle period is
	public static final long BLUETOOTH_PERIOD = 5 * 60 * 1000L;
	// When Bluetooth turns on, in terms of how many milliseconds into the Bluetooth period 
	public static final long BLUETOOTH_START_TIME_IN_PERIOD = 0 * 60 * 1000L;
	// How long the accelerometer runs for, each time it's turned on:
	public static final long ACCELEROMETER_ON_DURATION = 10 * 60 * 1000L;
	// The minimum amount of time for which the accelerometer is turned off, each time it's turned off
	public static final long ACCELEROMETER_OFF_MINIMUM_DURATION = 10 * 1000L;
	// How long the GPS runs for, each time the GPS is turned on:
	public static final long GPS_ON_DURATION = 5 * 60 * 1000L;
	// The minimum amount of time for which the GPS is turned off, each time it's turned off
	public static final long GPS_OFF_MINIMUM_DURATION = 5 * 60 * 1000L;
	// Hour, in 24-hour time, that the voice recording notification appears
	public static final int VOICE_RECORDING_HOUR_OF_DAY = 19;
	// Amount of time before the voice recording stops automatically
	public static final long VOICE_RECORDING_MAX_TIME_LENGTH = 5 * 60 * 1000L;
	// Time between when the user last loads a new screen and when the app automatically logs out
	public static final long MILLISECONDS_BEFORE_AUTO_LOGOUT = 5 * 60 * 1000L;
	
	public static final long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000L;
	public static final long ONE_WEEK_IN_MILLISECONDS = 7 * ONE_DAY_IN_MILLISECONDS;
	
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
	public static Intent uploadDatafilesIntent;
	public static Intent createNewDataFilesIntent;
	public static Intent checkForNewSurveysIntent;
	
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
	public IntentFilter getUploadDatafilesIntent() { return new IntentFilter( uploadDatafilesIntent.getAction() ); }
	public IntentFilter getCreateNewDataFilesIntent() { return new IntentFilter( createNewDataFilesIntent.getAction() ); }
	public IntentFilter getCheckForNewSurveysIntent() { return new IntentFilter( checkForNewSurveysIntent.getAction() ); }
		
	// Constructor
	public Timer( BackgroundProcess backgroundProcess ) {
		appContext = backgroundProcess.getApplicationContext();
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService( Context.ALARM_SERVICE ));
		
		// double alarm intents
		accelerometerOffIntent = setupIntent( appContext.getString(R.string.accelerometer_off) );
		accelerometerOnIntent = setupIntent( appContext.getString(R.string.accelerometer_on) );
		bluetoothOffIntent = setupIntent( appContext.getString(R.string.bluetooth_off) );
		bluetoothOnIntent = setupIntent( appContext.getString(R.string.bluetooth_on) );
		gpsOffIntent = setupIntent( appContext.getString(R.string.gps_off) );
		gpsOnIntent = setupIntent( appContext.getString(R.string.gps_on) );
		
		// Set up event triggering alarm intents
		dailySurveyIntent = setupIntent( appContext.getString(R.string.daily_survey) );
		signoutIntent = setupIntent( appContext.getString(R.string.signout_intent) );
		voiceRecordingIntent = setupIntent( appContext.getString(R.string.voice_recording) );
		weeklySurveyIntent = setupIntent( appContext.getString(R.string.weekly_survey) );
		wifiLogIntent = setupIntent( appContext.getString(R.string.run_wifi_log) );
		uploadDatafilesIntent = setupIntent( appContext.getString(R.string.upload_data_files_intent) );
		createNewDataFilesIntent = setupIntent( appContext.getString(R.string.create_new_data_files_intent) );
		checkForNewSurveysIntent = setupIntent( appContext.getString(R.string.check_for_new_surveys_intent) );
	}
	
	/* ######################################################################
	 * ############################ Common Code #############################
	 * ####################################################################*/
	
	// Setup custom intents to be sent to the listeners running in the background process
	private static Intent setupIntent( String action ){
		Intent newIntent = new Intent();
		newIntent.setAction( action );
		return newIntent; }
	
	
	/* ###############################################################################################
	 * ############################ The Various Types of Alarms Creation #############################
	 * #############################################################################################*/
	
	/* "Actions" in Android are saved as DNS-styled strings, "ACTION_CALL" is actually the string "android.intent.action.CALL".
	 * When using an IntentFilter as an... "action filter", we use this convention.
	 * This Intent/IntentFilter is registered with the background process, and will broadcast start the provided PendingIntent.
	 * 
	 * Vocab:
	 *  Fuzzy: an alarm that has a random interval added to the trigger time, for the purpose of load distribution.
	 *  	a fuzzy alarm will be delayed by up to one half hour. 
	 *  Exact: an alarm that Will go off at EXACTLY the time declared. (guaranteed by Android OS)
	 *  Power Optimized: an alarm that will trigger to go off close to the time given, but may be shifted around by Android OS
	 *  	in or to cluster power usage events close together to save battery.
	 *  Single: this alarm will happen once.
	 *  Double: a specific, common pattern in this code base, where one alarm triggers a second alarm.
	 *  Repeating: an alarm that will repeat based on some well defined interval. */
	
	public void setupFuzzySinglePowerOptimizedAlarm(Long milliseconds, Intent intentToBeBroadcast) {
		long nextTriggerTime = SystemClock.elapsedRealtime() + milliseconds + getRandomTimeOffset(); 
		PendingIntent pendingTimerIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTriggerTime, pendingTimerIntent);
	}
	
	/**Creates an alarm that will trigger within one half hour of the declared time, and then every hour thereafter
	 * but in a "nice" way so Android can cluster power usage events close together to save battery. */
	public void setupFuzzyPowerOptimizedRepeatingAlarm(long millisecondsRepeatInterval, Intent intentToBeBroadcast) {
		//this repeating alarm uses the RTC flag, it will not wake up the device, and it adds in a random interval up to one half hour 
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + getRandomTimeOffset(), millisecondsRepeatInterval, pendingIntent);
	}
	
	/** Single exact alarm for an event that happens once */
	public void setupExactSingleAlarm(Long milliseconds, Intent intentToBeBroadcast) {
		Long triggerTime = System.currentTimeMillis() + milliseconds;
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		setExactAlarm(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
	}
	
	
	/** Set a repeating, once-a-day alarm. Uses AlarmManager.setRepeating, which may not be precise
	 * @param hourOfDay in 24-hr time, when the alarm should fire. E.g., "19" means 7pm every day
	 * @param intentToBeBroadcast the intent to be broadcast when the alarm fires      */
	public void setupDailyRepeatingAlarm(int hourOfDay, Intent intentToBeBroadcast) {
		intentToBeBroadcast.putExtra("hour_of_day", hourOfDay);
		
		Calendar date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, hourOfDay);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		long triggerAtMillis = date.getTimeInMillis();
		//long triggerAtMillis = System.currentTimeMillis() - 5000L;  // For debugging only
		long timeTillFire = triggerAtMillis - System.currentTimeMillis();
		Log.d("Timer.java", "josh setupDailyRepeatingAlarm timeTillFire = " + timeTillFire + " milliseconds");

		if (alarmsAreExactInThisApiVersion()) {
			PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, ONE_DAY_IN_MILLISECONDS, pendingIntent); }
		else {
			// If we've already passed the trigger time for today, show the notification immediately
			if (triggerAtMillis < System.currentTimeMillis()) {
				appContext.sendBroadcast(intentToBeBroadcast); }
			// Otherwise, set an alarm to go off at the trigger time later today
			else {
				setupDailyAlarmForTomorrow(intentToBeBroadcast); } }
	}
	
	public void setupDailyAlarmForTomorrow(Intent intentToBeBroadcast) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		int hourOfDay = intentToBeBroadcast.getExtras().getInt("hour_of_day");
		
		Calendar date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, hourOfDay);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		long triggerAtMillis = date.getTimeInMillis();
		
		// If today's trigger time has already passed, set the alarm for tomorrow; otherwise leave it for today
		if (date.getTimeInMillis() < System.currentTimeMillis()) {
			triggerAtMillis += ONE_DAY_IN_MILLISECONDS; }
		long timeTillFire = triggerAtMillis - System.currentTimeMillis();
		Log.d("Timer.java", "josh DailyAlarm timeTillFire = " + timeTillFire + " milliseconds from now");
		
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
	}
	
	
	/** Set a repeating, once-a-week alarm. Uses AlarmManager.setRepeating, which may not be precise
	 * @param dayOfWeek Sunday = 1, Saturday = 7; or use Calendar.SUNDAY, Calendar.MONDAY, etc.
	 * @param hourOfDay in 24-hr time, when the alarm should fire. E.g., "19" means 7pm every day
	 * @param intentToBeBroadcast the intent to be broadcast when the alarm fires      */
	public void setupWeeklyRepeatingAlarm(int dayOfWeek, int hourOfDay, Intent intentToBeBroadcast) {
		intentToBeBroadcast.putExtra("day_of_week", dayOfWeek);
		intentToBeBroadcast.putExtra("hour_of_day", hourOfDay);

		if (alarmsAreExactInThisApiVersion()) {
			Calendar date = new GregorianCalendar();
			date.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			date.set(Calendar.HOUR_OF_DAY, hourOfDay);
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
			date.set(Calendar.MILLISECOND, 0);
			// The trigger date is in the past so that when you register, a weekly survey is immediately available
			long triggerAtMillis = date.getTimeInMillis() - ONE_WEEK_IN_MILLISECONDS;
			//long triggerAtMillis = System.currentTimeMillis() - 5000L;  // For debugging only
			long timeTillFire = triggerAtMillis - System.currentTimeMillis();
			Log.d("Timer.java", "josh setupWeeklyRepeatingAlarm timeTillFire = " + timeTillFire + " milliseconds");
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, ONE_WEEK_IN_MILLISECONDS, pendingIntent); }
		else {
			/* Send the broadcast right now, so that when you register, a weekly survey is
			 * immediately available (note: we don't do this for the daily survey) */
			appContext.sendBroadcast(intentToBeBroadcast); }
	}
	
	
	public void setupWeeklyAlarmForNextWeek(Intent intentToBeBroadcast) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		int dayOfWeek = intentToBeBroadcast.getExtras().getInt("day_of_week");
		int hourOfDay = intentToBeBroadcast.getExtras().getInt("hour_of_day");
		
		Calendar date = new GregorianCalendar();
		date.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		date.set(Calendar.HOUR_OF_DAY, hourOfDay);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		long triggerAtMillis = date.getTimeInMillis();
		
		// If this week's trigger time has already passed, set the alarm for next week; otherwise leave it for this week
		if (date.getTimeInMillis() < System.currentTimeMillis()) {
			triggerAtMillis += ONE_WEEK_IN_MILLISECONDS; }
		long timeTillFire = triggerAtMillis - System.currentTimeMillis();
		Log.d("Timer.java", "josh WeeklyAlarm timeTillFire = " + timeTillFire + " milliseconds from now");

		alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
	}
	
	
	/** setupExactTimeAlarm creates an Exact Alarm that will go off at a specific time within a
	 * period, e.g. every hour (period), at 47 minutes past the hour (start time within period).
	 * setupExactTimeAlarm is used for the Bluetooth timer, so that every device that has this app
	 * turns on its Bluetooth at the same moment. */
	public void setupExactTimeAlarm(long period, long startTimeInPeriod, Intent intentToBeBroadcast) {
		long currentTime = System.currentTimeMillis();
		// current unix time (mod) 3,600,000 milliseconds = the next hour-boundry, to which we add the EXACT_REPEAT_TIMER_OFFSET.
		Long nextTriggerTime = currentTime - ( currentTime % period ) + startTimeInPeriod;
		if (nextTriggerTime < currentTime) { nextTriggerTime += period; }
		PendingIntent pendingTimerIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		setExactAlarm(AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent);
	}
	
	/* ##################################################################################
	 * ############################ Other Utility Functions #############################
	 * ################################################################################*/
	
	/** In API 19 and above, alarms are inexact (to save power).  In API 18 and
	 *  below, alarms are exact.
	 *  This function checks the phone's operating system's API version and
	 *  returns TRUE if alarms are exact in this version (i.e., if it's API 18
	 *  or below), and returns FALSE if alarms are inexact.  */
	public static Boolean alarmsAreExactInThisApiVersion() {
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion < android.os.Build.VERSION_CODES.KITKAT) {
			return true; }
		else {
			return false; }
	}
	
	/** Calls AlarmManager.set() for API < 19, and AlarmManager.setExact() for API 19+
	 * For an exact alarm, it seems you need to use .set() for API 18 and below, and
	 * .setExact() for API 19 (KitKat) and above. */
	private void setExactAlarm(int type, long triggerAtMillis, PendingIntent operation) {
		if (alarmsAreExactInThisApiVersion()) {			
			alarmManager.set(type, triggerAtMillis, operation); }
		else {
			alarmManager.setExact(type, triggerAtMillis, operation); }
	}
	
	/**Cancels an alarm.
	 * @param intentToBeBroadcast an Intent identifying the alarm to cancel. */
	public void cancelAlarm(Intent intentToBeBroadcast) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		alarmManager.cancel(pendingIntent);
	}

	/**Checks if an alarm is set.
	 * @param intent an Intent identifying the alarm to check.
	 * @return Returns TRUE if there is an alarm set matching that intent; otherwise false. */
	public Boolean alarmIsSet(Intent intent) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent == null) { return false; }
		else { return true; }
	}
	
	/** Grabs an offset for use in the fuzzy alarms.
	 * Some alarms interact poorly if they all go off at once either on a single device, or across all devices
	 * running this software simultaneously.  A worst case would be if every device triggered simultaneous data uploads.
	 * @return a random int bounded by one half hour*/
	private static int getRandomTimeOffset(){
//		return new Random().nextInt((int) AlarmManager.INTERVAL_HALF_HOUR);
		//TODO: postproduction.  using a 10 second offset for debugging.
		return new Random().nextInt(10000);
	}
}