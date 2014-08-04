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

https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
If you want to declare that your app is available to BLE-capable devices only, include the following in your app's manifest:
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>

*/

/** BluetoothListener
 * @author elijones */
 
public class BluetoothListener extends BroadcastReceiver {
	//Base
	private BluetoothAdapter bluetoothAdapter;
	private Boolean exists;
	
	private static Boolean want_bluetooth_scan = false;
	public static Boolean get_scan_thing() { return want_bluetooth_scan; }
	public static void set_scan_thing(Boolean value) { want_bluetooth_scan = value; }
	
	//Stateful variables
	private Boolean external_state = null;
	private Boolean state_we_want = null;
	//Log file
	private TextFileManager bluetoothLog = null;
	private TextFileManager debugLog = null;
	
	public Boolean bluetoothEnabled() { 
		if (exists) { return bluetoothAdapter.isEnabled(); } 
		else { return false; } }
	
	
	/**BluetoothListener
	 * The BluetoothListener needs to gracefully handle existence issues.  We only want devices
	 * with Bluetooth 4.0 (BLE) to ever run our code, so if we encounter a version of Android before 
	 * BLE, we automatically "disable" the sensor code. */
	//FIXME: need to ACTUALLY CHECK for bluetooth 4.0
	public BluetoothListener() {
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
	    	this.exists = false; 
			return; }
	    //Thankfully the BluetoothAdaptor itself returns a simple null if it DNE
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ( bluetoothAdapter == null ) {
			this.exists = false; 
			return; }
		else { this.exists = true; }
		//set the previous state variable to the state the device was in on instantiation.
		//at instantiation we want to agree with the external state. 
		this.external_state = this.bluetoothEnabled();
		this.state_we_want = this.external_state;
		
//		set_scan_thing(false);
//		this.want_bluetooth_scan = false;
		
		this.bluetoothLog = TextFileManager.getBluetoothLogFile();
		this.debugLog = TextFileManager.getDebugLogFile();
	}
	
	
	/** Intelligently disables the bluetooth adaptor.
	 * @return True if bluetooth exists, false if bluetooth does not exist */
	//TODO: add check for devices connected, stop disable process if any devices are connected.
	private Boolean disableBluetooth() {
		if (!exists) return false; //esc
		state_we_want = false;
		if ( !external_state ) { //if the outside world and us agree that it should be off, turn it off
			this.bluetoothAdapter.disable();
			return true; }
		return false;
	}
	
	/** Intelligently enables the bluetooth adaptor. 
	 * @return True if bluetooth exists, false if bluetooth does not exist. */
	private Boolean enableBluetooth() {
		if (!exists) return false;
		state_we_want = true;
		
		if ( !external_state ){  //if we want it on and the external world wants it off, turn it on. (we retain state) 
			this.bluetoothAdapter.enable();
			return true; }
		return false;
	}
	
	
	/** Intelligently starts a bluetooth LE scan. */
	//TODO: is this code actually finished?
	@SuppressLint("NewApi")
	public void enableBLEScan(){
		Log.i("bluetooth", "enabling a scan, current mode: " + bluetoothAdapter.getScanMode() );
		
		if (!exists) { 
			Log.i("bluetooth", "requested BLE scan, but bluetooth does not exist");
			return; }
		
		// set the scan variable, enable the bluetooth
		//want_bluetooth_scan = true;		// We want a bluetooth scan, so set that to true
		set_scan_thing(true);
		Log.i("bluetooth", "want_bluetooth_scan set to true: " + want_bluetooth_scan.hashCode() );

		if ( bluetoothEnabled() ) {		//If bluetooth is already on, start the scan immediately.
			startScanning();
			return; }
		
		// Otherwise, enable the bluetooth.  This does not happen immediately, but the want_bluetooth_scan
		// variable has been set to true.  This modifies behavior of the OnReceive function below.
		enableBluetooth();
	}
	
	/** Stops the scan, sets the want_bluetooth_scan to zero.
	 *  Intelligently disables bluetooth.  */
	@SuppressLint("NewApi")
	public void disableBLEScan() {
//		want_bluetooth_scan = false;
		set_scan_thing(false);
		bluetoothAdapter.stopLeScan(bluetoothCallback);
		this.disableBluetooth();
		Log.i("bluetooth", "stopped scan maybe?"); }
	
	
	/** Intelligently STARTS a BLE scan.
	 * 	PRIVATE because this is actually a convenience function, it may be removed.
	 *  If a scan can start (if bluetooth is on), start scanning.
	 *  Print some verbose log statements. */
	@SuppressLint("NewApi")
	private void startScanning() {
	//If the bluetooth is actually
		Log.i("bluetooth", "starting a scan: " + want_bluetooth_scan );
		if ( bluetoothEnabled() ){
			if ( bluetoothAdapter.startLeScan(bluetoothCallback) ) {
				Log.i("bluetooth", "bluetooth LE scan started successfully."); }
			else { Log.i("bluetooth", "bluetooth LE scan NOT started successfully."); } }
		else { Log.i("bluetooth", "bluetooth was not enabled."); } }

	/** LeScanCallback is the code that is run when a bluetooth device is sensed by a
	 * Bluetooth LE scan. */
	//TODO: currently recording everything, log useful data, format it etc.
	@SuppressLint("NewApi")
	private LeScanCallback bluetoothCallback = new LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String data = new String( device.toString() + "\n"
					+ "rssi: " + rssi
					+ "scanRecord: " + scanRecord );
			Log.i("bluetooth", data);
			debugLog.write(data);
		} }; 

	
		
