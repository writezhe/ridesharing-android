package org.beiwe.app.survey;

import org.beiwe.app.R;

//TODO: josh.  Insert a few lines of documentation describing what this is for.

public class SurveyType {
	public enum Type {
		DAILY (002,
				"daily",
				R.string.daily_survey_questions_url,
				R.string.daily_survey_notification_message,
				R.string.daily_survey_notification_details),
		WEEKLY (003,
				"weekly",
				R.string.weekly_survey_questions_url,
				R.string.weekly_survey_notification_message,
				R.string.weekly_survey_notification_details);
		
		public final int notificationCode;
		public final String dictKey;
		public final int urlResource;
		public final int notificationMsgResource;
		public final int notificationDetailsResource;
		
		private Type(int notificationCode, String dictKey, int urlResource, int notificationMsgResource, int notificationDetailsResource) {
			this.notificationCode = notificationCode;
			this.dictKey = dictKey;
			this.urlResource = urlResource;
			this.notificationMsgResource = notificationMsgResource;
			this.notificationDetailsResource = notificationDetailsResource;
		}
	}

}
