package org.beiwe.app;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
		
		try {
			Log.i("log file encrypted", EncryptionEngine.encryptAES(log) );
		} catch (InvalidKeyException e) {
			Log.e("log file encrypted", "1");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e("log file encrypted", "2");
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e("log file encrypted", "3");
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			Log.e("log file encrypted", "4");
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e("log file encrypted", "5");
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e){
			Log.e("log file encrypted", "6");
			e.printStackTrace();
		}
		
		
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
		super.logoutUser();
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
		String data = TextFileManager.getKeyFile().read();
		Log.i("reading keyFile:", data );
		
		EncryptionEngine.readKey();

		String encrypted = EncryptionEngine.encryptRSA("ThIs Is a TeSt".getBytes() ).toString();
		Log.i("test encrypt - length:", "" + encrypted.length() );
		Log.i("test encrypt - output:", encrypted );
		Log.i("test hash:", EncryptionEngine.safeHash( encrypted ) );
	}
	
	public void resetPassword(View view) { startActivity(new Intent(appContext, LoginActivity.class) ); }

	public void loadMainMenu(View view) { startActivity(new Intent(appContext, MainMenuActivity.class) ); }
}
