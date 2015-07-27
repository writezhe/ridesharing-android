package org.beiwe.app.survey;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.beiwe.app.JSONUtils;
import org.beiwe.app.Timer;
import org.json.JSONArray;
import org.json.JSONException;
import org.beiwe.app.storage.PersistentData;

import android.util.Log;

import java.util.ArrayList;

//TODO: Eli. Rewrite day of week logic (looks like this is a rewrite of the entire class...) to grab a list from the json and parse it (possibly just pass it directly, buuuut that seems unsafe.  validate input at least.)  
//TODO: Eli. document.
public class SurveyScheduler {

	public static void scheduleSurvey(String surveyId) throws JSONException {
		String timeString = PersistentData.getSurveyTimes(surveyId);
		JSONArray JSONTimes = new JSONArray(timeString);
		
		ArrayList<ArrayList<Integer>> timesList = new ArrayList<ArrayList<Integer>>(7);
		
		Calendar.getInstance();
		//is it better to do this all as jsonarrays? no.
		//TODO: I am not convinced that this gets us todays day-of-week ordinal...
		int today = Calendar.DAY_OF_WEEK;
		List<String> days = JSONUtils.jsonArrayToStringList(JSONTimes);

		//create a list of days of the week, with order as such:
		// 0: today
		// 1: tomorrow
		// 2: day after ...

		JSONArray dayJSON;
		ArrayList<Integer> dayInts;
		for (int i=0; i <= 7; i++) {
			dayJSON = new JSONArray(days.get(i)); //convert to json array
			dayInts = JSONUtils.jsonArrayToIntegerList(dayJSON); //convert to (iteraable) list of ints
			Collections.sort(dayInts); //ensure sorted
			if (i < today) timesList.add( dayInts ); //if index < today, add to end
			if (i == today) timesList.add(0, dayInts ); //if index is today, add to beginning
			if (i > today) timesList.add(1, dayInts ); //if index > today, insert at position [1] 
		}
		
//		we now have a double nested list of lists.  element 0 is today, element 1 is tomorrow
		// the inner list contains the times of day at which the survey should trigger, these times are values between 0 and 86,400
		// these values are should be sorted.
		// these values indicate the "seconds past midnightt" of the day that the alarm should trigger.
		// we iterate through the nested list to come to the next time that is after right now.
		
		long newAlarmTime = findNextAlarmTime(timesList);  
	}
	private static Long findNextAlarmTime( ArrayList<ArrayList<Integer>> timesList) {
		//probably easier to comprehend by running this in a function and just returning on a successful time fit.
		Calendar todayCalendar = Calendar.getInstance();
		todayCalendar.set(todayCalendar.YEAR, todayCalendar.MONTH, todayCalendar.DAY_OF_MONTH, 0, 0, 0);
		Long startOfDay = todayCalendar.getTimeInMillis();
		Long now = System.currentTimeMillis();
		Long possibleAlarmTime;
		Long firstPossibleAlarmTime = null;
		Boolean firstFlag = true;
		for ( ArrayList<Integer> day : timesList ) {
			for (long time : day) {
				if (time > 86400 || time < 0) {
					throw new NullPointerException("time parser received an invalid value in the time parsing: " + time);
				}
				time *=1000;
				possibleAlarmTime = time + startOfDay;
				if (firstFlag){
					firstPossibleAlarmTime = possibleAlarmTime;
				}
				if (possibleAlarmTime > now ) {
					Log.d("Scheduler", "checked " + possibleAlarmTime + ", yup!");
					return possibleAlarmTime;
				}
				Log.d("Scheduler", "checked " + possibleAlarmTime + ", nope.");
			}
		}
		//handles the case where the next alarm is earlier in the day than right now, 1 week from now.
		// (i.e. no matches in the range of times that fall after right now occuring in the list(s) of times
		if (firstPossibleAlarmTime == null) {
			//TODO: Eli. create policy for a survey with no alarm times.
			throw new NullPointerException("the alarm time parser found no values in the times dictionary.");
		}
		return firstPossibleAlarmTime + Timer.ONE_WEEK_IN_MILLISECONDS;
	}	
}
