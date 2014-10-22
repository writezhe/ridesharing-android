package org.beiwe.app.survey;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.R;
import org.beiwe.app.Timer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SurveyScheduler {

	private Context appContext;
	
	public SurveyScheduler(Context applicationContext) {
		appContext = applicationContext;
	}
	
	
	public void scheduleSurvey(String jsonSurveyString) {
		Log.i("SurveyScheduler", "scheduleSurvey() called");
		// TODO: Josh ask Eli, is it safe to grab the Background Handle like this?
		Timer timer = new Timer(BackgroundProcess.getBackgroundHandle());
		int hour = hourOfDayToAskSurvey(jsonSurveyString);

		if (dayOfWeekToAskSurvey(jsonSurveyString) == -1) {
			// Schedule a daily survey
			Intent intent = new Intent(appContext.getString(R.string.daily_survey));
			timer.setupDailyRepeatingAlarm(hour, intent);
			Log.i("SurveyScheduler", "Daily survey scheduled");
		}
		else {
			// Schedule a weekly survey
			int dayOfWeek = dayOfWeekToAskSurvey(jsonSurveyString);
			Intent intent = new Intent(appContext.getString(R.string.weekly_survey));
			timer.setupWeeklyRepeatingAlarm(dayOfWeek, hour, intent);
			Log.i("SurveyScheduler", "Weekly survey scheduled");
		}
		Log.i("SurveyScheduler", "scheduleSurvey() finished");
	}
	
	
	private int hourOfDayToAskSurvey(String jsonSurveyString) {
		try {
			JSONObject surveyObject = new JSONObject(jsonSurveyString);
			return surveyObject.getInt("hour_of_day");
		} catch (JSONException e) {
			// If JSON parsing fails, schedule the survey for 19:00 (i.e., 7:00 p.m.)
			return 19;
		}
	}
	
	
	private int dayOfWeekToAskSurvey(String jsonSurveyString) {
		try {
			JSONObject surveyObject = new JSONObject(jsonSurveyString);
			return surveyObject.getInt("day_of_week");
		} catch (JSONException e) {
			// If attribute "day_of_week" doesn't exist, it's a daily survey; return -1
			return -1;
		}
	}
	
}
