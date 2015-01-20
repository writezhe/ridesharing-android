package org.beiwe.app;

import org.beiwe.app.storage.EncryptionEngine;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;


/**This is a class that NEEDS to be instantiated in the background process. In order to get the Android ID, the class needs
 * Context. Once instantiated, the class assigns two variables for AndroidID and BluetoothMAC. Once they are instantiated,
 * they can be called from different classes to be used. They are hashed when they are called.
 * 
 * The class is used to grab unique ID data, and pass it to the server. The data is used while authenticating users
 * 
 * @author Dor Samet, Eli Jones */
public class DeviceInfo {
	private static String androidID;
	private static String bluetoothMAC;
	private static String phoneNumber;
	
	/** grab the Android ID and the Bluetooth's MAC address */
	public static void initialize(Context appContext) {
		androidID = Settings.Secure.getString( appContext.getContentResolver(), Settings.Secure.ANDROID_ID );

		/* If the BluetoothAdapter is null, or if the BluetoothAdapter.getAddress() returns null 
		 * (this does happen sometimes!), record an empty string for the Bluetooth Mac address. */
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();	
		if ( bluetoothAdapter == null || bluetoothAdapter.getAddress() == null ) { bluetoothMAC = ""; }
		else { bluetoothMAC = bluetoothAdapter.getAddress(); }
		
		TelephonyManager phoneManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
		phoneNumber = phoneManager.getLine1Number();
		if (phoneNumber == null) phoneNumber = "";
	} 

	public static String getAndroidID() { return EncryptionEngine.safeHash(androidID); }
	 
	public static String getBlootoothMAC() { return EncryptionEngine.safeHash(bluetoothMAC); }
	
	public static String getPhoneNumber() { return EncryptionEngine.hashPhoneNumber(phoneNumber); }
}