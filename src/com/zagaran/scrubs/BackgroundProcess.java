package com.zagaran.scrubs;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.zagaran.scrubs.CSVFileManager;
 

public class BackgroundProcess extends Service {

	CSVFileManager logFile = null;
	Context appContext = null;
	PackageManager packageManager = null; 	//used to check if sensors exist
	
	
	public void onCreate(){
		/** onCreate is the constructor for the service, initialize variables here.*/
		Log.i("BackgroundService has started.", "start");
		appContext = this.getApplicationContext();
		packageManager = this.getPackageManager();
	}

	@Override
	public void onDestroy() {
		//this does not appear to run when the service or app are killed...
		//probably runs if the service is stopped by the app?
		Log.i("BackgroundService was killed.", "");
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		logFile.write("\nBackgroundProcess killed at " + timecode);
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