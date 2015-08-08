package org.beiwe.app.survey;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.JSONUtils;
import org.beiwe.app.Timer;
import org.json.JSONArray;
import org.json.JSONException;
import org.beiwe.app.storage.PersistentData;

import android.util.Log;

import java.util.ArrayList;
  
//TODO: Eli. document.
public class SurveyScheduler {

	public static void scheduleSurvey(String surveyId) {
		int today;
		JSONArray JSONTimes;
		JSONArray dayJSON;
		ArrayList<Integer> dayInts;
		ArrayList<ArrayList<Integer>> timesList = new ArrayList<ArrayList<Integer>>(7);
		String timeString = PersistentData.getSurveyTimes(surveyId);
		
		Calendar thisDay = Calendar.getInstance();
		//turns out the first day of the week is not necessarily Sunday. So, in case the user is in such a Locale we manually set that.
		thisDay.setFirstDayOfWeek(Calendar.SUNDAY);
		today = thisDay.get(Calendar.DAY_OF_WEEK) - 1;
		
		try { JSONTimes = new JSONArray(timeString); }
		catch (JSONException e) { e.printStackTrace(); //If this fails we have significant problems, but probably the errors come from external factors.
			throw new NullPointerException(e.getMessage()); }
		List<String> days = JSONUtils.jsonArrayToStringList(JSONTimes);
		
		//we will now create a list of days of the week, with order like this:
		// 0: today, 1: tomorrow, 2: day after ...
		for (int i=0; i <= 6; i++) {
			try { dayJSON = new JSONArray(days.get(i)); }  //convert to json array
			catch (JSONException e) { e.printStackTrace(); //Again, if this crashes we have problems but they probably come from external factors.
				throw new NullPointerException(e.getMessage()); }
			dayInts = JSONUtils.jsonArrayToIntegerList(dayJSON); //convert to (iteraable) list of ints
			Collections.sort(dayInts); //ensure sorted because... because.
			if (i < today) { timesList.add( dayInts ); } //if index < today, add to end
			if (i == today) { timesList.add(0, dayInts ); } //if index is today, add to beginning
			if (i > today) { timesList.add(1, dayInts ); } //if index > today, insert at position [1]
		}
		
//		we now have a double nested list of lists.  element 0 is today, element 1 is tomorrow
		// the inner list contains the times of day at which the survey should trigger, these times are values between 0 and 86,400
		// these values are should be sorted.
		// these values indicate the "seconds past midnight" of the day that the alarm should trigger.
		// we iterate through the nested list to come to the next time that is after right now.
		Calendar newAlarmTime = findNextAlarmTime(timesList);
		if (newAlarmTime == null) {
			Log.w("SurveyScheduler", "there were no times at all in the provided timings list.");
			return; }
		BackgroundService.setSurveyAlarm(surveyId, newAlarmTime);
	}
	
	private static Calendar findNextAlarmTime( ArrayList<ArrayList<Integer>> timesList) {
		Calendar now = Calendar.getInstance();
//		Log.d("scheduler", "now: " + now);
		Calendar possibleAlarmTime = null;
		Calendar firstPossibleAlarmTime = null;
		Boolean firstFlag = true;
		int days = 0;
		for ( ArrayList<Integer> day : timesList ) { //iterate through the days of the week
			for (int time : day) { //iterate through the times in each day
				if (time > 86400 || time < 0) { throw new NullPointerException("time parser received an invalid value in the time parsing: " + time); }
				possibleAlarmTime = getTimeFromStartOfDayOffset(time);
				if (firstFlag) { //grab the first time we come across in case it falls into the edge case.
					firstPossibleAlarmTime = (Calendar) possibleAlarmTime.clone();
					firstFlag = false;
				}
				possibleAlarmTime.add(Calendar.DATE, days); //add to this time the appropriate number of days
				if ( possibleAlarmTime.after( now ) ) { //If the time is in the future, return that time.
//					Log.d("Scheduler", "checked, yup: " + possibleAlarmTime );
					return possibleAlarmTime;
				}
//				Log.d("Scheduler", "checked, nope: " + possibleAlarmTime);
			}
			days++; //advance to next day...
		}
		/* Warning: for some reason... if you try to throw a null pointer exception in here the app freezes. */
		//TODO: Eli/Josh.  determine why the app stalls when nullpointerexceptions are thrown on... non gui threads?  insert a null pointer exception here and comment out the remainer of the function to see what I am talking about.
//		throw new NullPointerException("totally arbitrary message");
		if (firstPossibleAlarmTime == null) { return null; }
		firstPossibleAlarmTime.add(Calendar.DATE, 7);  // advance the date to the following week.
		return firstPossibleAlarmTime;
	}
	
	private static Calendar getTimeFromStartOfDayOffset(int offset) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, offset / 3600 ); //seconds divided by seconds per hour yields hour of day
//		calendar.set(Calendar.HOUR, offset / 3600 ); //this one appears to be derived, so we don't bother setting it.
		calendar.set(Calendar.MINUTE, offset / 60 % 60); //seconds divided by sixty mod sixty yields minutes
		calendar.set(Calendar.SECOND, offset % 60); //seconds mod 60 yields seconds into minute
		calendar.set(Calendar.MILLISECOND, 0);
//		Log.d("scheduler", "time of day  - " + calendar.get(Calendar.HOUR) + ":"  + calendar.get(Calendar.MINUTE)+ ":" + calendar.get(Calendar.SECOND));
//		Log.d("scheduler", "raw t of day - " + offset / 3600 + ":" + offset / 60 % 60 + ":" + offset % 60 );
		return calendar;
	}

}