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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**The purpose of this class is to deal with all that has to do with Survey Notifications.
 * This is a STATIC method, and is called from the background process
 * 
 * @author Eli Jones */
//TODO: Eli. Redoc.
public class SurveyNotifications {
	/**Creates a survey notification that transfers the user to the survey activity. 
	 * Note: the notification can only be dismissed through submitting the survey
	 * @param appContext */
	public static void displaySurveyNotification(Context appContext, String surveyId) {
		//TODO: Eli. Check that this doc is correct, I might have the intent and pendingintent backwards.
		//activityIntent contains information on the action triggered by tapping the notification. 
		//it contains the action ("launch this activity class"), and a surveyId.
		//   the original declaration:  activityIntent = new Intent(surveyType.dictKey);
		Intent activityIntent = new Intent(surveyId);
		int numericalSurveyId = PersistentData.getNumericalSurveyId(surveyId);
		
		int iconId;
		//TODO: Eli. make sure we have a consistent use of these type identifiers across both codebases
		if ( PersistentData.getSurveyType(surveyId) == "android_survey" ) { iconId = R.drawable.survey_icon; }
		else if ( PersistentData.getSurveyType(surveyId) == "audio_survey" ) { iconId = R.drawable.survey_icon; }
		else {
			Log.e("backgroundService", "survey type did not work correctly");
			throw new NullPointerException("survey type did not work correctly");
		};
		
		//FIXME: Eli.  add logic that checks the survey type and returns the appropriate activity class to launch.
		activityIntent = new Intent( appContext, AudioRecorderActivity.class );
        activityIntent.setClass( appContext, SurveyActivity.class );
        //TODO: Eli. changed this string from "SurveyType" to "SurveyId", look for bugs...
        activityIntent.putExtra( "SurveyId", surveyId );
        activityIntent.putExtra( "SurveyIdInt", numericalSurveyId );
		activityIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP ); //modifies behavior when the user is currently in the app.

		/* The pending intent defines properties of the notification itself.
		 * BUG. Cannot use FLAG_UPDATE_CURRENT, which handles conflicts with multiple notification with the same id, 
		 * to replace the existing notification.  This variable is supplied later in notificationManager.notify()
		 * if you use FLAG_UPDATE_CURRENT the notification will not launch the provided activity on android api 19.
		 * solution: use FLAG_CANCEL_CURRENT, it provides the same functionality for our purposes.
		 * (or add android:exported="true" to the activity's permissions in the Manifest.)
		 * http://stackoverflow.com/questions/21250364/notification-click-not-launch-the-given-activity-on-nexus-phones */
		PendingIntent pendingActivityIntent = PendingIntent.getActivity(appContext,
				1, // a Request code meaning "close the notification once done"
				activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		//TODO: Eli. this is the original call to the now defunct setup function, note the survey icon argument
//		Notification surveyNotification = setupNotification(appContext, surveyIdInt, R.drawable.survey_icon, surveyIdInt);
		// and this was the variables in the function definition: Context appContext, int notifCode, int iconID, int surveyIdInt
		NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);		
		builder.setSmallIcon(iconId);
        builder.setLargeIcon( BitmapFactory.decodeResource(appContext.getResources(), iconId) );
		builder.setContentTitle( appContext.getString(R.string.app_name) );
		builder.setContentText( appContext.getResources().getString(R.string.new_android_survey_notification_details) );
		builder.setTicker( appContext.getResources().getString(R.string.new_android_survey_notification_ticker) );
		builder.setContentIntent(pendingActivityIntent);
		
		//Build the notification, interface with a notification manager.
		Notification surveyNotification = builder.build();
		NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(numericalSurveyId); //cancel current
		surveyNotification.flags = Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(
				numericalSurveyId, // If another notification with the same ID pops up, this notification will be updated/cancelled.
				surveyNotification);
		//And, finally, set the notification state for zombie alarms.
		PersistentData.setSurveyNotificationState(surveyId, true);
	}
	
	/**Use to dismiss the notification corresponding the surveyIdInt.
	 * @param appContext
	 * @param notifCode */
	public static void dismissNotification(Context appContext, String surveyId) {
		//TODO: Eli.  Test.  I only Think that this value is the correct id to dismiss a notification, previously it used a per-study-type constant in a SurveyType.
		NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(PersistentData.getNumericalSurveyId(surveyId));
	}
}