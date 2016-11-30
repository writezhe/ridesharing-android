package org.beiwe.app.ui;

import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.PermissionHandler;
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
		// Log.i("print log button pressed", "press.");
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
	
	public void getEnabledFeatures(View view) {
		if ( PersistentData.getAccelerometerEnabled() ) { Log.i("features", "Accelerometer Enabled." ); } else { Log.e("features", "Accelerometer Disabled."); }
		if ( PersistentData.getGpsEnabled() ) { Log.i("features", "Gps Enabled." ); } else { Log.e("features", "Gps Disabled."); }
		if ( PersistentData.getCallsEnabled() ) { Log.i("features", "Calls Enabled." ); } else { Log.e("features", "Calls Disabled."); }
		if ( PersistentData.getTextsEnabled() ) { Log.i("features", "Texts Enabled." ); } else { Log.e("features", "Texts Disabled."); }
		if ( PersistentData.getWifiEnabled() ) { Log.i("features", "Wifi Enabled." ); } else { Log.e("features", "Wifi Disabled."); }
		if ( PersistentData.getBluetoothEnabled() ) { Log.i("features", "Bluetooth Enabled." ); } else { Log.e("features", "Bluetooth Disabled."); }
		if ( PersistentData.getPowerStateEnabled() ) { Log.i("features", "PowerState Enabled." ); } else { Log.e("features", "PowerState Disabled."); }
	}
	
	public void getPermissableFeatures(View view) {
		if (PermissionHandler.checkAccessFineLocation(getApplicationContext())) { Log.i("permissions", "AccessFineLocation enabled."); } else { Log.e("permissions", "AccessFineLocation disabled."); }
		if (PermissionHandler.checkAccessNetworkState(getApplicationContext())) { Log.i("permissions", "AccessNetworkState enabled."); } else { Log.e("permissions", "AccessNetworkState disabled."); }
		if (PermissionHandler.checkAccessWifiState(getApplicationContext())) { Log.i("permissions", "AccessWifiState enabled."); } else { Log.e("permissions", "AccessWifiState disabled."); }
		if (PermissionHandler.checkAccessBluetooth(getApplicationContext())) { Log.i("permissions", "Bluetooth enabled."); } else { Log.e("permissions", "Bluetooth disabled."); }
		if (PermissionHandler.checkAccessBluetoothAdmin(getApplicationContext())) { Log.i("permissions", "BluetoothAdmin enabled."); } else { Log.e("permissions", "BluetoothAdmin disabled."); }
		if (PermissionHandler.checkAccessCallPhone(getApplicationContext())) { Log.i("permissions", "CallPhone enabled."); } else { Log.e("permissions", "CallPhone disabled."); }
		if (PermissionHandler.checkAccessReadCallLog(getApplicationContext())) { Log.i("permissions", "ReadCallLog enabled."); } else { Log.e("permissions", "ReadCallLog disabled."); }
		if (PermissionHandler.checkAccessReadContacts(getApplicationContext())) { Log.i("permissions", "ReadContacts enabled."); } else { Log.e("permissions", "ReadContacts disabled."); }
		if (PermissionHandler.checkAccessReadPhoneState(getApplicationContext())) { Log.i("permissions", "ReadPhoneState enabled."); } else { Log.e("permissions", "ReadPhoneState disabled."); }
		if (PermissionHandler.checkAccessReadSms(getApplicationContext())) { Log.i("permissions", "ReadSms enabled."); } else { Log.e("permissions", "ReadSms disabled."); }
		if (PermissionHandler.checkAccessReceiveMms(getApplicationContext())) { Log.i("permissions", "ReceiveMms enabled."); } else { Log.e("permissions", "ReceiveMms disabled."); }
		if (PermissionHandler.checkAccessReceiveSms(getApplicationContext())) { Log.i("permissions", "ReceiveSms enabled."); } else { Log.e("permissions", "ReceiveSms disabled."); }
		if (PermissionHandler.checkAccessRecordAudio(getApplicationContext())) { Log.i("permissions", "RecordAudio enabled."); } else { Log.e("permissions", "RecordAudio disabled."); }
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
		String[] files = TextFileManager.getAllFiles();
		Arrays.sort(files);
		for( String file : files ) { Log.i( "files...", file); }
		TextFileManager.deleteEverything(); }
	public void listFiles(View view){
		String[] files = TextFileManager.getAllFiles();
		Arrays.sort(files);
		for( String file : files ) { Log.i( "files...", file); }
	}

	//ui operations
	public void loadMainMenu(View view) { startActivity(new Intent(appContext, MainMenuActivity.class) ); }
	public void popSurveyNotifications(View view) {
		for (String surveyId : PersistentData.getSurveyIds()){
			SurveyNotifications.displaySurveyNotification(appContext, surveyId);
		}
	}
	
	//crash operations (No, really, we actually need this.)
	public void crashUi(View view) { throw new NullPointerException("oops, you bwoke it."); }
	public void crashBackground(View view) { BackgroundService.timer.setupExactSingleAlarm((long) 0, new Intent("crashBeiwe")); }
	public void crashBackgroundInFive(View view) { BackgroundService.timer.setupExactSingleAlarm((long) 5000, new Intent("crashBeiwe")); }
	public void stopBackgroundService(View view) { backgroundService.stop(); }
}
