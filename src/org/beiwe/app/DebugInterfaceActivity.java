package org.beiwe.app;
 
import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.storage.Upload;
import org.beiwe.app.survey.AudioRecorderActivity;
import org.beiwe.app.survey.SurveyActivity;
import org.beiwe.app.ui.LoginSessionManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class DebugInterfaceActivity extends Activity {
	
//	TextFileManager logFile = null;
	Context appContext = null;
	
	GPSListener aGPSListener = null;
	AccelerometerListener anAccelerometerListener = null;
	//test variables of our classes
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_interface);
		appContext = this.getApplicationContext();
		
		//start background service
		//NOTE: the background service is started on a separate Thread (process? don't care)
		// and if you attempt to grab any item from a FileManager in this pseudo-constructor
		// the app will almost always crash immediately.  use the following construction to
		// access a FileManager object: TextFileManager.getDebugLogFile().some_function();
		Intent backgroundProcess = new Intent(this, BackgroundProcess.class);
		appContext.startService(backgroundProcess);
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
		String log = TextFileManager.getDebugLogFile().read();
		for( String line : log.split("\n") ) {
			Log.i( "log file...", line ); }
	}

	public void clearInternalLog(View view) {
		Log.i("clear log button pressed", "poke.");
		TextFileManager.getDebugLogFile().deleteSafely();
	}
	
	public void uploadDataFiles(View view) {
		Upload uploader = new Upload(appContext);
		uploader.uploadAllFiles();
	}
	
	public void goToAudioRecorder(View view) {
		Intent audioRecorderIntent = new Intent(this, AudioRecorderActivity.class);
		startActivity(audioRecorderIntent);
	}
	
	public void goToSurvey(View view) {
		Intent surveyIntent = new Intent(this, SurveyActivity.class);
		startActivity(surveyIntent);
	}
	
	public void deleteEverything(View view) {
		Log.i("Delete Everything button pressed", "poke.");
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
		
		TextFileManager.deleteEverything();
	}
	
	public void toggleAccelerometer(View view) {
		AccelerometerListener accel = BackgroundProcess.accelerometerListener;
		Log.i("Toggle Accelerometer button pressed", "Accel state: " + accel.toggle().toString() );
	}
	
	public void toggleGPS(View view) {
		GPSListener gps = BackgroundProcess.gpsListener;
		Log.i("Toggle GPS button pressed", "GPS state: " + gps.toggle().toString() );
	}
	
	public void signOut (View view) {
		LoginSessionManager session = new LoginSessionManager(appContext);
		session.logoutUser();
		finish();
	}
	
	public void bluetoothButtonStart (View view){
		BackgroundProcess.steve.bluetooth.enableBLEScan();
	}

	public void bluetoothButtonStop (View view){
		BackgroundProcess.steve.bluetooth.stopScanning();
	}

}
