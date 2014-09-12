package org.beiwe.app.listeners;

import org.beiwe.app.storage.TextFileManager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

//http://code.tutsplus.com/tutorials/android-quick-look-bluetoothadapter--mobile-7813

/* Tests.
4.4.2, nexus 7 tablet
	The UI does not allow toggling bluetooth on and off quickly.  It waits for the turning on/off state to finish.
	There is about a ... half second? lag between the turning on/off state broadcast and the actually on/off broadcast.     

LG G2 does not interrupt the whole process of turning off and turning on :) There is a lag of about a half a second in
between phases

https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
If you want to declare that your app is available to BLE-capable devices only, include the following in your app's manifest:
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>

*/

/** BluetoothListener
 * The BluetoothListener handles the location of nearby patients in the study, but is limited by
 * the way Android handles Bluetooth interactions.
 * 
 * BluetoothListener keeps track of the state of the device's Bluetooth Adaptor, and will
 * intelligently enable/disable Bluetooth as needed.  It only enables Bluetooth in order to make
 * a Bluetooth Low Energy scan and record any Bluetooth MAC addresses that show up, and then will
 * disable Bluetooth.  If the Bluetooth adaptor was already enabled it will not turn Bluetooth off.
 * 
 * @author elijones */

public class BluetoothListener extends BroadcastReceiver {
	public static String header = "timestamp, rssi";
	//Base
	private BluetoothAdapter bluetoothAdapter;
	
	//bluetoothExists can be set to false if the device does not meet our needs.
	private Boolean bluetoothExists;
	private static Boolean scanActive = false;
	
	//Stateful variables
	private Boolean externalBluetoothState;
	private Boolean internalBluetoothState;
	//Log file
	private TextFileManager bluetoothLog;
	
	public Boolean isBluetoothEnabled() {
		if ( bluetoothExists ) { return bluetoothAdapter.isEnabled(); }
		else { return false; } }
	
	
	/**The BluetoothListener needs to gracefully handle existence issues - we only want devices
	 * with Bluetooth Low Energy to ever run our code. BLE/Bluetooth 4.0 was introduced in JELLY_BEAN_MR2,
	 * so we check for that too, and we check that ANY bluetooth device exists. */
	public BluetoothListener() {
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//We have to check if the BluetoothAdaptor is null, or if the device is not running api 18+  
		if ( bluetoothAdapter == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 ) {
			this.bluetoothExists = false;
			return; }
		else { bluetoothExists = true; }
		
		//set the external state variable to the state the device was in on instantiation,
		//and set the internernal state variable to be the same.
		this.externalBluetoothState = this.isBluetoothEnabled();
		this.internalBluetoothState = this.externalBluetoothState;
		
		this.bluetoothLog = TextFileManager.getBluetoothLogFile();
//		Log.i("BluetoothListener", "bluetooth constructor finished");
	}
	
	
	/** Intelligently disables the bluetooth adaptor.
	 * @return True if bluetooth exists, false if bluetooth does not exist */
	private Boolean disableBluetooth() {
		if (!bluetoothExists) { return false; }
		Log.i("BluetoothListener", "disable bluetooth.");
		internalBluetoothState = false;
		if ( bluetoothAdapter.getBondedDevices().isEmpty() ) {
			Log.i("BluetoothListener", "found a bonded bluetooth device, will not be turning off bluetooth.");
			externalBluetoothState = true; }
		
		if ( !externalBluetoothState ) { //if the outside world and us agree that it should be off, turn it off
			this.bluetoothAdapter.disable();
			return true; }
		return false;
	}
	
	
	/** Intelligently enables the bluetooth adaptor. 
	 * @return True if bluetooth exists, false if bluetooth does not exist. */
	private Boolean enableBluetooth() {
		if (!bluetoothExists) { return false; }
		Log.i("BluetoothListener", "enable bluetooth.");
		internalBluetoothState = true;
		if ( !externalBluetoothState ){  //if we want it on and the external world wants it off, turn it on. (we retain state) 
			this.bluetoothAdapter.enable();
			return true; }
		return false;
	}
	
	
	/** Intelligently starts a bluetooth LE scan.
	 * Sets the scanActive variable to true, then checks if bluetooth is already on.
	 * If Bluetooth is already on start the scan, otherwise depend on the Bluetooth
	 * State Change On broadcast.  This can take a few seconds. */
	@SuppressLint("NewApi")
	public void enableBLEScan(){
		if (!bluetoothExists) { return; }
		Log.i("BluetoothListener", "enable BLE scan.");
		// set the scan variable, enable Bluetooth.
		scanActive = true;
		if ( isBluetoothEnabled() ) { tryScanning(); }
		else { enableBluetooth(); }
		bluetoothLog.newFile();
	}
	
