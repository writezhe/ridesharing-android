package org.beiwe.app.networking;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.beiwe.app.ui.utils.SurveyNotifications;

/**
 * Created by admin on 5/22/17.
 */

public class FCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("FCMmessaging", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("FCMmessaging", "Message data payload: " + remoteMessage.getData());

            String surveyType = remoteMessage.getData().get("survey_type");
            String surveyId = remoteMessage.getData().get("survey_id");
            Log.d("FCMmessaging", "survey_type = " + surveyType);
            if (surveyType != null && surveyId != null) {
                SurveyNotifications.displaySurveyNotification(getApplicationContext(), surveyId, surveyType);
            }

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                // scheduleJob();
            } else {
                // Handle message within 10 seconds
                // handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("FCMmessaging", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
