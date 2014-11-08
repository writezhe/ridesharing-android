package org.beiwe.app;

import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.SurveyType.Type;
import org.beiwe.app.ui.AlertsManager;
import org.beiwe.app.ui.AppNotifications;
import org.beiwe.app.ui.AudioRecorderActivity;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.MainMenuActivity;
import org.beiwe.app.ui.SurveyActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class DebugInterfaceActivity extends SessionActivity {
	//extends a session activity.
	Context appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_interface);
		appContext = this.getApplicationContext();
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
		PostRequest.uploadAllFiles(); }
	
	public void goToAudioRecorder(View view) { startActivity( new Intent(this, AudioRecorderActivity.class) ); }
	
	public void goToSurvey(View view) { startActivity( new Intent(this, SurveyActivity.class) ); }
	
	public void deleteEverything(View view) {
		Log.i("Delete Everything button pressed", "poke.");
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
		TextFileManager.deleteEverything(); }
	
	public void toggleAccelerometer(View view) {
		Boolean accel_state = backgroundProcess.accelerometerListener.toggle();
		Log.i("Toggle Accelerometer button pressed", "Accel state: " + accel_state.toString() ); }
	
	public void toggleGPS(View view) {
		Boolean gps_state = backgroundProcess.gpsListener.toggle();
		Log.i("Toggle GPS button pressed", "GPS state: " + gps_state.toString() ); }
	
	public void signOut (View view) {
		LoginManager.setLoggedIn(false);
		startActivity(new Intent(this, LoginActivity.class) );
	}
	
	public void unRegister (View view) {
		LoginManager.setRegistered(false);
		stopService( new Intent( appContext, BackgroundProcess.class) );
		AlertsManager.showAlert("registered set to fals, you must go start the app manually.", this);
		System.exit(0);
	}
	
	//public void bluetoothButtonStart (View view){ BackgroundProcess.getBackgroundHandle().bluetoothListener.enableBLEScan();	}

	//public void bluetoothButtonStop (View view){ BackgroundProcess.getBackgroundHandle().bluetoothListener.disableBLEScan();	}
	
	public void buttonTimer(View view) { backgroundProcess.startTimers(); }
	
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
