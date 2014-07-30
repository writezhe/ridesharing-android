package org.beiwe.app.ui;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * The purpose of this class is to deal with all that has to do with Survey Notifications.
 * This is a STATIC method, and is called from the background process
 * 
 * @author Dor Samet
 *
 */

public class SurveyNotification {

	/**
	 * Creates a notification, and displays it to the user. When clicking the notification, the user
	 * is taken to a new survey
	 * @param context
	 */
	
	public static void displayNotification(Context context) {
		// Notification setup
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle("Beiwe");
		builder.setContentText("There is a new survey ready for you to take");
		builder.setTicker("Take survey");
		builder.setSmallIcon(R.drawable.ic_launcher);

		Log.i("SurveyNotification", "Notification built");
		// The intent that will be passed when clicking the activity
		// send to the survey activity instead of DebugActivity

		// TODO: Change to Survey Activity once we have it..
		Intent intent = new Intent(context, DebugInterfaceActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// When the notification is clicked, the user will be sent to the survey activity (For now in the DebugInterfaceActivity)
		PendingIntent pendingIntent = PendingIntent.getActivity(
				context, // Context - where we are now
				0, // Request code meaning "close the notification once done"
				intent, // The actual intent - where are we going
				PendingIntent.FLAG_UPDATE_CURRENT); // The result should be updated to be the current

		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		Log.i("SurveyNotification", "Set up intent");
		notification.flags |= notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

		// Get an instance of the notification manager
		NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Terrible naming for the method to post a notification
		Log.i("SurveyNotification", "Notifying...");
		notificationManager.notify(
				001, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				notification); 
	}
}