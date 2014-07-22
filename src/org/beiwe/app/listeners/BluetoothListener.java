package org.beiwe.app.listeners;

import org.beiwe.app.storage.TextFileManager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


//http://code.tutsplus.com/tutorials/android-quick-look-bluetoothadapter--mobile-7813

/* Tests.
4.4.2, nexus 7 tablet
	The UI does not allow toggling bluetooth on and off quickly.  It waits for the turning on/off state to finish.
	There is about a ... half second? lag between the turning on/off state broadcast and the actually on/off broadcast.     

*/

/** BluetoothListener
 * 
 * @author elijones
 *
 */
 
public class BluetoothListener extends BroadcastReceiver {
	private BluetoothAdapter bluetoothAdapter;
	private Boolean exists;
	
	private Boolean external_state = null;
	//internal state is true/false "have we requested that bluetooth be turned on
	private Boolean state_we_want = null;
	
	private TextFileManager bluetoothLog = TextFileManager.getBluetoothLogFile();
	private TextFileManager debugLog = TextFileManager.getDebugLogFile();
	
	public Boolean bluetoothEnabled() { return bluetoothAdapter.isEnabled(); }
	
	public BluetoothListener(){
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//returns null if bluetooth DNE
		if ( bluetoothAdapter == null ) { exists = false; }
		else { exists = true; }
		
		//set the previous state variable to the state the device was in on instantiation.
		//at instantiation we want to agree with the external state. 
		this.external_state = this.bluetoothEnabled();
		this.state_we_want = this.external_state;
	}
	
	//TODO: add check for devices connected, stop disable process if any devices are connected.
	public Boolean disable() {
		if (!exists) return false; //esc
		
		state_we_want = false;
		if (external_state == false){ //if the outside world and us agree that it should be off, turn it off
			this.bluetoothAdapter.disable();
			return true; }
		else { return false; }
	}
	
	//crap, the user can't... turn it on when it is already on.
	public Boolean enable() {
		if (!exists) return false;
		state_we_want = true;
		if ( !external_state ){  //if we want it on and the external world wants it off, turn it on. (we retain state) 
			this.bluetoothAdapter.enable();
			return true; }
		return false;
	}
	
	@Override
	//TODO: If android allows toggling bluetooth on-off quickly, add logic to on and off state checking external state for correctness.
	public synchronized void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if ( action.equals( BluetoothAdapter.ACTION_STATE_CHANGED ) ) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			
			if ( state == BluetoothAdapter.ERROR) { Log.i("bluetooth", "BLUETOOTH ADAPTOR ERROR?"); }
			
			else if ( state == BluetoothAdapter.STATE_OFF ) { Log.i("bluetooth", "state change: off"); }
			
			else if ( state == BluetoothAdapter.STATE_ON ) { Log.i("bluetooth", "state change: on"); }
			
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
			
		}
	}

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
	}
}
