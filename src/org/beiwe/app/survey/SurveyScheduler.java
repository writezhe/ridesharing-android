package org.beiwe.app.survey;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.beiwe.app.BackgroundService;
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

	public static void scheduleSurvey(String surveyId) {
		
		String timeString = PersistentData.getSurveyTimes(surveyId);
		JSONArray JSONTimes;
		try { JSONTimes = new JSONArray(timeString); }
		catch (JSONException e) {
			e.printStackTrace();
			throw new NullPointerException(e.getMessage());
		}
		
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
		ArrayList<ArrayList<Integer>> timesList = new ArrayList<ArrayList<Integer>>(7);

		for (int i=0; i <= 6; i++) {
			try { dayJSON = new JSONArray(days.get(i)); }  //convert to json array
			catch (JSONException e) {
				e.printStackTrace();
				throw new NullPointerException(e.getMessage());
			}
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
		Calendar newAlarmTime = findNextAlarmTime(timesList);
		if (newAlarmTime == null) {
			Log.w("SurveyScheduler", "there were no times at all in the provided timings list.");
			return; } 
		Log.e("Survey Scheduler", "SCHEDULING LOGIC FINISHED, SCHEDULING SURVEY.");
		BackgroundService.setSurveyAlarm(surveyId, newAlarmTime);
	}
	
	private static Calendar findNextAlarmTime( ArrayList<ArrayList<Integer>> timesList) {
		//TODO: Josh. please double check for me that I am creating a calendar object in the phone's timezone.
		Calendar calendar = Calendar.getInstance();
		Long now = System.currentTimeMillis();
		Long possibleAlarmTime = null;
		Long firstPossibleAlarmTime = null;
		Boolean firstFlag = true;
		calendar.set(Calendar.HOUR_OF_DAY, 0);//TODO: probably don't need to set both of these.
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Long startOfDayInMilliseconds = calendar.getTimeInMillis();
		for ( ArrayList<Integer> day : timesList ) { //iterate through the days of the week
			for (long time : day) { //iterate through the times in each day
				if (time > 86400 || time < 0) { throw new NullPointerException("time parser received an invalid value in the time parsing: " + time); }
				time *=1000;
				possibleAlarmTime = time + startOfDayInMilliseconds;
				
				if (firstFlag) { //grab the first time we come across in case it falls into the edge case.
					firstPossibleAlarmTime = possibleAlarmTime;
					firstFlag = false; }
				
				if (possibleAlarmTime > now ) { //If the time is in the future, return that time.
					Log.d("Scheduler", "checked " + possibleAlarmTime + ", yup!");
					calendar.setTimeInMillis(possibleAlarmTime);
					return calendar; }
				
				Log.d("Scheduler", "checked " + possibleAlarmTime + ", nope.");
			}
			startOfDayInMilliseconds += Timer.ONE_DAY_IN_MILLISECONDS; //advance our start of day counter to the next day.
		}
		//handles the case where the next alarm is earlier in the day than right now, 1 week from now.
		// (i.e. no matches in the range of times that fall after right now occuring in the list(s) of times
		/* Warning: for some reason... if you try to throw a null pointer exception in here the app freezes. */
		//TODO: Eli/Josh.  determine why the app stalls when nullpointerexceptions are thrown on... non gui threads?  insert a null pointer exception here and comment out the remainer of the function to see what I am talking about.
//		throw new NullPointerException("totally arbitrary message");
		if (firstPossibleAlarmTime == null) { return null; }
		
		calendar.setTimeInMillis(firstPossibleAlarmTime + Timer.ONE_WEEK_IN_MILLISECONDS);
		return calendar;
	}
}