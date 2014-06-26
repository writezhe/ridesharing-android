package com.zagaran.scrubs;
 
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.zagaran.scrubs.CSVFileManager;
import com.zagaran.scrubs.BackgroundProcess;
import com.zagaran.scrubs.GPSListener;
import com.zagaran.scrubs.survey.SurveyActivity;

public class DebugInterfaceActivity extends Activity {
	
	CSVFileManager logFile = null;
	Context appContext = null;
	
	GPSListener aGPSListener = null;
	AccelerometerListener anAccelerometerListener = null;
	//test variables of our classes
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_interface);
		appContext = this.getApplicationContext();
		
		//start logger
		CSVFileManager.startFileManager(this.getApplicationContext());
		logFile = CSVFileManager.getDebugLogFile();
		
		//start background service
//		Intent backgroundProcess = new Intent(this, BackgroundProcess.class);
//		appContext.startService(backgroundProcess);
		
		//##########################################################
		// call the start functionality functions here for debugging
		//##########################################################
		startPowerStateListener();
//		startSmsSentLogger();
		
//		aGPSListener = new GPSListener(appContext);
//		aGPSListener.turn_on();
//		
//		anAccelerometerListener = new AccelerometerListener(appContext);
//		Boolean accel = anAccelerometerListener.turn_on();
//		Log.i("debuging accelerometer:", accel.toString() );
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.debug_interface, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void printInternalLog(View view) {
		Log.i("print log button pressed", "press.");
		String log = logFile.read();
		
		for( String line : log.split("\n") ) {
			Log.i( "log file...", line ); }
	}

	public void clearInternalLog(View view) {
		Log.i("clear log button pressed", "poke.");
		logFile.deleteMeSafely();
	}
	
	public void goToAudioRecorder(View view) {
		Intent audioRecorderIntent = new Intent(this, AudioRecorderActivity.class);
		startActivity(audioRecorderIntent);
	}
	
	public void goToSurvey(View view) {
		Intent surveyIntent = new Intent(this, SurveyActivity.class);
		startActivity(surveyIntent);
	}

	
	
//########################################################################################
//###################### Non-UI Things for debugging ###############################
//########################################################################################
	
	
	public void startSmsSentLogger() {
		SmsSentLogger smsSentLogger = new SmsSentLogger(new Handler(), appContext);
		this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsSentLogger);
	}
	
//	well this is interesting, registering these actions in the manifest does not work...
	private void startPowerStateListener() {
		final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		final BroadcastReceiver powerStateReceiver = new PowerStateListener();
		registerReceiver(powerStateReceiver, filter);
	}
}
