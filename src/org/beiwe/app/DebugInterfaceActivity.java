package org.beiwe.app;
 
import java.io.IOException;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.storage.FileDownloader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.storage.Upload;
import org.beiwe.app.survey.AudioRecorderActivity;
import org.beiwe.app.survey.SurveyActivity;
import org.beiwe.app.ui.AppNotifications;
import org.beiwe.app.ui.LoginSessionManager;
import org.beiwe.app.ui.ResetPassword;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


// TODO: SOMEONE. Error strings - make sure to add error strings

public class DebugInterfaceActivity extends Activity {
	
//	TextFileManager logFile = null;
	Context appContext = null;
	
	GPSListener aGPSListener = null;
	AccelerometerListener anAccelerometerListener = null;
	//test variables of our classes
	
	private LoginSessionManager sessionManager = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_interface);
		Log.i("DebugInterface", "Created!");
		appContext = this.getApplicationContext();
		
		//start background service
		//NOTE: the background service is started on a separate Thread (process? don't care)
		// and if you attempt to grab any item from a FileManager in this pseudo-constructor
		// the app will almost always crash immediately.  use the following construction to
		// access a FileManager object: TextFileManager.getDebugLogFile().some_function();
		Intent backgroundProcess = new Intent(this, BackgroundProcess.class);
		appContext.startService(backgroundProcess);
	}
	
//	@Override
//	public void onResume() {
//		super.onResume();
//		sessionManager = new LoginSessionManager(appContext);
//		if (!sessionManager.isLoggedIn()) {
//			sessionManager.logoutUser();
//			finish();
//		}
//	}
//	

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
		Boolean accel_state = BackgroundProcess.BackgroundHandle.accelerometerListener.toggle();
		Log.i("Toggle Accelerometer button pressed", "Accel state: " + accel_state.toString() );
	}
	
	public void toggleGPS(View view) {
		Boolean gps_state = BackgroundProcess.BackgroundHandle.gpsListener.toggle();
		Log.i("Toggle GPS button pressed", "GPS state: " + gps_state.toString() );
	}
	
	public void signOut (View view) {
		LoginSessionManager session = new LoginSessionManager(appContext);
		session.logoutUser();
		finish();
	}
	
	public void bluetoothButtonStart (View view){
		BackgroundProcess.BackgroundHandle.bluetoothListener.enableBLEScan();
	}

	public void bluetoothButtonStop (View view){
		BackgroundProcess.BackgroundHandle.bluetoothListener.disableBLEScan();
	}
	
	public void buttonTimer(View view) {
		BackgroundProcess.BackgroundHandle.startTimers();
	}
	
	public void notificationSender (View view) {
		AppNotifications.displaySurveyNotification(appContext);
		Log.i("DebugInterfaceActivity", "Notification Displayed");
	}
	
	public void cancelNotification(View view) {
		AppNotifications.dismissNotificatoin(appContext, AppNotifications.surveyCode);
	}
	
	public void notificationRecordingSender (View view) {
		AppNotifications.displayRecordingNotification(appContext);
		Log.i("DebugInterfaceActivity", "Notification Displayed");
	}
	
	public void getKeyFile(View view) {
		new GetKeyFile().execute(" ");
	}
	
	class GetKeyFile extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				return FileDownloader.downloadFileFromURL("http://beiwe.org/fetch_key");
			} catch (IOException e) {
				Log.i("GetKeyFile", "Couldn't download key file. Error = " + e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (result != null) {
				TextFileManager.getKeyFile().newFile();
				FileDownloader.writeStringToFile(result, TextFileManager.getKeyFile());
				
				Log.i("GetKeyFile", "Contents = " + TextFileManager.getKeyFile().read());
			}			
		}
	}
	

	public void testEncrypt (View view) {
		
		write_public();
		String data = TextFileManager.getKeyFile().read();
		Log.i("reading keyFile:", data );
		
		EncryptionEngine.readKey();

		Log.i("something", "anything");

		String encrypted = EncryptionEngine.encrypt("ThIs Is a TeSt");
		Log.i("test encrypt", encrypted );
		Log.i("test encrypt - length", "" + encrypted.length() );
	}
	
	
	void write_public() {
		TextFileManager.getKeyFile().deleteSafely();
		TextFileManager.getKeyFile().write("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApTiH9gI0zXimSSX+lIsPVvsRDKj5+ebBKAxUJ/laWkfz59yDmfw9TkuLPRfU5cI4GWN/3kzyVdP72bt8p7ZU1LpL/WsSrRb3mzDXZtUnEnfKMTL2NvXUG/qJJyI0wzTmTNaY/hN4aKhITTBjX2Lo+8REtHuijxvaVVThbwlEg+Hmk5611f/BoHC29jHI1O6j4t+PdlO+2h+jBYthL7C0+Tfu74s0o3CLonCGNik8sLsZ6hps1sz0Gwn4f4ehLe7OwGviv4svZZAhufJebyPFNaIbWiO8bQN7ev8rEwnT9ROsBZvZ1AzoUYtewBxauBgpF8/NdvgcNqkILKbIFvscawIDAQAB");
	}
	
	public void resetPassword(View view) {
		Log.i("DebugInterface", "this has been called!");
	}
}
