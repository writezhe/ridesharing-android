package org.beiwe.app;

import org.beiwe.app.storage.EncryptionEngine;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;


/**This is a class that NEEDS to be instantiated in the background service. In order to get the Android ID, the class needs
 * Context. Once instantiated, the class assigns two variables for AndroidID and BluetoothMAC. Once they are instantiated,
 * they can be called from different classes to be used. They are hashed when they are called.
 * 
 * The class is used to grab unique ID data, and pass it to the server. The data is used while authenticating users
 * 
 * @author Dor Samet, Eli Jones */  

public class DeviceInfo {
	/* TODO:  Ensure this number is updated whenever a version of the app is pushed to the website for any reason.
	 * Don't forget to update the version in the android manifest, only the version string is visible to the user.
	 * Version history:
	 * 1: add additional device data during the registration process, including this version number.
	 * 2: added sms debugging
	 * 3: added universal crash log to the app
	 * 4: added a lot of CrashHandler integration to all sorts of error-handled conditions the codebase. */
	
	private static Integer beiweVersion = 4;
	//DO NOT FORGET TO UPDATE THE MANIFEST VERSION NUMBERS AS WELL.
	
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
	
	public static String getBeiweVersion() { return beiweVersion.toString(); }
	public static String getAndroidVersion() { return android.os.Build.VERSION.RELEASE; }
	public static String getProduct() { return android.os.Build.PRODUCT; }
	public static String getBrand() { return android.os.Build.BRAND; }
	public static String getHardwareId() { return android.os.Build.HARDWARE; }
	public static String getManufacturer() { return android.os.Build.MANUFACTURER; }
	public static String getModel() { return android.os.Build.MODEL; }
	public static String getAndroidID() { return EncryptionEngine.safeHash(androidID); }
	public static String getBlootoothMAC() { return EncryptionEngine.safeHash(bluetoothMAC); }
	public static String getPhoneNumber() { return EncryptionEngine.hashPhoneNumber(phoneNumber); }
}