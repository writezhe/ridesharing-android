package org.beiwe.app;

import java.util.List;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.LoginSessionManager;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
//import android.content.pm.PackageManager;
//import android.content.pm.PackageManager;

// TODO: Add logic that has to do with receiving a notification

public class BackgroundProcess extends Service {

	private TextFileManager logFile = null;
	private Context appContext = null;
	private LoginSessionManager sessionManager = null;
	//	private PackageManager packageManager = null; 	//used to check if sensors exist

	public GPSListener gpsListener;
	public AccelerometerListener accelerometerListener;

	private Timer timer;

	BluetoothListener bluetooth;
	// TODO: Work out if accessing the Background process using a static method is necessary 
	
	public static BackgroundProcess BackgroundHandle;

	private void make_log_statement(String message) {
		Log.i("BackgroundService", message);
		Long javaTimeCode = System.currentTimeMillis();
		logFile.write(javaTimeCode.toString() + "," + message +"\n" ); 
	}

	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here.*/
	public void onCreate(){		
		appContext = this.getApplicationContext();
		//		packageManager = this.getPackageManager();
		BackgroundHandle = this;
		TextFileManager.start(appContext);
		logFile = TextFileManager.getDebugLogFile();

		// COMMENT COMMENT COMMENT COMMENT COMMENT COMMENT COMMENT
		// COMMENT COMMENT COMMENT COMMENT COMMENT COMMENT COMMENT
		// COMMENT COMMENT COMMENT COMMENT COMMENT COMMENT COMMENT
		gpsListener = new GPSListener(appContext);
		accelerometerListener = new AccelerometerListener(appContext);

		timer = new Timer(this);

		make_log_statement("Things have allocated, starting listeners");

		startSmsSentLogger();
		startCallLogger();
		startPowerStateListener();
		startBluetoothListener();
		
		//		Boolean accelStatus = accelerometerListener.toggle( );
		//		Log.i("accel Status", accelStatus.toString() );
		//		Boolean gpsStatus = gpsListener.toggle();
		//		Log.i("GPS Status", gpsStatus.toString() );

		startTimers();
		startControlMessageReceiver();
	}

	public boolean isForeground(String myPackage){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if(componentInfo.getPackageName().equals(myPackage)) return true;
		return false;
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
		powerStateListener.finish_instantiation(this);  //TODO: fix this, it has to do with airplane mode
		registerReceiver( (BroadcastReceiver) powerStateListener, filter);
	}

	/** Start the BluetoothListener */
	private void startBluetoothListener (){
		bluetooth = new BluetoothListener(); }
	
	/** Register custom Intents with the control message receiver */
	public void startControlMessageReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Timer.ACCELEROMETER_OFF);
		filter.addAction(Timer.ACCELEROMETER_ON);
		filter.addAction(Timer.BLUETOOTH_OFF);
		filter.addAction(Timer.BLUETOOTH_ON);
		filter.addAction(Timer.GPS_OFF);
		filter.addAction(Timer.GPS_ON);
		filter.addAction(Timer.SIGN_OUT);
		registerReceiver(controlReceiver, filter);
	}
	
	/** create timers that will trigger events throughout the program. */
	private void startTimers() {
		timer.setupRepeatingAlarm(5000, Timer.GPSTimerIntent, Timer.GPSOnIntent); // GPS
		timer.setupRepeatingAlarm(5000, Timer.bluetoothTimerIntent, Timer.bluetoothOnIntent); // Bluetooth
		timer.setupRepeatingAlarm(5000, Timer.accelerometerTimerIntent, Timer.accelerometerOnIntent); // Accelerometer

		timer.setupRepeatingAlarm(5000, Timer.signOutTimerIntent, Timer.signoutIntent); // Automatic Signout
	}
	
	/*##############################################################
	  ###############       Separator Comment       ################
	  #############################################################*/

	// FIXME: THIS CRASHES THE PROGRAM!!! ABANDON ALL HOPE :(
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
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

	/*##########################################################################################
	################## onStartCommand, onBind, and onDesroy, ignore these ######################
	##########################################################################################*/	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		//		Log.i("BackgroundService received start command:", "" + startId );
		return START_STICKY; }
	@Override
	public IBinder onBind(Intent arg0) {
		//		Log.i("BackgroundService has been bound", "");
		return null; }
	@Override
	public void onDestroy() {
		//this does not appear to run when the service or app are killed...
		//TODO: research when onDestroy is actually called, insert informative comment.
		make_log_statement("BackgroundService Killed");
	}
	
	public void togglegps(){
		Log.i("Something", "anythingeutaoeiungt");
//		gpsListener.toggle();
	}
	
	//TODO: make this a separate class, control receiver
	BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context appContext, Intent intent) {
			BackgroundProcess back = BackgroundProcess.BackgroundHandle; 
			
			Log.i("BackgroundService", "Received Broadcast " + intent.toString());

			if (intent.getAction().equals( Timer.ACCELEROMETER_OFF ) ) {
				back.accelerometerListener.turn_on(); }

			if (intent.getAction().equals( Timer.ACCELEROMETER_ON ) ) {
				back.accelerometerListener.turn_off(); }

			if (intent.getAction().equals( Timer.BLUETOOTH_OFF ) ) {
				back.bluetooth.disableBLEScan(); }

			if (intent.getAction().equals( Timer.BLUETOOTH_ON ) ) {
				back.bluetooth.enableBLEScan();
			}

			if (intent.getAction().equals( Timer.GPS_OFF ) ) {
				back.gpsListener.turn_off();
			}

			if (intent.getAction().equals( Timer.GPS_ON ) ) {
				back.gpsListener.toggle();
				back.gpsListener.turn_on();
			}
			
			if (intent.getAction().equals(Timer.SIGN_OUT) ){

				Log.i("BackgroundProcess", "Received Signout Message");
//				sessionManager = new LoginSessionManager(appContext);
//				if(isForeground("org.beiwe.app")) {
//					sessionManager.logoutUser();
//				} else {
//					sessionManager.logoutUserPassive();
//				}
			}
		}; 
	};

}