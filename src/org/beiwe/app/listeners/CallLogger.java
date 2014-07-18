package org.beiwe.app.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallLogger extends BroadcastReceiver {

	@Override
	public void onReceive(Context appContext, Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
//	 Uri allCalls = Uri.parse("content://call_log/calls");
//	 Cursor c = managedQuery(allCalls, null, null, null, null);
	/*
	 * TODO: 
	 * 		Saved variables:
	 * 		- Last date of logged call
	 * 			
	 * 		Flow is this:
	 * 		1. User ends a call (waiting for a phone state IDLE after a another phone state)
	 * 		2. Logger runs to check if there are any new calls that should be logged in our file
	 *		3. Logger searches logs all new entries
	 *		4. Logger saves the current date as a mark
	 *		5. Repeat
	 * 
	 */
	

}
