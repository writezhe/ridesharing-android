package org.beiwe.app;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.AudioRecorderActivity;
import org.beiwe.app.survey.SurveyActivity;
import org.beiwe.app.survey.SurveyType.Type;
import org.beiwe.app.ui.AppNotifications;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.MainMenuActivity;
import org.beiwe.app.ui.ResetPasswordActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class DebugInterfaceActivity extends Activity {
	Context appContext = null;
	AccelerometerListener anAccelerometerListener = null;
	
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
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.common_menu, menu);
		return true;
	}
	
	@Override
	// TODO Josh: put this into the Activity superclass, once it's built.
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_about:
			// TODO Josh: create an about-the-app page/activity.
			return true;
		case R.id.menu_call_hotline:
			// TODO Josh: if possible, maybe make this a static function somewhere, or just make sure it's only implemented once. Right now it's implemented like 3 times. Just ack for "R.string.hotline_phone_number" or something to figure out where else it's implemented.
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			String phoneNum = (String) getApplicationContext().getResources().getText(R.string.hotline_phone_number);
		    callIntent.setData(Uri.parse("tel:" + phoneNum));
		    startActivity(callIntent);
			return true;
		case R.id.menu_change_password:
			startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
			return true;
		case R.id.menu_signout:
			LoginManager.logoutUser();
			finish();			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void printInternalLog(View view) {
		Log.i("print log button pressed", "press.");
		String log = TextFileManager.getDebugLogFile().read();
		for( String line : log.split("\n") ) {
			Log.i( "log file...", line ); }
	}

	public void clearInternalLog(View view) {
		Log.i("clear log button pressed", "poke.");
		TextFileManager.getDebugLogFile().deleteSafely(); }
	
	public void uploadDataFiles(View view) {
//		NetworkUtilities.initializeNetworkUtilities(appContext);
		PostRequest.uploadAllFiles(); }
	
	public void goToAudioRecorder(View view) { startActivity( new Intent(this, AudioRecorderActivity.class) ); }
	
	public void goToSurvey(View view) { startActivity( new Intent(this, SurveyActivity.class) ); }
	
	public void deleteEverything(View view) {
		Log.i("Delete Everything button pressed", "poke.");
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
		TextFileManager.deleteEverything(); }
	
	public void toggleAccelerometer(View view) {
		Boolean accel_state = BackgroundProcess.getBackgroundHandle().accelerometerListener.toggle();
		Log.i("Toggle Accelerometer button pressed", "Accel state: " + accel_state.toString() ); }
	
	public void toggleGPS(View view) {
		Boolean gps_state = BackgroundProcess.getBackgroundHandle().gpsListener.toggle();
		Log.i("Toggle GPS button pressed", "GPS state: " + gps_state.toString() ); }
	
	public void signOut (View view) {
		LoginManager.logoutUser();
		finish(); }
	
	//public void bluetoothButtonStart (View view){ BackgroundProcess.getBackgroundHandle().bluetoothListener.enableBLEScan();	}

	//public void bluetoothButtonStop (View view){ BackgroundProcess.getBackgroundHandle().bluetoothListener.disableBLEScan();	}
	
	public void buttonTimer(View view) { BackgroundProcess.getBackgroundHandle().startTimers(); }
	
	public void notificationSender (View view) {
		AppNotifications.displaySurveyNotification(appContext, Type.DAILY);
		Log.i("DebugInterfaceActivity", "Notification Displayed"); 	}
	
	public void notificationRecordingSender (View view) {
		AppNotifications.displayRecordingNotification(appContext);
		Log.i("DebugInterfaceActivity", "Notification Displayed"); }
	
	public void getKeyFile(View view) {
		Log.i("DEBUG", "key file data: " + TextFileManager.getKeyFile().read());
	}
	
	public void testEncrypt (View view) {
		Log.i("Debug..", TextFileManager.getKeyFile().read());
//		write_public();
		String data = TextFileManager.getKeyFile().read();
		Log.i("reading keyFile:", data );
		
		EncryptionEngine.readKey();

		String encrypted = EncryptionEngine.encrypt("ThIs Is a TeSt");
		Log.i("test encrypt - length:", "" + encrypted.length() );
		Log.i("test encrypt - output:", encrypted );
		Log.i("test hash:", EncryptionEngine.safeHash( encrypted ) );
	}
	
	
	void write_public() {
		TextFileManager.getKeyFile().deleteSafely();
		TextFileManager.getKeyFile().write("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApTiH9gI0zXimSSX+lIsPVvsRDKj5+ebBKAxUJ/laWkfz59yDmfw9TkuLPRfU5cI4GWN/3kzyVdP72bt8p7ZU1LpL/WsSrRb3mzDXZtUnEnfKMTL2NvXUG/qJJyI0wzTmTNaY/hN4aKhITTBjX2Lo+8REtHuijxvaVVThbwlEg+Hmk5611f/BoHC29jHI1O6j4t+PdlO+2h+jBYthL7C0+Tfu74s0o3CLonCGNik8sLsZ6hps1sz0Gwn4f4ehLe7OwGviv4svZZAhufJebyPFNaIbWiO8bQN7ev8rEwnT9ROsBZvZ1AzoUYtewBxauBgpF8/NdvgcNqkILKbIFvscawIDAQAB");
	}
	
	public void resetPassword(View view) { startActivity(new Intent(appContext, LoginActivity.class) ); }

	public void loadMainMenu(View view) { startActivity(new Intent(appContext, MainMenuActivity.class) ); }
}
