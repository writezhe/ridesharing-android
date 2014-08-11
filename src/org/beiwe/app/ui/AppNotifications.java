package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.survey.AudioRecorderActivity;
import org.beiwe.app.survey.SurveyActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	 * Creates a survey notification that transfers the user to the survey activity. 
	 * 
	 * Note: the notification can only be dismissed through submitting the survey
	 * @param appContext
	 */
	
	public static void displaySurveyNotification(Context appContext) {
		Notification surveyNotification = setupNotification(appContext, surveyCode, R.drawable.survey_icon);
		surveyNotification.flags = Notification.FLAG_ONGOING_EVENT;
		Log.i("SurveyNotification", "Set up intent");

		// Get an instance of the notification manager
		NotificationManager notificationManager = 
				(NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// Terrible naming for the method to post a notification
		Log.i("SurveyNotification", "Notifying...");
		notificationManager.notify(
				surveyCode, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				surveyNotification); 
	}
	
	/**
	 * Creates a voice recording notification that transfers the user to the audio recording activity
	 * 
	 * Note: the notification can only be dismissed through submitting the survey
	 * @param appContext
	 */
	public static void displayRecordingNotification(Context appContext) {
		Notification recordingNotification = setupNotification(appContext, recordingCode, R.drawable.voice_recording_icon);
		recordingNotification.flags = Notification.FLAG_ONGOING_EVENT;
		
		NotificationManager notificationManager = 
				(NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notificationManager.notify(
				recordingCode, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				recordingNotification); 
	}
	
	/**
	 * Used to dismiss the notification corresponding the appCode.
	 * For example, if the user finishes a survey and presses "submit", the app will call appCode "001" to dismiss the notification.
	 * 
	 * @param appContext
	 * @param notifCode
	 */
	public static void dismissNotificatoin(Context appContext, int notifCode) {
		NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notifCode);
	}
	
	/**
	 * Sets up a notification for the user. This uses the given context and one of two notification codes (two static final ints given to this class).
	 * The drawable code is also given for shorter setup of the notification.
	 * 
	 * @param appContext
	 * @param notifCode
	 * @param drawableCode
	 * @return
	 */
	private static Notification setupNotification(Context appContext, int notifCode, int drawableCode) {
		Notification.Builder builder = new Notification.Builder(appContext);
		Intent intent;
		builder.setContentTitle("Beiwe");
		if (notifCode == surveyCode) { // Sets up a survey notification
			builder.setContentText(appContext.getResources().getString(R.string.survey_notification_details));
			builder.setTicker(appContext.getResources().getString(R.string.survey_notification_message));
	        intent = new Intent(appContext, SurveyActivity.class);
		} else { // Sets up a voice recording notification
			builder.setContentText(appContext.getResources().getString(R.string.recording_notification_details));
			builder.setTicker(appContext.getResources().getString(R.string.recording_notification_message));
	        intent = new Intent(appContext, AudioRecorderActivity.class);
		}
		// Sets up the two icons to be displayed
		builder.setSmallIcon(drawableCode);
		Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), drawableCode);
        builder.setLargeIcon(bitmap);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(
				appContext, // Context - where we are now
				1, // Request code meaning "close the notification once done"
				intent, // The actual intent - where are we going
				PendingIntent.FLAG_UPDATE_CURRENT); // The result should be updated to be the current

		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		return notification;
	}
}