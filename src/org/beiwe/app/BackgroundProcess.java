package org.beiwe.app;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.MMSSentLogger;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.QuestionsDownloader;
import org.beiwe.app.survey.SurveyType.Type;
import org.beiwe.app.ui.AppNotifications;
import org.beiwe.app.ui.LoginActivity;

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


public class BackgroundProcess extends Service {
	
	private Context appContext;
	
	// TODO: postproduction. Make these private after killing DebugInterfaceActivity
	public GPSListener gpsListener;
	public AccelerometerListener accelerometerListener;
	public BluetoothListener bluetoothListener;
	
	private static Timer timer;
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){
//		Log.d("backgroundprocess", "Backgroundprocess Created");
		appContext = this.getApplicationContext();
		
		DeviceInfo.initialize( getApplicationContext() );
		LoginManager.initialize( getApplicationContext() );
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
		registerTimers();
		//If this device is both registered and timers have not already been started, start them!
		if (LoginManager.isRegistered()) { startTimers(); }
	}

	
	/*##############################################################################
	########################## Android Service Lifecycle ###########################
	##############################################################################*/
	
	@Override
	/** The BackgroundService is meant to be all the time, so we return START_STICKY */
	// We could also use, and may change it if we encounter problems, START_REDELIVER_INTENT, which has nearly identical behavior.
	public int onStartCommand(Intent intent, int flags, int startId){
//		Log.d("BackroundProcess onStartCommand", "started with flag " + flags );
		TextFileManager.getDebugLogFile().writePlaintext(System.currentTimeMillis()+" "+"started with flag " + flags);
		return START_STICKY;
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
//		Log.d("BackroundProcess onTaskRemoved", "onTaskRemoved called with intent: " + rootIntent.toString() );
		TextFileManager.getDebugLogFile().writePlaintext(System.currentTimeMillis()+" "+"onTaskRemoved called with intent: " + rootIntent.toString());
		restartService();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
//		Log.d("BackroundProcess onUnbind", "onUnbind called with intent: " + intent.toString() );
		TextFileManager.getDebugLogFile().writePlaintext(System.currentTimeMillis()+" "+"onUnbind called with intent: " + intent.toString());
		restartService();
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		//this does not run when the service is killed in a task manager, OR when the stopService() function is called from debugActivity.
//		Log.w("BackgroundProcess", "BACKGROUNDPROCESS WAS DESTROYED.");
		TextFileManager.getDebugLogFile().writePlaintext(System.currentTimeMillis()+" "+"BACKGROUNDPROCESS WAS DESTROYED.");
		restartService();
		super.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
//		Log.w("BackroundProcess onLowMemory", "Low memory conditions encountered");
		TextFileManager.getDebugLogFile().writePlaintext(System.currentTimeMillis()+" "+"onLowMemory called.");
		restartService();
	}
	
	/** Sets a timer that starts the service if it is not running in ten seconds. */
	private void restartService(){
		//how does this even...
		//whatever, 10 seconds later the background service will start.
		Intent restartServiceIntent = new Intent( getApplicationContext(), this.getClass() );
	    restartServiceIntent.setPackage( getPackageName() );
	    PendingIntent restartServicePendingIntent = PendingIntent.getService( getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT );
	    AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService( Context.ALARM_SERVICE );
	    alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10000, restartServicePendingIntent);
	}
	
	/*#############################################################################
	#########################         Starters              #######################
	#############################################################################*/
	
	/** Initializes the Bluetooth listener 
	 * Note: Bluetooth has several checks to make sure that it actually exists on the device with the capabilities we need.
	 * Checking for Bluetooth LE is necessary because it is an optional extension to Bluetooth 4.0. */
	public void startBluetooth(){
		// TODO Josh: log (to DeviceInfo or something) whether the device supports Bluetooth LE
		//  Josh: I have inserted into the bluetooth listener an additional check for this device feature.
		//  we may be able to remove the logging.
		if ( appContext.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) ) {
			Log.d("BackgroundProcess.java", "This device supports Bluetooth LE; the app will log which other devices are in Bluetooth range.");
			this.bluetoothListener = new BluetoothListener( appContext );
		}
		else {
			// TODO Josh: show an alert saying "this device does not support Bluetooth LE; it won't be able to blahblahblah
			//  Josh: this could be a big, fat, rabbit hole to display a message from the background process.
			//  I think we should make the button in debug display a message, users don't care about whether the bluetooth feature works
			Log.d("BackgroundProcess.java", "This device does not support Bluetooth LE; the app will not log which other devices are in Bluetooth range.");
			this.bluetoothListener = null;
		} 
	}
	
	/** Initializes the sms logger. */
	public void startSmsSentLogger() {
		SmsSentLogger smsSentLogger = new SmsSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsSentLogger); }
	
	public void startMmsSentLogger(){
		MMSSentLogger mmsMonitor = new MMSSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms"), true, mmsMonitor); }
	
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
	private void registerTimers() {
		timer = new Timer(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction( appContext.getString( R.string.accelerometer_off ) );
		filter.addAction( appContext.getString( R.string.accelerometer_on ) );
		filter.addAction( appContext.getString( R.string.action_accelerometer_timer ) );
		filter.addAction( appContext.getString( R.string.action_bluetooth_timer ) );
		filter.addAction( appContext.getString( R.string.action_gps_timer ) );
		filter.addAction( appContext.getString( R.string.action_wifi_log ) );
		filter.addAction( appContext.getString( R.string.bluetooth_off ) );
		filter.addAction( appContext.getString( R.string.bluetooth_on ) );
		filter.addAction( appContext.getString( R.string.daily_survey ) );
		filter.addAction( appContext.getString( R.string.gps_off ) );
		filter.addAction( appContext.getString( R.string.gps_on ) );
		filter.addAction( appContext.getString( R.string.signout_intent ) );
		filter.addAction( appContext.getString( R.string.voice_recording ) );
		filter.addAction( appContext.getString( R.string.weekly_survey ) );
		filter.addAction( appContext.getString( R.string.upload_data_files_intent ) );
		filter.addAction( appContext.getString( R.string.check_for_new_surveys_intent ) );
		registerReceiver(timerReceiver, filter);
	}
	
	/*#############################################################################
	####################            Timer Logic             #######################
	#############################################################################*/
	
	public void startTimers() {
//		Log.i("BackgroundProcess", "starting timers");
		//TODO: Postproduction: set timer intervals to their real values. (hourly? I think?)
		if (!timer.alarmIsSet(Timer.accelerometerTimerIntent)) {
			timer.setupExactDoubleAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent); }
		if (!timer.alarmIsSet(Timer.GPSTimerIntent)) {
			timer.setupFuzzySinglePowerOptimizedAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent); }
		if (!timer.alarmIsSet(Timer.bluetoothTimerIntent)) {
			timer.setupExactTimeAlarm(Timer.BLUETOOTH_PERIOD, Timer.BLUETOOTH_START_TIME_IN_PERIOD, Timer.bluetoothOnIntent); }
		if (!timer.alarmIsSet(Timer.wifiLogIntent)) {
			timer.setupFuzzyPowerOptimizedRepeatingAlarm(Timer.WIFI_LOG_PERIOD, Timer.wifiLogIntent); }
		if (!timer.alarmIsSet(Timer.voiceRecordingIntent)) {
			timer.setupDailyRepeatingAlarm(Timer.VOICE_RECORDING_HOUR_OF_DAY, Timer.voiceRecordingIntent); }
		if (!timer.alarmIsSet(Timer.uploadDatafilesIntent)) {
			timer.setupFuzzyPowerOptimizedRepeatingAlarm(Timer.UPLOAD_DATA_FILES_PERIOD, Timer.uploadDatafilesIntent); }
		if (!timer.alarmIsSet(Timer.checkForNewSurveysIntent)) {
			timer.setupFuzzyPowerOptimizedRepeatingAlarm(Timer.CHECK_FOR_NEW_SURVEYS_PERIOD, Timer.checkForNewSurveysIntent); }
	}
	
	public static void startAutomaticLogoutCountdownTimer(){
		timer.setupExactSingleAlarm(LoginManager.millisecondsBeforeAutoLogout, Timer.signoutIntent);
		LoginManager.loginOrRefreshLogin();
	}

	public static void clearAutomaticLogoutCountdownTimer() { timer.cancelAlarm(Timer.signoutIntent); }
	
	public static void setDailySurvey(int hour) { timer.setupDailyRepeatingAlarm(hour, Timer.dailySurveyIntent); }
	
	public static void setWeeklySurvey(int hour, int dayOfWeek) { timer.setupWeeklyRepeatingAlarm(dayOfWeek, hour, Timer.weeklySurveyIntent); }
	
	
	/**The timerReceiver is an Android BroadcastReceiver that listens for our timer events to trigger,
	 * and then runs the appropriate code for that trigger. */
	private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context appContext, Intent intent) {
			Log.d("BackgroundService - timers", "Received Broadcast: " + intent.toString() );
			TextFileManager.getDebugLogFile().writePlaintext(System.currentTimeMillis() + " Received Broadcast: " + intent.toString() );
			
			//sets the next trigger time for the accelerometer to record data 
			if (intent.getAction().equals( appContext.getString(R.string.accelerometer_off) ) ) {
				accelerometerListener.turn_off();
				timer.setupExactDoubleAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent); }
			
			//sets a timer that will turn off the accelerometer
			if (intent.getAction().equals( appContext.getString(R.string.accelerometer_on) ) ) {
				accelerometerListener.turn_on();
				timer.setupFuzzySinglePowerOptimizedAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOffIntent); }
			
			//sets the next trigger time for the bluetooth scan to record data
			if (intent.getAction().equals( appContext.getString(R.string.bluetooth_off) ) ) {
				if (bluetoothListener != null) bluetoothListener.disableBLEScan();
				timer.setupExactTimeAlarm(Timer.BLUETOOTH_PERIOD, Timer.BLUETOOTH_START_TIME_IN_PERIOD, Timer.bluetoothOnIntent); }
			
			//sets a timer that will turn off the bluetooth scan
			if (intent.getAction().equals( appContext.getString(R.string.bluetooth_on) ) ) {
				if (bluetoothListener != null) bluetoothListener.enableBLEScan();
				timer.setupExactSingleAlarm(Timer.BLUETOOTH_ON_DURATION, Timer.bluetoothOffIntent); }
			
			//sets the next trigger time for the gps to record data
			if (intent.getAction().equals( appContext.getString(R.string.gps_off) ) ) {
				gpsListener.turn_off();
				timer.setupFuzzySinglePowerOptimizedAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent); }
			
			//sets a timer that will turn off the gps
			if (intent.getAction().equals( appContext.getString(R.string.gps_on) ) ) {
				gpsListener.turn_on();
				timer.setupExactDoubleAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOffIntent); }
			
			//runs a wifi scan
			if (intent.getAction().equals( appContext.getString(R.string.action_wifi_log) ) ) {
				WifiListener.scanWifi(); }
			
			//registers a notification for the user to make an audio recording.
			if (intent.getAction().equals( appContext.getString(R.string.voice_recording) ) ) {
				AppNotifications.displayRecordingNotification(appContext); }
			
			//registers a notification for the user to take the daily survey.
			if (intent.getAction().equals( appContext.getString(R.string.daily_survey) ) ) {
				AppNotifications.displaySurveyNotification(appContext, Type.DAILY); }
			
			//registers a notification for the user to take the weekly survey.
			if (intent.getAction().equals( appContext.getString(R.string.weekly_survey) ) ) {
				AppNotifications.displaySurveyNotification(appContext, Type.WEEKLY); }
			
			//runs the user signout logic, bumping the user to the login screen.
			if (intent.getAction().equals( appContext.getString(R.string.signout_intent) ) ) {
				LoginManager.logout();
				Intent loginPage = new Intent(appContext, LoginActivity.class);
				loginPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appContext.startActivity(loginPage); }
			
			//starts a data upload attempt.
			if (intent.getAction().equals( appContext.getString(R.string.upload_data_files_intent) ) ) {
				PostRequest.uploadAllFiles(); }

			//Downloads the most recent survey questions and schedules the surveys.
			if (intent.getAction().equals( appContext.getString(R.string.check_for_new_surveys_intent))) {
				QuestionsDownloader downloader = new QuestionsDownloader(appContext);
				downloader.downloadJsonQuestions(); }
		}
	};
	
	/*##########################################################################################
	############## code related to onStartCommand and binding to an activity ###################
	##########################################################################################*/
	@Override
	public IBinder onBind(Intent arg0) { return new BackgroundProcessBinder(); }
	
	/**A public "Binder" class for Activities to access.
	 * Provides a (safe) handle to the background process using the onStartCommand code
	 * used in every RunningBackgroundProcessActivity */
	public class BackgroundProcessBinder extends Binder {
        public BackgroundProcess getService() {
            return BackgroundProcess.this;
        }
    }
}