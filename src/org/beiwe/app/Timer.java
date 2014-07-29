package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
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
	public void regsiterBroadcastReceiver(Integer seconds) {
		seconds *= 1000;
		broReceiver = broadcastReceiverCreator(seconds);
		
		backgroundProcess.registerReceiver(broReceiver, new IntentFilter("org.beiwe.app.START_TIMER")); 
		pendingIntent = PendingIntent.getBroadcast(appContext, 0, new Intent("org.beiwe.app.START_TIMER"), 0);
		
		// These two lines actually wake up the broadcast receiver to check for incoming intents
		// http://developer.android.com/reference/android/app/AlarmManager.html#ELAPSED_REALTIME
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService(Context.ALARM_SERVICE ));
		alarmManager.set( (int) SystemClock.elapsedRealtime(), SystemClock.elapsedRealtime() + seconds, pendingIntent );
	}
	
	public BroadcastReceiver broadcastReceiverCreator(final int milliseconds) {		
		return new BroadcastReceiver() {
		
			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, "Receive broadcast, reseting timer...", 5);

				alarmManager.set((int) SystemClock.elapsedRealtime(), SystemClock.elapsedRealtime() + milliseconds, pendingIntent);
				// Double check if SystemClock.elapsedRealtime works if the phone is asleep
				// Phone is asleep if there is no app holding it active, ergo, if the backgroundservice is active, 
				// phone is NOT asleep (even if screen is turned off).
			}
		};
	}
}
