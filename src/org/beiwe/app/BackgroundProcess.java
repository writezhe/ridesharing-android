package org.beiwe.app;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.networking.PostRequest;
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
import android.os.Binder;
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
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){
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
		startCallLogger();
		startPowerStateListener();
		registerTimers();
		
		//If this device is both registered and timers have not already been started, start them!
		//FIXME: this logic needs improvement, it currently resets timers whenever the backgroundservice is restarted.
		if (LoginManager.isRegistered()) {
			Log.i("BackgroundProcess", "starting timers");
//			startTimers();
		}
		
		// Download the survey questions and schedule the surveys
		// TODO: Josh. onCreate is going to be called with some frequency, can you add some logic
		// so it only downloads new surveys if ... there are no current files? (other logic is good)
		QuestionsDownloader downloader = new QuestionsDownloader(appContext);
		downloader.downloadJsonQuestions();
	}


	@Override
	/** The BackgroundService is meant to be all the time, so we return START_STICKY */
	//testing start_redeliver_intent
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.w("BackroundProcess onStartCommand", "started with flag " + flags );
		return START_REDELIVER_INTENT; 
	}
	
	@Override
	public void onDestroy() {
		//this does not run when the service is killed in a task manager, OR when the stopService() function is called from debugActivity.
		Log.e("BackgroundService", "BACKGROUNDPROCESS WAS DESTROYED.");
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + "," + "BACKGROUNDPROCESS WAS DESTROYED" +"\n" );
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
	private void registerTimers() {
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
	}
	
	public void startTimers(){
//		timer.setupSingularExactAlarm( 5000L, Timer.signOutTimerIntent, Timer.signoutIntent);
//		timer.setupSingularExactAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent);
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent);
//		timer.setupExactHourlyAlarm(Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
		timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiLogTimerIntent, Timer.wifiLogIntent);
		
		//FIXME: Josh, create timer for checking for a new survey.
//		timer.setupDailyRepeatingAlarm(19, Timer.voiceRecordingIntent);
	}
	
	public static void resetAutomaticLogoutCountdownTimer(){
		timer.setupSingularExactAlarm( 5000L, Timer.signOutTimerIntent, Timer.signoutIntent);
	}
	
	public static void setDailySurvey(int hour) {
		timer.setupDailyRepeatingAlarm(hour, Timer.dailySurveyIntent);
	}
	
	public static void setWeeklySurvey(int hour, int dayOfWeek) {
		timer.setupWeeklyRepeatingAlarm(dayOfWeek, hour, Timer.weeklySurveyIntent);
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
				// TODO Josh: take user to LoginActivity?
				// repy: Eli: that should not be necessary. the pattern of triggers of the SessionActivity's onPause, onResume, onDestroy expects this behavior
				LoginManager.logout();
			}
		}
	};
	
	/*##########################################################################################
	################# onStartCommand, onBind, and onDesroy (ignore these)# #####################
	##########################################################################################*/
	@Override
	public IBinder onBind(Intent arg0) { return new BackgroundProcessBinder(); }
	
	//this is the public "Binder" class, it provides a (safe) handle to the background process
	public class BackgroundProcessBinder extends Binder {
        public BackgroundProcess getService() {
            return BackgroundProcess.this;
        }
    }
}