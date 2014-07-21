package org.beiwe.app.listeners;

import org.beiwe.app.storage.TextFileManager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


//http://code.tutsplus.com/tutorials/android-quick-look-bluetoothadapter--mobile-7813

public class Bluetooth extends BroadcastReceiver {
	private BluetoothAdapter bluetoothAdapter;
	private Boolean exists;
	
	private Boolean external_state = null;
	//internal state is true/false "have we requested that bluetooth be turned on
	private Boolean state_we_want = null;
	
	private TextFileManager bluetoothLog = TextFileManager.getBluetoothLogFile();
	private TextFileManager debugLog = TextFileManager.getDebugLogFile();
	
	public Bluetooth(){
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//returns null if bluetooth DNE
		if ( bluetoothAdapter == null ) { exists = false; }
		else { exists = true; }
		
		//set the previous state variable to the state the device was in on instantiation.
		this.external_state = this.enabled();
	}
	
	public Boolean disable() {
		if (!exists) return false; //esc
		
		state_we_want = false;
		if (external_state == false){ //if the outside world and us agree that it should be off, turn it off
			this.bluetoothAdapter.disable();
			return true;
		}
		else{
			return false;
		}
	}
	//crap, the user can't... turn it on when it is already on.
	public Boolean enable() {
		if (!exists) return false;
		state_we_want = true;
		if ( !external_state ){  //if we want it on and the external world wants it off, turn it on. (we retain state) 
			this.bluetoothAdapter.enable();
			return true;
		}
		return false;
	}
	
	public Boolean exists() { return exists; }
	
	public Boolean enabled() { return bluetoothAdapter.isEnabled(); }
	
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
		Log.i("bluetooth", "bluetooth enabled: " + this.enabled() ) ; 
		Log.i("bluetooth", "bluetooth address: " + bluetoothAdapter.getAddress() );
		Log.i("bluetooth", "bluetooth state: " + this.getState() );
	}
	
	@Override
	public synchronized void onReceive(Context arg0, Intent arg1) {
		
		
		if ( arg1.getAction() == BluetoothAdapter.ACTION_STATE_CHANGED ) {
			int state = arg1.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			
			if ( state == BluetoothAdapter.STATE_OFF ) {
				
			}
			
			else if ( state == BluetoothAdapter.STATE_ON ) {
				
			}
			
			else if ( state == BluetoothAdapter.STATE_TURNING_ON ) {
				if (!this.state_we_want){  //if we want the state off, then we did not turn it on.
					external_state = true; //therefore, the request must originate with the external world, so set external state to "on".
				}
			}
			
			else if ( state == BluetoothAdapter.STATE_TURNING_OFF ) {
				if (this.state_we_want){ //if we want the state on, then we did not turn it off.
					external_state = false; //therefore the request mut originate with the external world, set external state to "off"
				}
			}
			
			else { Log.i("bluetooth", "unknown state received: " +arg1.toString() ); }
		}
	}
	
	//Failure modes...
	//if a state change is received out of order
}
