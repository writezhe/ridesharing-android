package org.beiwe.app;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.provider.Settings;
//IMEI: phone SIM identifier.
//android uuid

//needs to grab data on first device run, and use that to construct a unique identifying  blob to send to the server.
//this blob will uniquely identify the advice, and assist in pushing the user to the correct actions if they
//have a new phone, if they have a new phone number, etc.  all identifying data must be hashed.
//this blob may need to be queryable from the server, it should be unique to each user.

public class DeviceInfo {
	private static String androidID;
	private static String bluetoothMAC;
	
	/** grab the Android ID and the Bluetooth's MAC address */
	public DeviceInfo(Context appContext) {
		androidID = Settings.Secure.getString( appContext.getContentResolver(), Settings.Secure.ANDROID_ID );

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		/* If the BluetoothAdapter is null, or if the BluetoothAdapter.getAddress() returns null 
		 * (this does happen sometimes!), record an empty string for the Bluetooth Mac address. */
		if ( bluetoothAdapter == null || bluetoothAdapter.getAddress() == null ) { bluetoothMAC = ""; }
		else { bluetoothMAC = bluetoothAdapter.getAddress(); }
	}

	public static String getAndroidID() {
		return androidID;
	}
	
	public static String getBlootoothMAC() {
		return bluetoothMAC;
	}
}