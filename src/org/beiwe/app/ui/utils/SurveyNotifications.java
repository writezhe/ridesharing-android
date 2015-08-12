package org.beiwe.app.ui.utils;

import org.beiwe.app.R;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.survey.AudioRecorderActivity;
import org.beiwe.app.survey.SurveyActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**The purpose of this class is to deal with all that has to do with Survey Notifications.
 * This is a STATIC method, and is called from the background process
 * 
 * @author Eli Jones */
//TODO: Low priority: Eli. Redoc.
public class SurveyNotifications {
	/**Creates a survey notification that transfers the user to the survey activity. 
	 * Note: the notification can only be dismissed through submitting the survey
	 * @param appContext */
	public static void displaySurveyNotification(Context appContext, String surveyId) {
		//activityIntent contains information on the action triggered by tapping the notification. 
		Intent activityIntent;		
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(appContext);
		notificationBuilder.setContentTitle( appContext.getString(R.string.app_name) );
		//TODO: Eli. make sure we have a consistent use of these survey type identifiers across both codebases
		if ( PersistentData.getSurveyType(surveyId).equals("android_survey" ) ) {
			activityIntent = new Intent(appContext, SurveyActivity.class);
			activityIntent.setAction("org.beiwe.app.start_android_survey"); //todo: put in strings
			notificationBuilder.setTicker( appContext.getResources().getString(R.string.new_android_survey_notification_ticker) );
			notificationBuilder.setContentText( appContext.getResources().getString(R.string.new_android_survey_notification_details) );
			notificationBuilder.setSmallIcon(R.drawable.survey_icon);
			notificationBuilder.setLargeIcon( BitmapFactory.decodeResource(appContext.getResources(), R.drawable.survey_icon ) );
		}
		else if ( PersistentData.getSurveyType(surveyId).equals("audio_survey" ) ) {
			activityIntent = new Intent(appContext, AudioRecorderActivity.class);
			activityIntent.setAction("org.beiwe.app.start_audio_survey");  //todo: put in strings
			notificationBuilder.setTicker( appContext.getResources().getString(R.string.new_audio_survey_notification_ticker) );
			notificationBuilder.setContentText( appContext.getResources().getString(R.string.new_audio_survey_notification_details) );
			notificationBuilder.setSmallIcon(R.drawable.voice_recording_icon);
			notificationBuilder.setLargeIcon( BitmapFactory.decodeResource(appContext.getResources(), R.drawable.voice_recording_icon) );
		}
		else { throw new NullPointerException("survey type did not parse correctly: " + PersistentData.getSurveyType(surveyId)); }

        activityIntent.putExtra( "surveyId", surveyId );
		activityIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP ); //modifies behavior when the user is already in the app.
		/* The pending intent defines properties of the notification itself.
		 * BUG. Cannot use FLAG_UPDATE_CURRENT, which handles conflicts of multiple notification with the same id, 
		 * so that the new notification replaces the old one.  if you use FLAG_UPDATE_CURRENT the notification will
		 * not launch the provided activity on android api 19.
		 * Solution: use FLAG_CANCEL_CURRENT, it provides the same functionality for our purposes.
		 * (or add android:exported="true" to the activity's permissions in the Manifest.)
		 * http://stackoverflow.com/questions/21250364/notification-click-not-launch-the-given-activity-on-nexus-phones */
		//we manually cancel the notification anyway, so this is likely moot.
		PendingIntent pendingActivityIntent = PendingIntent.getActivity(appContext,
				1, // a Request code meaning "close the notification once done"
				activityIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);		
		notificationBuilder.setContentIntent(pendingActivityIntent);
		Notification surveyNotification = notificationBuilder.build();
		surveyNotification.flags = Notification.FLAG_ONGOING_EVENT;
		
		//This value is used inside the notification as the unique Identifier of that notification (it has to be an int)
		//note: recommendations about not using the .hashCode function in java are in usually regards to Object.hashCode(),
		//or are about the fact that the specific hash algorithm is not necessarily consistent between versions of the JVM.
		// If you look at the source of the String class hashCode function you will see that it operates on the value of the string, this is all we need.
		int surveyIdHash = surveyId.hashCode();
		NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(surveyIdHash); //cancel current
		notificationManager.notify(
				surveyIdHash, // If another notification with the same ID pops up, this notification will be updated/cancelled.
				surveyNotification);
		
		//And, finally, set the notification state for zombie alarms.
		PersistentData.setSurveyNotificationState(surveyId, true);
	}
	
	
	/**Use to dismiss the notification corresponding the surveyIdInt.
	 * @param appContext
	 * @param notifCode */
	public static void dismissNotification(Context appContext, String surveyId) {
 		NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(surveyId.hashCode());
	}
}