/*####################################################################################
################# the onReceive Stack for bluetooth state messages ###################
####################################################################################*/
	
	@Override
	//TODO: If android allows toggling bluetooth on-off quickly, add logic to on and off state checking external state for correctness.
	// SEE COMMENT AT TOP.
	public synchronized void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if ( action.equals( BluetoothAdapter.ACTION_STATE_CHANGED ) ) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			
			if ( state == BluetoothAdapter.ERROR) { Log.i("bluetooth", "BLUETOOTH ADAPTOR ERROR?"); }
			
			else if ( state == BluetoothAdapter.STATE_OFF ) { Log.i("bluetooth", "state change: off"); }
			
			else if ( state == BluetoothAdapter.STATE_ON ) {
				Log.i("bluetooth", "state change: on" );
				Log.i("bluetooth", "want_bluetooth_scan = " + want_bluetooth_scan.hashCode() );
				if ( want_bluetooth_scan ) { startScanning(); }
				else { Log.i("something", "anything"); } }
				//when we are waiting for bluetooth to turn on so we can scan, this triggers a scan
			
			else if ( state == BluetoothAdapter.STATE_TURNING_ON ) {
				Log.i("bluetooth", "state change: turning on");
				if (!this.state_we_want){  //if we want the state off, then we did not turn it on.
					external_state = true; //therefore, the request must originate with the external world, so set external state to "on".
				} }
			
			else if ( state == BluetoothAdapter.STATE_TURNING_OFF ) {
				Log.i("bluetooth", "state change: turning off");
				if (this.state_we_want){ //if we want the state on, then we did not turn it off.
					external_state = false; //therefore the request mut originate with the external world, set external state to "off"
				} }
			
			else { Log.i("bluetooth", "unknown state received: " + intent.toString() ); }	
		} }
	
/*###############################################################################
########################## Probably Debug? ######################################
###############################################################################*/
	
	public String getState() {
		if (!exists) return "does not exist.";
//		STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF
		int state = bluetoothAdapter.getState();
		if ( state == BluetoothAdapter.STATE_OFF ) return "off";
		if ( state == BluetoothAdapter.STATE_TURNING_ON ) return "turning on";
		if ( state == BluetoothAdapter.STATE_ON) return "on";
		if ( state == BluetoothAdapter.STATE_TURNING_OFF ) return "turning off";
		return "getstate is broken, state was " + state;
	}
	
	public void bluetoothInfo() {
		Log.i("bluetooth", "bluetooth existence: " + exists.toString() );
		Log.i("bluetooth", "bluetooth enabled: " + this.bluetoothEnabled() );
		Log.i("bluetooth", "bluetooth address: " + bluetoothAdapter.getAddress() );
		Log.i("bluetooth", "bluetooth state: " + this.getState() );
		Log.i("bluetooth", "bluetooth scan mode: " + this.bluetoothAdapter.getScanMode() );
		Log.i("bluetooth", "bluetooth bonded devices:" +bluetoothAdapter.getBondedDevices() );
	}
}
