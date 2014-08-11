package org.beiwe.app.ui;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * The purpose of this class is to deal with all that has to do with Survey Notifications.
 * This is a STATIC method, and is called from the background process
 * 
 * @author Dor Samet
 *
 */

public class AppNotifications {

	public static final int surveyCode = 001;
	public static final int recordingCode = 002;
	
	/**
	 * Creates a notification, and displays it to the user. When clicking the notification, the user
	 * is taken to a new survey
	 * @param context
	 */
	
	public static void displaySurveyNotification(Context context) {
		// Notification setup
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle("Beiwe");
		builder.setContentText("There is a new survey ready for you to take");
		builder.setTicker("Take survey");
		builder.setSmallIcon(R.drawable.survey_icon);

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
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		Log.i("SurveyNotification", "Set up intent");

		// Get an instance of the notification manager
		NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Terrible naming for the method to post a notification
		Log.i("SurveyNotification", "Notifying...");
		notificationManager.notify(
				surveyCode, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				notification); 
	}
	
	/**
	 * Given context, this method should be activated when Bluetooth is activated, to notify the user that
	 * everything is under control.
	 * @param context
	 */
	public static void displayBluetoothNotfication(Context context) {
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle("Beiwe");
		builder.setContentText("Bluetooth has been activated by Beiwe, " +
				"and will be closed in 15 minutes Automatically. Click here to dismiss");
		builder.setTicker("Bluetooth has been activated");
		builder.setSmallIcon(R.drawable.ic_launcher);
		
		Notification notification = builder.build();
		
		NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(
				recordingCode, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				notification); 
	}
	
	public static void dismissNotificatoin(Context context, int appCode) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(appCode);
	}
}