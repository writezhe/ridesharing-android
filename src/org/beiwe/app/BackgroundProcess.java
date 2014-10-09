package org.beiwe.app;

import java.util.List;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WiFiListener;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.TextFileManager;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
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

	public static BackgroundProcess getBackgroundHandle(){
		if (BackgroundHandle != null) { return BackgroundHandle; }
		Log.e("BackgroundProcess", "background process handle called for before background process had started." );
		throw new NullPointerException();
	}
	
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){		
		appContext = this.getApplicationContext();
		BackgroundHandle = this;
//		NetworkUtilities.initializeNetworkUtilities(appContext);
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
	// TODO: consider deprecating this function because every activity calls finish() when it ends, so onPause() should only get called when the app really is paused
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
		IntentFilter filter = new IntentFilter();
		
		// TODO: Eli. This is fixed now. Set up timers throughout the program, or tell Dori how to do it
		
		filter.addAction( appContext.getString( R.string.accelerometer_off ) );
		filter.addAction( appContext.getString( R.string.accelerometer_on ) );
		filter.addAction( appContext.getString( R.string.bluetooth_off ) );
		filter.addAction( appContext.getString( R.string.bluetooth_on ) );
		filter.addAction( appContext.getString( R.string.gps_off ) );
		filter.addAction( appContext.getString( R.string.gps_on ) );
		filter.addAction( appContext.getString( R.string.signout_intent ) );
		filter.addAction( appContext.getString( R.string.action_signout_timer ) );
		filter.addAction( appContext.getString( R.string.action_accelerometer_timer ) );
		filter.addAction( appContext.getString( R.string.action_bluetooth_timer ) );
		filter.addAction( appContext.getString( R.string.action_gps_timer ) );
		filter.addAction( appContext.getString( R.string.action_wifi_scan ) );
	
//		timer.setupExactHourlyAlarm(Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
//		timer.setupRepeatingAlarm(5000, Timer.signOutTimerIntent, Timer.signoutIntent); // Automatic Signout
//		filter.addAction(Timer.SIGN_OUT);
		registerReceiver(controlMessageReceiver, filter);
	
		//TODO: add timer for checking for new survey
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiScanTimerIntent, Timer.wifiScanIntent);
//		timer.setupSingularExactAlarm( 5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent);
//		timer.setupExactHourlyAlarm( Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
//		timer.setupSingularFuzzyAlarm( 5000L, Timer.GPSTimerIntent, Timer.gpsOnIntent);
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
		
			if (intent.getAction().equals( appContext.getString(R.string.action_wifi_scan) ) ) {
				wifiListener.scanWifi();
				timer.setupSingularFuzzyAlarm( 5000L, Timer.wifiScanTimerIntent, Timer.wifiScanIntent); }
			
			// TODO: Dori. Find out when this needs to go off. Provide a function inside the logic logic that is "start logout timer"
			// What needs to be done is to send the activity to the background process in case it is no longer used (onPause, onStop, etc...) 
			// and then start the timer.. There has to be a simpler solution - will write it down as soon as I figuer it out.
			if (intent.getAction().equals(appContext.getString(R.string.signout_intent) ) ) {
				Log.i("BackgroundProcess", "Received Signout Message");
				
				sessionManager = new LoginSessionManager(appContext);
				if( isForeground("org.beiwe.app") ) {
					sessionManager.logoutUser(); }
				else { sessionManager.logoutUserPassive(); }
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
		//TODO: Eli. research when onDestroy is actually called, insert informative comment.
		Log.i("BackgroundService", "BackgroundService Killed");
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + "," + "BackgroundService Killed" +"\n" ); }
}