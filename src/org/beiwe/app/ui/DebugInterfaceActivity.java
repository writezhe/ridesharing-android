package org.beiwe.app.ui;

import java.security.spec.InvalidKeySpecException;

import org.beiwe.app.R;
import org.beiwe.app.Timer;
import org.beiwe.app.R.layout;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.AudioRecorderActivity;
import org.beiwe.app.survey.SurveyDownloader;
import org.beiwe.app.survey.SurveyActivity;
import org.beiwe.app.ui.user.LoginActivity;
import org.beiwe.app.ui.user.MainMenuActivity;
import org.beiwe.app.ui.utils.SurveyNotifications;

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
//		Log.i("log file encrypted", EncryptionEngine.encryptAES(log) );
	}
	
	public void clearInternalLog(View view) { TextFileManager.getDebugLogFile().deleteSafely(); }
	
	public void uploadDataFiles(View view) { PostRequest.uploadAllFiles(); }
	
	public void runSurveyDownload(View view) { SurveyDownloader.downloadSurveys(getApplicationContext()); }
	
	public void goToAudioRecorder(View view) { startActivity( new Intent(this, AudioRecorderActivity.class) ); }
	
	public void goToSurvey(View view) { startActivity( new Intent(this, SurveyActivity.class) ); }
	
	public void deleteEverything(View view) {
		Log.i("Delete Everything button pressed", "poke.");
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
		TextFileManager.deleteEverything(); }
	
	public void listFiles(View view){
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
	}
	
	public void toggleAccelerometer(View view) {
		Boolean accel_state = backgroundService.accelerometerListener.toggle();
		Log.i("Toggle Accelerometer button pressed", "Accel state: " + accel_state.toString() ); }
	
	public void toggleGPS(View view) {
		Boolean gps_state = backgroundService.gpsListener.toggle();
		Log.i("Toggle GPS button pressed", "GPS state: " + gps_state.toString() ); }
	
	public void signOut (View view) { super.logoutUser(); }
	
	public void scanWifi (View view) { WifiListener.scanWifi(); }
	
	public void bluetoothButtonStart (View view) { appContext.sendBroadcast(Timer.bluetoothOnIntent); }

	public void bluetoothButtonStop (View view) { appContext.sendBroadcast(Timer.bluetoothOffIntent); }
	
	public void buttonTimer(View view) { backgroundService.startTimers(); }
		
	public void getKeyFile(View view) { Log.i("DEBUG", "key file data: " + TextFileManager.getKeyFile().read()); }
	
	public void testEncrypt (View view) {
		Log.i("Debug..", TextFileManager.getKeyFile().read());
		String data = TextFileManager.getKeyFile().read();
		Log.i("reading keyFile:", data );
		
		try { EncryptionEngine.readKey(); }
		catch (InvalidKeySpecException e) {
			Log.e("DebugInterfaceActivity", "this is only partially implemented, unknown behavior");
			e.printStackTrace();
			throw new NullPointerException("some form of encryption error, type 1");
		}

		String encrypted;
		try { encrypted = EncryptionEngine.encryptRSA("ThIs Is a TeSt".getBytes() ).toString(); }
		catch (InvalidKeySpecException e) {
			Log.e("DebugInterfaceActivity", "this is only partially implemented, unknown behavior");
			e.printStackTrace();
			throw new NullPointerException("some form of encryption error, type 2");
		}
		Log.i("test encrypt - length:", "" + encrypted.length() );
		Log.i("test encrypt - output:", encrypted );
		Log.i("test hash:", EncryptionEngine.safeHash( encrypted ) );
	}
	
	public void resetPassword(View view) { startActivity(new Intent(appContext, LoginActivity.class) ); }

	public void loadMainMenu(View view) { startActivity(new Intent(appContext, MainMenuActivity.class) ); }
	
	public void makeNewFiles(View view) { TextFileManager.makeNewFilesForEverything(); }
	
	public void popSurveyNotifications(View view) {
		for (String surveyId : PersistentData.getSurveyIds()){
			SurveyNotifications.displaySurveyNotification(appContext, surveyId);
		}
	}

	public void alarmStates(View view) {
		for (String surveyId : PersistentData.getSurveyIds()){
			SurveyNotifications.displaySurveyNotification(appContext, surveyId);
			Log.i("most recent alarm state", "" +PersistentData.getMostRecentSurveyAlarmTime(surveyId) + ", " + PersistentData.getSurveyNotificationState(surveyId)) ; 
		}
	}
}
