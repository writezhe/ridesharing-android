package org.beiwe.app.ui.utils;

import org.beiwe.app.R;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.survey.SurveyType;
import org.beiwe.app.ui.user.AudioRecorderActivity;
import org.beiwe.app.ui.user.SurveyActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**The purpose of this class is to deal with all that has to do with Survey Notifications.
 * This is a STATIC method, and is called from the background process
 * 
 * @author Dor Samet */

public class AppNotifications {

	public static final int recordingCode = 001;
	
	/**
	 * Creates a survey notification that transfers the user to the survey activity. 
	 * 
	 * Note: the notification can only be dismissed through submitting the survey
	 * @param appContext
	 */
	public static void displaySurveyNotification(Context appContext, SurveyType.Type surveyType) {
		Notification surveyNotification = setupNotification(appContext, surveyType.notificationCode, R.drawable.survey_icon, surveyType);
		surveyNotification.flags = Notification.FLAG_ONGOING_EVENT;

		// Get an instance of the notification manager
		NotificationManager notificationManager = 
				(NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// Terrible naming for the method to post a notification
		notificationManager.cancel(surveyType.notificationCode);
		
		notificationManager.notify(
				surveyType.notificationCode, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				surveyNotification);
		
		if (surveyType == SurveyType.Type.DAILY){ PersistentData.setCorrectDailyNotificationState(true); }
		if (surveyType == SurveyType.Type.WEEKLY){ PersistentData.setCorrectWeeklyNotificationState(true); }
	}
	
	/**
	 * Creates a voice recording notification that transfers the user to the audio recording activity
	 * 
	 * Note: the notification can only be dismissed through submitting the survey
	 * @param appContext
	 */
	public static void displayRecordingNotification(Context appContext) {
		Notification recordingNotification = setupNotification(appContext, recordingCode, R.drawable.voice_recording_icon, null);
		recordingNotification.flags = Notification.FLAG_ONGOING_EVENT;
		
		NotificationManager notificationManager = 
				(NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notificationManager.cancel(recordingCode);
		
		notificationManager.notify(
				recordingCode, // If another notification with the same ID pops up, it will be updated. This SHOULD be fine
				recordingNotification);
		PersistentData.setCorrectAudioNotificationState(true);
	}
	
	/**
	 * Used to dismiss the notification corresponding the appCode.
	 * For example, if the user finishes a survey and presses "submit", the app will call appCode "001" to dismiss the notification.
	 * 
	 * @param appContext
	 * @param notifCode
	 */
	public static void dismissNotification(Context appContext, int notifCode) {
		NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notifCode);
	}
	
	/**
	 * Sets up a notification for the user. This uses the given context and one of two notification codes (two static final ints given to this class).
	 * The drawable code is also given for shorter setup of the notification.
	 * 
	 * @param appContext
	 * @param notifCode
	 * @param iconID
	 * @return
	 */
	private static Notification setupNotification(Context appContext, int notifCode, int iconID, SurveyType.Type surveyType) {

		NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
		Intent activityIntent;
		builder.setContentTitle( appContext.getString(R.string.app_name) );
		if ( notifCode == recordingCode ) { // Sets up a voice recording notification
			builder.setContentText( appContext.getResources().getString(R.string.recording_notification_details) );
			builder.setTicker( appContext.getResources().getString(R.string.recording_notification_message) );
	        activityIntent = new Intent( appContext, AudioRecorderActivity.class ); }
	        
		else { // Sets up a survey notification
			builder.setContentText(appContext.getResources().getString( surveyType.notificationDetailsResource) );
			builder.setTicker(appContext.getResources().getString( surveyType.notificationMsgResource ) );
			/* The intent needs an action string that is unique for each survey type, because the
			 * flag PendingIntent.FLAG_UPDATE_CURRENT means that any time a new identical
			 * PendingIntent gets created, it replaces the existing one of the same type. We want a
			 * new Daily Survey PendingIntent to replace an existing PendingIntent, but NOT replace
			 * an existing Weekly Survey PendingIntent. See here: http://stackoverflow.com/a/10538554 */ 
			activityIntent = new Intent(surveyType.dictKey); 
	        activityIntent.setClass( appContext, SurveyActivity.class );
	        activityIntent.putExtra( "SurveyType", surveyType.dictKey );
		}
		
		// add the two icons to be displayed
		builder.setSmallIcon(iconID);
		Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), iconID);
        builder.setLargeIcon(bitmap);

		activityIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
//		Intent.FLAG_ACTIVITY_NEW_TASK
//		Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK

		PendingIntent pendingActivityIntent = PendingIntent.getActivity(
				appContext, // Context - where we are now
				1, // Request code meaning "close the notification once done"
				activityIntent, // The actual intent - where are we going
				PendingIntent.FLAG_CANCEL_CURRENT); // The result should be updated to be the current
		
		/*known problem:
		 * if we use PendingIntent.FLAG_UPDATE_CURRENT the notification will not launch the survey on api 19, there are two known solutions:
		 * use PendingIntent.FLAG_CANCEL_CURRENT
		 * or
		 * add android:exported="true" to the activity's permissions in the Manifest.
		 * source: http://stackoverflow.com/questions/21250364/notification-click-not-launch-the-given-activity-on-nexus-phones
		 */
		
		builder.setContentIntent(pendingActivityIntent);
		
		Notification notification = builder.build();
		return notification;
	}
}