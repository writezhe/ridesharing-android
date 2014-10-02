package org.beiwe.app;

import java.util.List;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
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
	private Timer timer;
	public BluetoothListener bluetoothListener;
	
	//TODO: Eli. this [stupid hack] should only be necessary for debugging, comment out before production.
	public static BackgroundProcess BackgroundHandle = null;
	
	private void make_log_statement(String message) {
		Log.i("BackgroundService", message);
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getDebugLogFile().write(javaTimeCode.toString() + "," + message +"\n" ); 
	}
	
	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){		
		appContext = this.getApplicationContext();
		BackgroundHandle = this;
		
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
	}
	
	
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
		PowerStateListener powerStateListener = new PowerStateListener();
		powerStateListener.finish_instantiation(this);  //FIXME:  Eli. fix this, it has to do with airplane mode
		registerReceiver( (BroadcastReceiver) powerStateListener, filter);
	}
	
	/** create timers that will trigger events throughout the program, and
	 * register the custom Intents with the controlMessageReceiver. */
	public void startTimers() {
//		IntentFilter filter = new IntentFilter();
////		timer.setupSingularFuzzyAlarm(5000L, Timer.GPSTimerIntent, Timer.GPSOnIntent);
//		filter.addAction(Timer.GPS_TURN_OFF);
//		filter.addAction(Timer.GPS_TURN_ON);
//		timer.setupExactHourlyAlarm(Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent);
//		filter.addAction(Timer.BLUETOOTH_TURN_OFF);
//		filter.addAction(Timer.BLUETOOTH_TURN_ON);
////		timer.setupSingularFuzzyAlarm(5000L, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent);
//		filter.addAction(Timer.ACCELEROMETER_TURN_OFF);
//		filter.addAction(Timer.ACCELEROMETER_TURN_ON);
////		timer.setupRepeatingAlarm(5000, Timer.signOutTimerIntent, Timer.signoutIntent); // Automatic Signout
////		filter.addAction(Timer.SIGN_OUT);
//		registerReceiver(controlMessageReceiver, filter);
	}
	
	/*#############################################################################
	####################       Externally Accessed Functions       ################
	#############################################################################*/
	
	// FIXME: Eli. THIS CRASHES THE PROGRAM!!! ABANDON ALL HOPE :(
	@SuppressWarnings( "deprecation" )
	@TargetApi( Build.VERSION_CODES.JELLY_BEAN_MR1 )
	/** Checks if airplane mode is active, if so it shuts down the GPSListener. */
	public synchronized void doAirplaneModeThings(){
		//you MUST actively check airplane mode, the system broadcast is not guaranteed when it is toggled quickly. >_o
		Boolean airplaneModeEnabled = null;
		ContentResolver resolver = appContext.getContentResolver();
		//API call changed in jelly bean (4.3).
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			airplaneModeEnabled = Settings.System.getInt(resolver, Settings.System.AIRPLANE_MODE_ON, 0) != 0; }
		else { airplaneModeEnabled = Settings.Global.getInt(resolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0; }
		//if airplane mode enabled and gps is off:
		if (airplaneModeEnabled && gpsListener.check_status() ){
			gpsListener.toggle();
			make_log_statement("GPS turned off"); }
		//if airplane mode disabled and gps is off
		if ( !airplaneModeEnabled && !gpsListener.check_status() ) {
			gpsListener.toggle();
			if ( gpsListener.check_status() ) { make_log_statement("GPS turned on."); }
			else { make_log_statement("GPS failed to turn on"); } }
	}
	
	/** Checks whether the foreground app is Beiwe
	 * @param myPackage
	 * @return */
	public boolean isForeground(String myPackage){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List < ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 
		
		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if(componentInfo.getPackageName().equals(myPackage)) return true;
		return false;
	}
	
	/*#############################################################################
	####################       Contlrol Message Logic         #####################
	#############################################################################*/
	
	BroadcastReceiver controlMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context appContext, Intent intent) {
			Log.i("BackgroundService", "Received Broadcast: " + intent.toString());
			TextFileManager.getDebugLogFile().write("");
			
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
			
			if (intent.getAction().equals(appContext.getString(R.string.signout_intent)) ) {
				Log.i("BackgroundProcess", "Received Signout Message");
				// TODO: Dori. Needs to be tested
				
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
	
	/** The BackgroundService is meant to be all the time, so we return START_STICKY */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){ return START_STICKY; }
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	@Override
	public void onDestroy() {
		//this does not appear to run when the service or app are killed...
		//TODO: Eli. research when onDestroy is actually called, insert informative comment.
		make_log_statement("BackgroundService Killed"); }
}