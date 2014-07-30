package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class Timer {
	Context appContext;
	PendingIntent pendingIntent;
	BroadcastReceiver broReceiver;
	AlarmManager alarmManager;
	BackgroundProcess backgroundProcess;
	
	
	public Timer(BackgroundProcess backgroundProcess) {
		this.backgroundProcess = backgroundProcess;
		appContext = backgroundProcess.getApplicationContext();
	}
	
	/*
	 * Actions in Android are saved as values in locations. For example ACTION_CALL is actually saved as android.intent.action.CALL.
	 * When using an IntentFilter as an "action filter", we will use this convention, and start the timer using the action 
	 * "org.beiwe.app.START_TIMER". This will be registered in the background process, and will start the PendingIntent 
	 * (an intent waiting for something to call it) in the timer.
	 */
	public void alarmSetup(Integer seconds) {
		seconds *= 1000;
		broReceiver = broadcastReceiverCreator(seconds);
		Intent intent = new Intent();
		intent.putExtra("customTimerFilter", true);
		intent.setAction("org.beiwe.app");
		
		Log.i("Timer Object", intent.toString() );
		Log.i("Timer Object", intent.getAction() );
		
		backgroundProcess.registerReceiver( broReceiver, new IntentFilter( intent.getAction() ) );
		
//		pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, 0);
		// These two lines actually wake up the broadcast receiver to check for incoming intents
		// http://developer.android.com/reference/android/app/AlarmManager.html#ELAPSED_REALTIME
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService(Context.ALARM_SERVICE ));
		alarmManager.set( (int) SystemClock.elapsedRealtime(), SystemClock.elapsedRealtime() + seconds, pendingIntent);
		Log.i("alarm manager", "alarm started");
	}
	
	public BroadcastReceiver broadcastReceiverCreator(final int milliseconds) {		
		return new BroadcastReceiver() {
		
			@Override
			public void onReceive(Context appContext, Intent intent) {
				Toast.makeText(appContext, "Receive broadcast, reseting timer...", 5);
				Log.i("Timer", "toast! toast is broken");
				// This causes the alarm manager to register another alarm for elapsedRealtime() milliseconds later.
				alarmManager.set((int) SystemClock.elapsedRealtime(), SystemClock.elapsedRealtime() + milliseconds, pendingIntent);
				// Double check if SystemClock.elapsedRealtime works if the phone is asleep
				// Phone is asleep if there is no app holding it active, ergo, if the backgroundservice is active, 
				// phone is NOT asleep (even if screen is turned off).
				
				Intent newIntent = new Intent();
				newIntent.setAction("Example");
				
				appContext.sendBroadcast(newIntent);
			}
		};
	}
}
