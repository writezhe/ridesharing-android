package org.beiwe.app;

import java.util.Calendar;
import java.util.List;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.MMSSentLogger;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.networking.SurveyDownloader;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.SurveyScheduler;
import org.beiwe.app.ui.user.LoginActivity;
import org.beiwe.app.ui.utils.SurveyNotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class BackgroundService extends Service {
	private Context appContext;
	// TODO: postproduction. Make these private after killing DebugInterfaceActivity
	public GPSListener gpsListener;
	public AccelerometerListener accelerometerListener;
	public BluetoothListener bluetoothListener;
	private static Timer timer;
	
	
	//localHandle is how static functions access the currently instantiated background service.
	//It is to be used ONLY to register new surveys with the running background service, because
	//that code needs to be able to update the IntentFilters associated with timerReceiver.
	//This is Really Hacky and terrible style, but it is okay because the scheduling code can only ever
	//begin to run with an already fully instantiated background service.
	private static BackgroundService localHandle;
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate() {
		appContext = this.getApplicationContext();
		
		DeviceInfo.initialize( getApplicationContext() );
		PersistentData.initialize( getApplicationContext() );
		TextFileManager.initialize( getApplicationContext() );
		PostRequest.initialize( getApplicationContext() );
		WifiListener.initialize( getApplicationContext() );
		
		gpsListener = new GPSListener(appContext);
		accelerometerListener = new AccelerometerListener( appContext );
		startBluetooth();
		startSmsSentLogger();
		startMmsSentLogger();
		startCallLogger();
		startPowerStateListener();
		localHandle = this;  //yes yes I know.
		registerTimers(appContext);
		DeviceInfo.getPhoneNumber();
		//If this device is registered, start timers!
		if (PersistentData.isRegistered()) { startTimers(); }
	}

	
	/*##############################################################################
	########################## Android Service Lifecycle ###########################
	##############################################################################*/
	
	@Override
	/** The BackgroundService is meant to be all the time, so we return START_STICKY */
	// We could also use, and may change it if we encounter problems, START_REDELIVER_INTENT, which has nearly identical behavior.
	public int onStartCommand(Intent intent, int flags, int startId){ //Log.d("BackroundService onStartCommand", "started with flag " + flags );
		TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"started with flag " + flags);
		return START_STICKY; }
	//(the rest of these are identical, so I have compactified it)
	@Override public void onTaskRemoved(Intent rootIntent) { //Log.d("BackroundService onTaskRemoved", "onTaskRemoved called with intent: " + rootIntent.toString() );
		TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"onTaskRemoved called with intent: " + rootIntent.toString());
		restartService(); }
	@Override public boolean onUnbind(Intent intent) { //Log.d("BackroundService onUnbind", "onUnbind called with intent: " + intent.toString() );
		TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"onUnbind called with intent: " + intent.toString());
		restartService();
		return super.onUnbind(intent); }
	@Override public void onDestroy() { //Log.w("BackgroundService", "BackgroundService was destroyed.");
		//note: this does not run when the service is killed in a task manager, OR when the stopService() function is called from debugActivity.
		TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"BackgroundService was destroyed.");
		restartService();
		super.onDestroy(); }
	@Override public void onLowMemory() { //Log.w("BackroundService onLowMemory", "Low memory conditions encountered");
		TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"onLowMemory called.");
		restartService(); }
	
	/** Sets a timer that starts the service if it is not running in ten seconds. */
	private void restartService(){
		//how does this even...  Whatever, 10 seconds later the background service will start.
		Intent restartServiceIntent = new Intent( getApplicationContext(), this.getClass() );
	    restartServiceIntent.setPackage( getPackageName() );
	    // TODO: Research. Eli/Josh. We may want to change PendingIntent.FLAG_ONE_SHOT to FLAG_CANCEL_CURRENT, research the benefits, this might be a pain to test...
	    PendingIntent restartServicePendingIntent = PendingIntent.getService( getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT );
	    AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService( Context.ALARM_SERVICE );
	    alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
	}
	
	/** Stops the BackgroundService instance. */
	//TODO: Low priority.  This is not used anywhere.
	public void stop() { this.stopSelf(); }
	
	/*#############################################################################
	#########################         Starters              #######################
	#############################################################################*/
	
	/** Initializes the Bluetooth listener 
	 * Note: Bluetooth has several checks to make sure that it actually exists on the device with the capabilities we need.
	 * Checking for Bluetooth LE is necessary because it is an optional extension to Bluetooth 4.0. */
	public void startBluetooth(){
		//Note: the Bluetooth listener is a BroadcastReceiver, which means it must have a 0-argument constructor in order for android can instantiate it on broadcast receipts.
		//The following check must be made, but it requires a Context that we cannot pass into the BluetoothListener, so we do the check in the BackgroundService.
		if ( appContext.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) && PersistentData.getBluetoothEnabled() ) {
			this.bluetoothListener = new BluetoothListener();
			if ( this.bluetoothListener.isBluetoothEnabled() ) {
				Log.i("Background Service", "success, actually doing bluetooth things.");
				registerReceiver(this.bluetoothListener, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED") ); }
			else {
				//TODO: Low priority. Eli. Track down why this error log pops up, cleanup.  -- the above check should be for the (new) doesBluetoothCapabilityExist function instead of isBluetoothEnabled
				Log.e("Background Service", "bluetooth Failure. Should not have gotten this far.");
				TextFileManager.getDebugLogFile().writeEncrypted("bluetooth Failure, device should not have gotten to this line of code"); }
		}
		else {
			if (PersistentData.getBluetoothEnabled()) {
				TextFileManager.getDebugLogFile().writeEncrypted("Device does not support bluetooth LE, bluetooth features disabled.");
				Log.w("BackgroundService bluetooth init", "Device does not support bluetooth LE, bluetooth features disabled."); }
			else { Log.d("BackgroundService bluetooth init", "Bluetooth not enabled for study."); }
			this.bluetoothListener = null; }
	}
	
	/** Initializes the sms logger. */
	public void startSmsSentLogger() {
		SmsSentLogger smsSentLogger = new SmsSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsSentLogger); }
	
	public void startMmsSentLogger(){
		MMSSentLogger mmsMonitor = new MMSSentLogger(new Handler(), appContext);
		//this is retarded, it needs to be mms-sms here, and just "mms" in the mms code
		this.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/conversation?simple=true"), true, mmsMonitor); }
	
	/** Initializes the call logger. */
	private void startCallLogger() {
		CallLogger callLogger = new CallLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls/"), true, callLogger); }
	
	/** Initializes the PowerStateListener. 
	 * The PowerStateListener required the ACTION_SCREEN_OFF and ACTION_SCREEN_ON intents
	 * be registered programmatically.  They do not work if registered in the app's manifest. */
	private void startPowerStateListener() {
		IntentFilter filter = new IntentFilter(); 
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver( (BroadcastReceiver) new PowerStateListener(), filter);
		PowerStateListener.start();
	}
	
	
	/** create timers that will trigger events throughout the program, and
	 * register the custom Intents with the controlMessageReceiver. */
	@SuppressWarnings("static-access")
	public static void registerTimers(Context appContext) {
		localHandle.timer = new Timer(localHandle);
		IntentFilter filter = new IntentFilter();
		filter.addAction( appContext.getString( R.string.accelerometer_off ) );
		filter.addAction( appContext.getString( R.string.accelerometer_on ) );
		filter.addAction( appContext.getString( R.string.bluetooth_off ) );
		filter.addAction( appContext.getString( R.string.bluetooth_on ) );
		filter.addAction( appContext.getString( R.string.gps_off ) );
		filter.addAction( appContext.getString( R.string.gps_on ) );
		filter.addAction( appContext.getString( R.string.signout_intent ) );
		filter.addAction( appContext.getString( R.string.voice_recording ) );
		filter.addAction( appContext.getString( R.string.daily_survey ) );
		filter.addAction( appContext.getString( R.string.weekly_survey ) );
		filter.addAction( appContext.getString( R.string.run_wifi_log ) );
		filter.addAction( appContext.getString( R.string.upload_data_files_intent ) );
		filter.addAction( appContext.getString( R.string.create_new_data_files_intent ) );
		filter.addAction( appContext.getString( R.string.check_for_new_surveys_intent ) );
		List<String> surveyIds = PersistentData.getSurveyIds();
		for (String surveyId : surveyIds) { filter.addAction(surveyId); }
		appContext.registerReceiver(localHandle.timerReceiver, filter);
	}
	
	/*#############################################################################
	####################            Timer Logic             #######################
	#############################################################################*/
	
	public void startTimers() {
		// Sensor timers.
		if (PersistentData.getAccelerometerEnabled() && !timer.alarmIsSet(Timer.accelerometerOnIntent) && !timer.alarmIsSet(Timer.accelerometerOffIntent)) {
			timer.setupExactSingleAlarm( PersistentData.getAccelerometerOffDurationMilliseconds(), Timer.accelerometerOnIntent); }
		if (PersistentData.getGpsEnabled() && !timer.alarmIsSet(Timer.gpsOnIntent) && !timer.alarmIsSet(Timer.gpsOffIntent)) {
			timer.setupExactSingleAlarm( PersistentData.getGpsOffDurationMilliseconds(), Timer.gpsOnIntent); }
		if (PersistentData.getBluetoothEnabled() && !timer.alarmIsSet(Timer.bluetoothOnIntent) && !timer.alarmIsSet(Timer.bluetoothOffIntent)) {
			timer.setupExactSingleAbsoluteTimeAlarm(PersistentData.getBluetoothTotalDurationMilliseconds(), PersistentData.getBluetoothGlobalOffsetMilliseconds(), Timer.bluetoothOnIntent); }
		if (PersistentData.getWifiEnabled() && !timer.alarmIsSet(Timer.wifiLogIntent)) {
			timer.setupExactSingleAlarm(PersistentData.getWifiLogFrequencyMilliseconds(), Timer.wifiLogIntent); }
		
		// Functionality timers.
		if (!timer.alarmIsSet(Timer.uploadDatafilesIntent)) {
			timer.setupExactSingleAlarm(PersistentData.getUploadDataFilesFrequencyMilliseconds(), Timer.uploadDatafilesIntent); }
		if (!timer.alarmIsSet(Timer.createNewDataFilesIntent)) {
			timer.setupExactSingleAlarm(PersistentData.getCreateNewDataFilesFrequencyMilliseconds(), Timer.createNewDataFilesIntent); }
		if (!timer.alarmIsSet(Timer.checkForNewSurveysIntent)) {
			timer.setupExactSingleAlarm(PersistentData.getCheckForNewSurveysFrequencyMilliseconds(), Timer.checkForNewSurveysIntent); }

		//checks for the current expected state with app notifications. (must be run before we potentially set new alarms)
		Long now = System.currentTimeMillis();
		for (String surveyId : PersistentData.getSurveyIds() ){
			if ( PersistentData.getSurveyNotificationState(surveyId) || PersistentData.getMostRecentSurveyAlarmTime(surveyId) < now ) {
				SurveyNotifications.displaySurveyNotification(appContext, surveyId); }
		}
		
		for (String surveyId : PersistentData.getSurveyIds() ) { //check each survey to ensure it is scheduled.
			if ( !timer.alarmIsSet( new Intent(surveyId) ) ) { SurveyScheduler.scheduleSurvey(surveyId); } }
	}
	
	/**Refreshes the logout timer.
	 * This function has a THEORETICAL race condition, where the BackgroundService is not fully instantiated by a session activity,
	 * in this case we log an error to the debug log, print the error, and then wait for it to crash.  In testing on a (much) older
	 * version of the app we would occasionally see the error message, but we have never (august 10 2015) actually seen the app crash
	 * inside this code. */
	public static void startAutomaticLogoutCountdownTimer(){
		if (timer == null) {
			Log.e("bacgroundService", "timer is null, BackgroundService may be about to crash, the Timer was null when the BackgroundService was supposed to be fully instantiated.");
			TextFileManager.getDebugLogFile().writeEncrypted("our not-quite-race-condition encountered, Timer was null when the BackgroundService was supposed to be fully instantiated");
		}
		timer.setupExactSingleAlarm(PersistentData.getMillisecondsBeforeAutoLogout(), Timer.signoutIntent);
		PersistentData.loginOrRefreshLogin();
	}
	
	/** cancels the signout timer */
	public static void clearAutomaticLogoutCountdownTimer() { timer.cancelAlarm(Timer.signoutIntent); }
	
	/** The Timer requires the BackgroundService in order to create alarms, hook into that functionality here. */
	public static void setSurveyAlarm(String surveyId, Calendar alarmTime) { timer.startSurveyAlarm(surveyId, alarmTime); }
	
	
	/**The timerReceiver is an Android BroadcastReceiver that listens for our timer events to trigger,
	 * and then runs the appropriate code for that trigger. */
	//programmer note: this variable is instantiated here AT CONSTRUCTION, BEFORE onCreate runs.
	private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context appContext, Intent intent) {
			Log.d("BackgroundService - timers", "Received broadcast: " + intent.toString() );
			TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis() + " Received Broadcast: " + intent.toString() );
			String broadcastAction = intent.getAction();
			
			//sets the next trigger time for the accelerometer to record data 
			if (broadcastAction.equals( appContext.getString(R.string.accelerometer_off) ) ) {
				accelerometerListener.turn_off();
				timer.setupExactSingleAlarm(PersistentData.getAccelerometerOffDurationMilliseconds(), Timer.accelerometerOnIntent);
				return; }
			
			//sets a timer that will turn off the accelerometer
			if (broadcastAction.equals( appContext.getString(R.string.accelerometer_on) ) ) {
				if ( !PersistentData.getAccelerometerEnabled() ) { Log.e("BackgroundService Listener", "invalid Accelerometer on received"); return; }
				accelerometerListener.turn_on();
				timer.setupExactSingleAlarm(PersistentData.getAccelerometerOnDurationMilliseconds(), Timer.accelerometerOffIntent);
				return; }
			
			//sets the next trigger time for the bluetooth scan to record data
			if (broadcastAction.equals( appContext.getString(R.string.bluetooth_off) ) ) {
				if ( bluetoothListener != null) bluetoothListener.disableBLEScan();
				timer.setupExactSingleAbsoluteTimeAlarm(PersistentData.getBluetoothTotalDurationMilliseconds(), PersistentData.getBluetoothGlobalOffsetMilliseconds(), Timer.bluetoothOnIntent);
				return; }
			
			//sets a timer that will turn off the bluetooth scan
			if (broadcastAction.equals( appContext.getString(R.string.bluetooth_on) ) ) {
				if ( !PersistentData.getBluetoothEnabled() ) { Log.e("BackgroundService Listener", "invalid Bluetooth on received"); return; }
				if (bluetoothListener != null) bluetoothListener.enableBLEScan();
				timer.setupExactSingleAlarm(PersistentData.getBluetoothOnDurationMilliseconds(), Timer.bluetoothOffIntent);
				return; }
			
			//sets the next trigger time for the gps to record data
			if (broadcastAction.equals( appContext.getString(R.string.gps_off) ) ) {
				gpsListener.turn_off();
				timer.setupExactSingleAlarm(PersistentData.getGpsOffDurationMilliseconds(), Timer.gpsOnIntent);
				return; }
			
			//sets a timer that will turn off the gps
			if (broadcastAction.equals( appContext.getString(R.string.gps_on) ) ) {
				if ( !PersistentData.getGpsEnabled() ) { Log.e("BackgroundService Listener", "invalid GPS on received"); return; }
				gpsListener.turn_on();
				timer.setupExactSingleAlarm(PersistentData.getGpsOnDurationMilliseconds(), Timer.gpsOffIntent);
				return; }
			
			//runs a wifi scan
			if (broadcastAction.equals( appContext.getString(R.string.run_wifi_log) ) ) {
				if ( !PersistentData.getWifiEnabled() ) { Log.e("BackgroundService Listener", "invalid WiFi scan received"); return; }
				WifiListener.scanWifi();
				timer.setupExactSingleAlarm(PersistentData.getWifiLogFrequencyMilliseconds(), Timer.wifiLogIntent);
				return; }
						
			//runs the user signout logic, bumping the user to the login screen.
			if (broadcastAction.equals( appContext.getString(R.string.signout_intent) ) ) {
				PersistentData.logout();
				Intent loginPage = new Intent(appContext, LoginActivity.class);
				loginPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appContext.startActivity(loginPage);
				return; }
			
			//starts a data upload attempt.
			if (broadcastAction.equals( appContext.getString(R.string.upload_data_files_intent) ) ) {
				PostRequest.uploadAllFiles();
				timer.setupExactSingleAlarm(PersistentData.getUploadDataFilesFrequencyMilliseconds(), Timer.uploadDatafilesIntent);
				return; }

			//creates new data files
			if (broadcastAction.equals( appContext.getString(R.string.create_new_data_files_intent) ) ) {
				TextFileManager.makeNewFilesForEverything();
				timer.setupExactSingleAlarm(PersistentData.getCreateNewDataFilesFrequencyMilliseconds(), Timer.createNewDataFilesIntent);
				return; }

			//Downloads the most recent survey questions and schedules the surveys.
			if (broadcastAction.equals( appContext.getString(R.string.check_for_new_surveys_intent))) {
				SurveyDownloader.downloadSurveys( getApplicationContext() );
				timer.setupExactSingleAlarm(PersistentData.getCheckForNewSurveysFrequencyMilliseconds(), Timer.checkForNewSurveysIntent);
				return; }
			
			//checks if the action is the id of a survey, if so pop up the notification for that survey, schedule the next alarm
			if ( PersistentData.getSurveyIds().contains( broadcastAction ) ) {
				Log.w("BACKGROUND SERVICE", "trying to start notification: " + broadcastAction);
				SurveyNotifications.displaySurveyNotification(appContext, broadcastAction);
				SurveyScheduler.scheduleSurvey(broadcastAction);
				return; }
		}
	};
	
	//misclanie
	public void crashBackgroundService() { throw new NullPointerException("stop poking me!"); }
	
	/*##########################################################################################
	############## code related to onStartCommand and binding to an activity ###################
	##########################################################################################*/
	@Override
	public IBinder onBind(Intent arg0) { return new BackgroundServiceBinder(); }
	
	/**A public "Binder" class for Activities to access.
	 * Provides a (safe) handle to the background Service using the onStartCommand code
	 * used in every RunningBackgroundServiceActivity */
	public class BackgroundServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}