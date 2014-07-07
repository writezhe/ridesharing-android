package org.beiwe.app;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.storage.TextFileManager;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
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

	TextFileManager logFile = null;
	Context appContext = null;
	PackageManager packageManager = null; 	//used to check if sensors exist
	
	GPSListener gpsListener;
	AccelerometerListener accelerometerListener;
	
	private void make_log_statement(String message) {
		Log.i("BackgroundService", message);
		Long javaTimeCode = System.currentTimeMillis();
		logFile.write(javaTimeCode.toString() + "," + message +"\n" ); 
	}
	
	public void onCreate(){
		/** onCreate is the constructor for the service, initialize variables here.*/
		appContext = this.getApplicationContext();
		packageManager = this.getPackageManager();
		
		TextFileManager.start(appContext);
		logFile = TextFileManager.getDebugLogFile();
		
		gpsListener = new GPSListener(appContext);
		accelerometerListener = new AccelerometerListener(appContext);
		
		make_log_statement("Things have allocated, starting listeners");
		
		startSmsSentLogger();
		startPowerStateListener();
								
		Boolean accelStatus = accelerometerListener.turn_on( );
		Log.i("accelStatus", accelStatus.toString() );
		Boolean gpsStatus = gpsListener.turn_on();
		Log.i("accelStatus", gpsStatus.toString() );
	}

	@Override
	public void onDestroy() {
		//this does not appear to run when the service or app are killed...
		//TODO: research when onDestroy is actually called, insert informative comment
		make_log_statement("BackgroundService Killed");
		// TODO: decide if we need to unRegisterContentObserver(SmsSentLogger) in onDestroy() or something
	}
	
	/** Initializes the sms logger. */
	public void startSmsSentLogger() {
		SmsSentLogger smsSentLogger = new SmsSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsSentLogger);	}
	
	/** Initializes the PowerStateListener. */
	private void startPowerStateListener() {
//		The the ACTION_SCREEN_ON and ACTION_SCREEN_OFF intents must be registered at initialization.
		final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		final BroadcastReceiver mReceiver = new PowerStateListener();
		registerReceiver(mReceiver, filter);
	}

//	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	/** Checks if airplane mode is active, if so it shuts down the GPSListener. */
	public synchronized void doAirplaneModeThings(){
		//you MUST actively check airplane mode, the system broadcast is not guaranteed when it is toggled quickly. >_o
		Boolean AirplaneModeEnabled = null;
		ContentResolver resolver = appContext.getContentResolver();
		//API call changed in jelly bean (4.3).
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        AirplaneModeEnabled = Settings.System.getInt(resolver, Settings.System.AIRPLANE_MODE_ON, 0) != 0; }
	    else { AirplaneModeEnabled = Settings.Global.getInt(resolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0; }
	    
	    if (AirplaneModeEnabled){ gpsListener.turn_off(); 
	    	make_log_statement("GPS turned off");}
	    else { gpsListener.turn_on(); 
	    make_log_statement("GPS turn on");}
	}
	
/*###############################################################################
################ onStartCommand and onBind, ignore these ########################
###############################################################################*/	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i("BackgroundService received start command:", ""+startId );
		return START_STICKY; }
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i("BackgroundService has been bound", "");
		return null; }
}