	/** Intelligently disables bluetooth.
	 * Sets the scanActive variable to false, and stops any running Bluetooth LE scan,
	 * then disable Bluetooth (intelligently).
	 * Note: we cannot actually guarantee the scan has stopped, that function returns void. */
	@SuppressLint("NewApi")
	public void disableBLEScan() {
		if (!bluetoothExists) { return; }
		Log.i("BluetoothListener", "disable BLE scan.");
		scanActive = false;
		bluetoothAdapter.stopLeScan(bluetoothCallback);
		this.disableBluetooth(); 
	}
	
	
	/** Intelligently STARTS a Bluetooth LE scan.
	 *  If Bluetooth is available, start scanning.  Makes verbose logging statements */
	@SuppressLint("NewApi")
	private void tryScanning() {
	//If the bluetooth is actually
		Log.i("bluetooth", "starting a scan: " + scanActive );
		if ( isBluetoothEnabled() ) {
			if ( bluetoothAdapter.startLeScan(bluetoothCallback) ) {
				Log.i("bluetooth", "bluetooth LE scan started successfully."); }
			else { Log.i("bluetooth", "bluetooth LE scan NOT started successfully."); } }
		else { Log.i("bluetooth", "bluetooth was not enabled."); } }

	
	/** LeScanCallback is code that is run when a Bluetooth LE scan returns some data.
	*   We take the returned data and log it. */
	//TODO: Eli.  Currently recording everything, log useful data, format it etc.
	@SuppressLint("NewApi")
	private LeScanCallback bluetoothCallback = new LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Long time = System.currentTimeMillis();
			
			String data = new String( "BLUETOOTH LE SCAN DATA: "
					+ device.toString() + "\n"
					+ "rssi: " + rssi + ", "
					+ "scanRecord: " + scanRecord );
			Log.i("bluetooth", data);
			
			bluetoothLog.write( "" + time + "," + device.toString() );
		} }; 

		
/*####################################################################################
################# the onReceive Stack for bluetooth state messages ###################
####################################################################################*/
	
	@Override
	//TODO: Everyone. If android allows toggling bluetooth on-off quickly, add logic to on and off state checking external state for correctness.
	// SEE COMMENT AT TOP.
	/** The onReceive method for the BluetoothListener listens for Bluetooth State changes.
	 * The Bluetooth adaptor can be in any of 4 states: on, off, turning on, and turning off. 
	 * Whenever the turning on or off state comes in, we update the externalBluetoothState variable
	 * so that we never turn Bluetooth off when the user wants it on.
	 * Additionally, if a Bluetooth On notification comes in AND the scanActive variable is set to TRUE
	 * we start a Bluetooth LE scan. */
	public synchronized void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if ( action.equals( BluetoothAdapter.ACTION_STATE_CHANGED ) ) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			
			if ( state == BluetoothAdapter.ERROR) { Log.i("bluetooth", "BLUETOOTH ADAPTOR ERROR?"); }
			
			else if ( state == BluetoothAdapter.STATE_OFF ) { Log.i("bluetooth", "state change: off"); }
			
			else if ( state == BluetoothAdapter.STATE_ON ) {
				Log.i("bluetooth", "state change: on" );
				if ( scanActive ) { enableBLEScan(); } }
			
			else if ( state == BluetoothAdapter.STATE_TURNING_ON ) {
				Log.i("bluetooth", "state change: turning on");
				if (!this.internalBluetoothState){	externalBluetoothState = true; } }
			
			else if ( state == BluetoothAdapter.STATE_TURNING_OFF ) {
				Log.i("bluetooth", "state change: turning off");
				if (this.internalBluetoothState){ externalBluetoothState = false; } }			
		} }
	
/*###############################################################################
########################## Probably Debug? ######################################
###############################################################################*/
	
	public String getState() {
		if (!bluetoothExists) return "does not exist.";
//		STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF
		int state = bluetoothAdapter.getState();
		if ( state == BluetoothAdapter.STATE_OFF ) return "off";
		if ( state == BluetoothAdapter.STATE_TURNING_ON ) return "turning on";
		if ( state == BluetoothAdapter.STATE_ON) return "on";
		if ( state == BluetoothAdapter.STATE_TURNING_OFF ) return "turning off";
		return "getstate is broken, value was " + state;
	}
	
	public void bluetoothInfo() {
		Log.i("bluetooth", "bluetooth existence: " + bluetoothExists.toString() );
		Log.i("bluetooth", "bluetooth enabled: " + this.isBluetoothEnabled() );
		Log.i("bluetooth", "bluetooth address: " + bluetoothAdapter.getAddress() );
		Log.i("bluetooth", "bluetooth state: " + this.getState() );
		Log.i("bluetooth", "bluetooth scan mode: " + this.bluetoothAdapter.getScanMode() );
		Log.i("bluetooth", "bluetooth bonded devices:" +bluetoothAdapter.getBondedDevices() );
	}
}