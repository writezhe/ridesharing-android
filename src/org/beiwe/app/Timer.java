package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class Timer {
	Context appContext;
	BroadcastReceiver broadcastReceiver;
	AlarmManager alarmManager;
	BackgroundProcess backgroundProcess;
	
	private Intent example;
	public static String EXAMPLE_ACTION = "Example";
	
	public IntentFilter getExampleIntentFilter() { return new IntentFilter( example.getAction() ); 	}	
//	public void sendExampleBroadcast(){ appContext.sendBroadcast(example); }
	public Intent getExample() { return example; }	

	
	public Timer(BackgroundProcess backgroundProcess) {
		this.backgroundProcess = backgroundProcess;
		appContext = backgroundProcess.getApplicationContext();
		example = setupExampleIntent();
	}
	
	private Intent setupExampleIntent(){
		Intent newIntent = new Intent();
		newIntent.setAction(EXAMPLE_ACTION);
		return newIntent;	}
	
	/*
	 * Actions in Android are saved as values in locations. For example ACTION_CALL is actually saved as android.intent.action.CALL.
	 * When using an IntentFilter as an "action filter", we will use this convention, and start the timer using the action 
	 * "org.beiwe.app.START_TIMER". This will be registered in the background process, and will start the PendingIntent 
	 * (an intent waiting for something to call it) in the timer.
	 */
	public void setupRepeatngAlarm(int milliseconds, Intent customIntent) {
		
		//create the intent, and then the pending intent from it		
		Intent eliIntentSteve = new Intent();
		eliIntentSteve.setAction("org.beiwe.app.steve"); // Eli's intent
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, eliIntentSteve, 0);
		//create and then register the broadcastreceiver with the background process
		broadcastReceiver = broadcastReceiverCreator(milliseconds, pendingIntent, customIntent);
		backgroundProcess.registerReceiver( broadcastReceiver, new IntentFilter( eliIntentSteve.getAction() ) );
		
		//and then start the alarm manager to actually trigger that intent it X seconds
		
		// These two lines actually wake up the broadcast receiver to check for incoming intents
		// http://developer.android.com/reference/android/app/AlarmManager.html#ELAPSED_REALTIME
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService(Context.ALARM_SERVICE ));
		long nextTriggerTime = System.currentTimeMillis() + milliseconds;
		alarmManager.set( AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingIntent); 
		Log.i("alarm manager", "alarm started");
	}
	
	
	
	
	public BroadcastReceiver broadcastReceiverCreator(final int milliseconds, final PendingIntent pendingIntent, final Intent customIntent) {		
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context appContext, Intent intent) {
				Toast.makeText(appContext, "Receive broadcast, reseting timer...", Toast.LENGTH_SHORT).show();
				backgroundProcess.unregisterReceiver(this);
				
				appContext.sendBroadcast( customIntent );
				
				setupRepeatngAlarm( milliseconds , customIntent); }
		}; }
}
