package org.beiwe.app.survey;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

public class SurveyType {
	// TODO Josh: can this public enum _not_ be wrapped in a class?  That would make calling it easier
	public enum Type {
		DAILY (002,
				TextFileManager.getCurrentDailyQuestionsFile(),
				R.string.daily_survey_questions_url,
				"daily",
				R.string.daily_survey_notification_message,
				R.string.daily_survey_notification_details),
		WEEKLY (003,
				TextFileManager.getCurrentWeeklyQuestionsFile(),
				R.string.weekly_survey_questions_url,
				"weekly",
				R.string.weekly_survey_notification_message,
				R.string.weekly_survey_notification_details);
		
		public final int notificationCode;
		public final TextFileManager file;
		public final int urlResource;
		public final String dictKey;
		public final int notificationMsgResource;
		public final int notificationDetailsResource;
		
		private Type(int notificationCode, TextFileManager file, int urlResource, String dictKey, int notificationMsgResource, int notificationDetailsResource) {
			this.notificationCode = notificationCode;
			this.file = file;
			this.urlResource = urlResource;
			this.dictKey = dictKey;
			this.notificationMsgResource = notificationMsgResource;
			this.notificationDetailsResource = notificationDetailsResource;
		}
	}

}
