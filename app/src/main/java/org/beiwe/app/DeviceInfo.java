package org.beiwe.app;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.beiwe.app.storage.EncryptionEngine;

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
	 * 4: added a lot of CrashHandler integration to all sorts of error-handled conditions the codebase.
	 * 5: Initial version of Android 6.
	 * 		Enhanced Audio UI rewrite included,raw recording code is functional but not enabled, no standard on parsing audio surveys for their type.
	 * 6: second version of Android 6.
	 * 		Enhanced audio functionality is included and functional, fixed crash that came up on a device when the app was uninstalled and then reinstalled?
	 * 		A functionally identical 6 was later released, it contains a new error message for a new type of iOS-related registration error.
	 * 7: audio survey files now contain the surveyid for the correct audio survey.
	 * 8: Now support a flag for data upload over cellular, fixed bug in wifi scanning.
	 * 9: Moves Record/Play buttons outside the scroll window on voice recording screen.
	 * 10: Change to TextFileManager to potentially improve uninitialized errors, added device idle and low power mode change to power state listener.*/

	private static String beiweVersion = "11";
	//DO NOT FORGET TO UPDATE THE MANIFEST VERSION NUMBERS AS WELL.
	
	private static String androidID;
	private static String bluetoothMAC;
	//TODO: Eli. phoneNumbeer is not used anywhere...
	private static String phoneNumber;

	/** grab the Android ID and the Bluetooth's MAC address */
	@SuppressLint("HardwareIds")
	public static void initialize(Context appContext) {
		androidID = Settings.Secure.getString( appContext.getContentResolver(), Settings.Secure.ANDROID_ID );
		
		/* If the BluetoothAdapter is null, or if the BluetoothAdapter.getAddress() returns null 
		 * (this does happen sometimes!), record an empty string for the Bluetooth Mac address. */
		
		if ( android.os.Build.VERSION.SDK_INT >= 23) { //This will not work on all devices: http://stackoverflow.com/questions/33377982/get-bluetooth-local-mac-address-in-marshmallow
			bluetoothMAC = EncryptionEngine.safeHash(android.provider.Settings.Secure.getString(appContext.getContentResolver(), "bluetooth_address")); }
		else { //Android before version 6
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();	
			if ( bluetoothAdapter == null || bluetoothAdapter.getAddress() == null ) { bluetoothMAC = ""; }
			else { bluetoothMAC = bluetoothAdapter.getAddress(); }
		}
		TelephonyManager phoneManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
		phoneNumber = phoneManager.getLine1Number();
		if (phoneNumber == null) phoneNumber = "";
	}
	
	public static String getBeiweVersion() { return beiweVersion; }
	public static String getAndroidVersion() { return android.os.Build.VERSION.RELEASE; }
	public static String getProduct() { return android.os.Build.PRODUCT; }
	public static String getBrand() { return android.os.Build.BRAND; }
	public static String getHardwareId() { return android.os.Build.HARDWARE; }
	public static String getManufacturer() { return android.os.Build.MANUFACTURER; }
	public static String getModel() { return android.os.Build.MODEL; }
	public static String getAndroidID() { return EncryptionEngine.safeHash(androidID); }
	public static String getBluetoothMAC() { return EncryptionEngine.safeHash(bluetoothMAC); }
}