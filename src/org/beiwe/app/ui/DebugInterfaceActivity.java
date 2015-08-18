package org.beiwe.app.ui;

import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.beiwe.app.R;
import org.beiwe.app.Timer;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.networking.SurveyDownloader;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
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
	
	
	//Intent triggers caught in BackgroundService
	public void accelerometerOn (View view) { appContext.sendBroadcast( Timer.accelerometerOnIntent ); }
	public void accelerometerOff (View view) { appContext.sendBroadcast( Timer.accelerometerOffIntent ); }	
	public void gpsOn (View view) { appContext.sendBroadcast( Timer.gpsOnIntent ); }
	public void gpsOff (View view) { appContext.sendBroadcast( Timer.gpsOffIntent ); }
	public void scanWifi (View view) { appContext.sendBroadcast( Timer.wifiLogIntent ); }
	public void bluetoothButtonStart (View view) { appContext.sendBroadcast(Timer.bluetoothOnIntent); }
	public void bluetoothButtonStop (View view) { appContext.sendBroadcast(Timer.bluetoothOffIntent); }
	
	
	//raw debugging info
	public void printInternalLog(View view) {
		Log.i("print log button pressed", "press.");
		String log = TextFileManager.getDebugLogFile().read();
		for( String line : log.split("\n") ) {
			Log.i( "log file...", line ); }
//		Log.i("log file encrypted", EncryptionEngine.encryptAES(log) );
	}
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
	public void logDataToggles(View view) {
		Log.i("DebugInterfaceActivity.logDataToggles()", "Accelerometer: " + Boolean.toString(PersistentData.getAccelerometerEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "GPS: " + Boolean.toString(PersistentData.getGpsEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "Calls: " + Boolean.toString(PersistentData.getCallsEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "Texts: " + Boolean.toString(PersistentData.getTextsEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "WiFi: " + Boolean.toString(PersistentData.getWifiEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "Bluetooth: " + Boolean.toString(PersistentData.getBluetoothEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "Power State: " + Boolean.toString(PersistentData.getPowerStateEnabled()));
	}
	public void getAlarmStates(View view) {
		List<String> ids = PersistentData.getSurveyIds();
		for (String surveyId : ids){
			Log.i("most recent alarm state", "" +PersistentData.getMostRecentSurveyAlarmTime(surveyId) + ", " + PersistentData.getSurveyNotificationState(surveyId)) ; 
		}
	}
	public void clearInternalLog(View view) { TextFileManager.getDebugLogFile().deleteSafely(); }
	public void getKeyFile(View view) { Log.i("DEBUG", "key file data: " + TextFileManager.getKeyFile().read()); }
	
	
	//network operations
	public void uploadDataFiles(View view) { PostRequest.uploadAllFiles(); }
	public void runSurveyDownload(View view) { SurveyDownloader.downloadSurveys(getApplicationContext()); }
	public void buttonTimer(View view) { backgroundService.startTimers(); }	
	
	
	//file operations
	public void makeNewFiles(View view) { TextFileManager.makeNewFilesForEverything(); }
	public void deleteEverything(View view) {
		Log.i("Delete Everything button pressed", "poke.");
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
		TextFileManager.deleteEverything(); }
	public void listFiles(View view){
		for( String file : TextFileManager.getAllFiles() ) {
			Log.i( "files...", file); }
	}

	//ui operations
	public void loadMainMenu(View view) { startActivity(new Intent(appContext, MainMenuActivity.class) ); }
	public void popSurveyNotifications(View view) {
		for (String surveyId : PersistentData.getSurveyIds()){
			SurveyNotifications.displaySurveyNotification(appContext, surveyId);
		}
	}
}
