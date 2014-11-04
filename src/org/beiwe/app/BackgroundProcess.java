package org.beiwe.app;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.QuestionsDownloader;
import org.beiwe.app.survey.SurveyType.Type;
import org.beiwe.app.ui.AppNotifications;

import android.app.Service;
import android.content.BroadcastReceiver;
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
	
	// TODO: postproduction. Make these private after killing DebugInterfaceActivity
	public GPSListener gpsListener;
	public AccelerometerListener accelerometerListener;
	public BluetoothListener bluetoothListener;
	
	private static Timer timer;
	
	//TODO: Eli. this [stupid hack] should only be necessary for debugging, comment out before production?
	private static BackgroundProcess BackgroundHandle = null;

	//returns the backgroundHandle
	//always check to see if null before using.
	public static BackgroundProcess getBackgroundHandle() throws NullPointerException{ return BackgroundHandle;	}
	
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){
		appContext = this.getApplicationContext();
		BackgroundHandle = this;
		
		// TODO: Eli. investigate this crash, see email
		// Proposed fix:
		// LoginManager.initialize(appContext);
		// TextFileManager.start(appContext); // CRASH HAPPENS HERE; NPE on LoginManager.getPatientID() because pref hasn't been initialized and is null
		
		// Download the survey questions and schedule the surveys
		QuestionsDownloader downloader = new QuestionsDownloader(appContext);
		downloader.downloadJsonQuestions();
		
		gpsListener = new GPSListener(appContext);
		accelerometerListener = new AccelerometerListener( appContext );
		startBluetooth();
		
		startSmsSentLogger();
		startCallLogger();
		startPowerStateListener();
		
		startTimers();
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
	####################            Timer Logic             #######################
	#############################################################################*/
	
	/** create timers that will trigger events throughout the program, and
	 * register the custom Intents with the controlMessageReceiver. */
	public void startTimers() {
		timer = new Timer(this);

		IntentFilter filter = new IntentFilter();
		
		filter.addAction( appContext.getString( R.string.accelerometer_off ) );
		filter.addAction( appContext.getString( R.string.accelerometer_on ) );
		filter.addAction( appContext.getString( R.string.action_accelerometer_timer ) );
		filter.addAction( appContext.getString( R.string.action_bluetooth_timer ) );
		filter.addAction( appContext.getString( R.string.action_gps_timer ) );
		filter.addAction( appContext.getString( R.string.action_signout_timer ) );
		filter.addAction( appContext.getString( R.string.action_wifi_log ) );
		filter.addAction( appContext.getString( R.string.bluetooth_off ) );
		filter.addAction( appContext.getString( R.string.bluetooth_on ) );
		filter.addAction( appContext.getString( R.string.daily_survey ) );
		filter.addAction( appContext.getString( R.string.gps_off ) );
		filter.addAction( appContext.getString( R.string.gps_on ) );
		filter.addAction( appContext.getString( R.string.signout_intent ) );
		filter.addAction( appContext.getString( R.string.voice_recording ) );
		filter.addAction( appContext.getString( R.string.weekly_survey ) );
		registerReceiver(controlMessageReceiver, filter);
		
//		timer.setupSingularExactAlarm( 5000L, Timer.signOutTimerIntent, Timer.signoutIntent);
//		timer.setupSingularExactAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent);
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent);
//		timer.setupExactHourlyAlarm(Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiLogTimerIntent, Timer.wifiLogIntent);
		
		//FIXME: Josh, create timer for checking for a new survey.  
//		timer.setupDailyRepeatingAlarm(19, new Intent(appContext.getString(R.string.voice_recording)));
	}
	
	public static void restartTimeout(){
		timer.setupSingularExactAlarm( 5000L, Timer.signOutTimerIntent, Timer.signoutIntent);
	}
	
	
	BroadcastReceiver controlMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context appContext, Intent intent) {
			//Log.i("BackgroundService - timers", "Received Broadcast: " + intent.toString());
			
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
				WifiListener.scanWifi();
				timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiLogTimerIntent, Timer.wifiLogIntent); }
			
			if (intent.getAction().equals( appContext.getString(R.string.voice_recording) ) ) {
				AppNotifications.displayRecordingNotification(appContext); }
			
			if (intent.getAction().equals( appContext.getString(R.string.daily_survey) ) ) {
				AppNotifications.displaySurveyNotification(appContext, Type.DAILY); }
			
			if (intent.getAction().equals( appContext.getString(R.string.weekly_survey) ) ) {
				AppNotifications.displaySurveyNotification(appContext, Type.WEEKLY); }
			
			if (intent.getAction().equals(appContext.getString(R.string.signout_intent) ) ) {
				Log.d("BackgroundProcess", "RECEIVED LOGOUT, LOGGING OUT");
				LoginManager.setLoggedIn(false);
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
		Log.e("BackgroundService", "BackgroundService Killed");
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + "," + "BackgroundService Killed" +"\n" ); }
}