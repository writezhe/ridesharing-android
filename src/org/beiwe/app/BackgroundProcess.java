package org.beiwe.app;

import java.util.List;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WiFiListener;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.QuestionsDownloader;
import org.beiwe.app.survey.SurveyScheduler;
import org.beiwe.app.ui.AppNotifications;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class BackgroundProcess extends Service {
	
	private Context appContext;
	private LoginSessionManager sessionManager;
	
	// TODO: Eli. Make these private after killing DebugInterfaceActivity
	public GPSListener gpsListener;
	public AccelerometerListener accelerometerListener;
	public BluetoothListener bluetoothListener;
	public WiFiListener wifiListener;
	
	private Timer timer;
	
	//TODO: Eli. this [stupid hack] should only be necessary for debugging, comment out before production.
	public static BackgroundProcess BackgroundHandle = null;

	public static BackgroundProcess getBackgroundHandle() throws NullPointerException{
		if (BackgroundHandle != null) { return BackgroundHandle; }
		throw new NullPointerException("background process handle called for before background process had started.");
	}
	
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){		
		appContext = this.getApplicationContext();
		BackgroundHandle = this;
		TextFileManager.start(appContext);
		
		gpsListener = new GPSListener(appContext);
		accelerometerListener = new AccelerometerListener( appContext );
		startBluetooth();
		
//		bluetoothListener = new BluetoothListener( this.appContext );
//		bluetoothListener = new BluetoothListener();
		timer = new Timer(this);

		startSmsSentLogger();
		startCallLogger();
		startPowerStateListener();
		
		@SuppressWarnings("unused")  //the constructor hands DeviceInfo a Context, which it uses to grab info.
		DeviceInfo deviceInfo = new DeviceInfo(appContext);
		
//		Log.i("androidID", DeviceInfo.androidID);
//		Log.i("bluetoothMAC", DeviceInfo.bluetoothMAC);
		startTimers();
		wifiListener = new WiFiListener(appContext);
	}
	
	/*#############################################################################
	#########################         Starters              #######################
	#############################################################################*/
	
	/** Initializes the Bluetooth listener 
	 * Note: Bluetooth needs several checks to make sure that it actually exists,
	 * checking for Bluetooth LE is unlikely strictly necessary, but it should be done anyway. */
	public void startBluetooth(){
		if ( appContext.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) ) {
			this.bluetoothListener = new BluetoothListener(); }
		else { this.bluetoothListener = null; } 
	}
	
	/** Initializes the sms logger. */
	public void startSmsSentLogger() {
		SmsSentLogger smsSentLogger = new SmsSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsSentLogger);	}
	
	/** Initializes the call logger. */
	private void startCallLogger() {
		CallLogger callLogger = new CallLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls/"), true, callLogger);	}
	
	/** Initializes the PowerStateListener. 
	 * The PowerStateListener required the ACTION_SCREEN_OFF and ACTION_SCREEN_ON intents
	 * be registered programatically.  They do not work if registered in the app's manifest. */
	private void startPowerStateListener() {
		IntentFilter filter = new IntentFilter(); 
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver( (BroadcastReceiver) new PowerStateListener(), filter);
		PowerStateListener.start();
	}
		
	/*#############################################################################
	####################       Externally Accessed Functions       ################
	#############################################################################*/
	
	
	/** Checks whether the foreground app is Beiwe
	 * @param myPackage
	 * @return */
	/*
	 * Some things I found while researching this but never really got around to write:
	 * http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
	 * http://stackoverflow.com/questions/2166961/determining-the-current-foreground-application-from-a-background-task-or-service
	 * 
	 * Basically this can be checked using an AsyncTask.
	 * 
	 */
	// TODO: Eli/Josh deprecate this function? every activity calls finish() when it ends, so onPause() should only get called when the app is paused/no longer foreground.
	public boolean isForeground(String myPackage){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List < ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 
		
		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if(componentInfo.getPackageName().equals(myPackage)) return true;
		return false;
	}
	
	/*#############################################################################
	####################            Timer Logic             #######################
	#############################################################################*/
	
	/** create timers that will trigger events throughout the program, and
	 * register the custom Intents with the controlMessageReceiver. */
	public void startTimers() {
//		IntentFilter filter = new IntentFilter();
//		
//		filter.addAction( appContext.getString( R.string.accelerometer_off ) );
//		filter.addAction( appContext.getString( R.string.accelerometer_on ) );
//		filter.addAction( appContext.getString( R.string.action_accelerometer_timer ) );
//		filter.addAction( appContext.getString( R.string.action_bluetooth_timer ) );
//		filter.addAction( appContext.getString( R.string.action_gps_timer ) );
//		filter.addAction( appContext.getString( R.string.action_signout_timer ) );
//		filter.addAction( appContext.getString( R.string.action_wifi_log ) );
//		filter.addAction( appContext.getString( R.string.bluetooth_off ) );
//		filter.addAction( appContext.getString( R.string.bluetooth_on ) );
//		filter.addAction( appContext.getString( R.string.daily_survey ) );
//		filter.addAction( appContext.getString( R.string.gps_off ) );
//		filter.addAction( appContext.getString( R.string.gps_on ) );
//		filter.addAction( appContext.getString( R.string.signout_intent ) );
//		filter.addAction( appContext.getString( R.string.voice_recording ) );
//		filter.addAction( appContext.getString( R.string.weekly_survey ) );
//		
//		timer.setupExactHourlyAlarm(Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
//		timer.setupSingularExactAlarm( 5000L, Timer.signOutTimerIntent, Timer.signoutIntent); // Automatic Signout, also this line is simply incorrect
//		registerReceiver(controlMessageReceiver, filter);
//	
//		//TODO: Josh, create timer for checking for a new survey. (I think you have done this)  
//		
//		timer.setupExactHourlyAlarm( Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
//		timer.setupSingularExactAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent);
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent);
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiLogTimerIntent, Timer.wifiLogIntent);				
//		// Start voice recording alarm
//		timer.setupDailyRepeatingAlarm(19, new Intent(appContext.getString(R.string.voice_recording)));

		// TODO: Josh delete these; they're only for debugging while downloading from the server is broken
		QuestionsDownloader downloader = new QuestionsDownloader(appContext);
		downloader.downloadJsonQuestions();
		SurveyScheduler scheduler = new SurveyScheduler(appContext);
		scheduler.scheduleSurvey("");
		scheduler.scheduleSurvey("{weekly_or_daily: 'weekly'}");
	}	
	
	
	BroadcastReceiver controlMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context appContext, Intent intent) {
			Log.i("BackgroundService - timers", "Received Broadcast: " + intent.toString());
			
			if (intent.getAction().equals( appContext.getString(R.string.accelerometer_off) ) ) {
				accelerometerListener.turn_off();
				timer.setupSingularExactAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent); }
			
			if (intent.getAction().equals( appContext.getString(R.string.accelerometer_on) ) ) {
				accelerometerListener.turn_on();
				timer.setupSingularFuzzyAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOffIntent); }
			
			if (intent.getAction().equals( appContext.getString(R.string.bluetooth_off) ) ) {
				bluetoothListener.disableBLEScan();
				timer.setupExactHourlyAlarm( Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent); }
			
			if (intent.getAction().equals( appContext.getString(R.string.bluetooth_on) ) ) {
				bluetoothListener.enableBLEScan(); 
				timer.setupSingularExactAlarm( 5000L, Timer.bluetoothTimerIntent, Timer.bluetoothOffIntent ); }
			
			if (intent.getAction().equals( appContext.getString(R.string.gps_off) ) ) {
				gpsListener.turn_off();
				timer.setupSingularFuzzyAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent); }
			
			if (intent.getAction().equals( appContext.getString(R.string.gps_on) ) ) {
				gpsListener.turn_on();
				timer.setupSingularExactAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOffIntent); }
		
			if (intent.getAction().equals( appContext.getString(R.string.action_wifi_log) ) ) {
				wifiListener.scanWifi();
				timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiLogTimerIntent, Timer.wifiLogIntent); }
			
			if (intent.getAction().equals( appContext.getString(R.string.voice_recording) ) ) {
				Log.i("TIMERS", "voice recording timer sounded");
				AppNotifications.displayRecordingNotification(appContext); }
			
			if (intent.getAction().equals( appContext.getString(R.string.daily_survey) ) ) {
				Log.i("TIMERS", "DAILY SURVEY CALLED");
				AppNotifications.displaySurveyNotification(appContext); }
			
			if (intent.getAction().equals( appContext.getString(R.string.weekly_survey) ) ) {
				// TODO: Josh, differentiate between daily and weekly surveys
				Log.i("TIMERS", "WEEKLY SURVEY CALLED");
				AppNotifications.displaySurveyNotification(appContext); }
			
			// TODO: Eli. formerly dori. Find out when this needs to go off. Provide a function inside the logic that is "start logout timer"
			// What needs to be done is to send the activity to the background process in case it is no longer used (onPause, onStop, etc...) 
			// and then start the timer.. There has to be a simpler solution - will write it down as soon as I figuer it out.
			if (intent.getAction().equals(appContext.getString(R.string.signout_intent) ) ) {
				Log.i("BackgroundProcess", "Received Logout");
				
				// TODO: Eli. Add to all activities in either onCreate(), onDestroy() and/or onPause() to reset this loginSession timer.
				// (onDestroy() is called whenever finish() is called), which is whenever a user leaves to go to another activity within the app.
				// onPause() is called whenever a user leaves the app, but does not kill it. We should not use onDestroy(), because a user can
				// also kill the app from the task manager. Therefore the timer call should probably be called onCreate() using a static function
				// (handle signOutTimer() ).
				// If onPause is called before onDestroy in the taskManager, we could put this in onPause or onDestroy.
				// So, therefore the best solution is to have it in onPause and onDestroy.
				// refinement:
				//		in onPause and onDestroy call a function that...
				//		...signs the user in (because... that is exactly the behavior we want), starts a fifteen minute logout timer.
				// is t
				if( isForeground("org.beiwe.app") ) {
					LoginSessionManager.logoutUser(); }
				else { LoginSessionManager.logoutUserPassive(); }
			}
		}
	};
	
	/*##########################################################################################
	################# onStartCommand, onBind, and onDesroy (ignore these)# #####################
	##########################################################################################*/
	
	@Override
	/** The BackgroundService is meant to be all the time, so we return START_STICKY */
	public int onStartCommand(Intent intent, int flags, int startId){ return START_STICKY; }
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	@Override
	public void onDestroy() {
		//this does not appear to run when the service or app are killed...
		Log.i("BackgroundService", "BackgroundService Killed");
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + "," + "BackgroundService Killed" +"\n" ); }
}