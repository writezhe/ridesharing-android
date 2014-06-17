package com.zagaran.scrubs;

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

	CSVFileManager logFile = null;
	Context appContext = null;
	PackageManager packageManager = null; 	//used to check if sensors exist
	
	
	public void onCreate(){
		/** onCreate is the constructor for the service, initialize variables here.*/
		Log.i("BackgroundService has started.", "start");
		appContext = this.getApplicationContext();
		packageManager = this.getPackageManager();
		
//the background service can make debugging a little more difficult (we have to deal with a background service)
// so test things using the debug interface activity, but insert the same code commented out here.
		
//		startSmsSentLogger();
//		startPowerStateListener();
		
	}

	@Override
	public void onDestroy() {
		//this does not appear to run when the service or app are killed...
		//probably runs if the service is stopped by the app?
		Log.i("BackgroundService was killed.", "");
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		logFile.write("\nBackgroundProcess killed at " + timecode);
	}
	
	// TODO: ask Eli if this is the right place (from a code organizational standpoint) to call this function
	// Eli: it is, but we are also going to shove it into the debug interface activity
	public void startSmsSentLogger() {
		SmsSentLogger smsSentLogger = new SmsSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsSentLogger);
	}
	// TODO: decide if we need to unRegisterContentObserver(SmsSentLogger) in onDestroy() or something
	
	private void startPowerStateListener() {
		final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		final BroadcastReceiver mReceiver = new PowerStateListener();
		registerReceiver(mReceiver, filter);
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