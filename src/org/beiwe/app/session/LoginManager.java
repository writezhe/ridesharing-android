package org.beiwe.app.session;

import org.beiwe.app.R;
import org.beiwe.app.Timer;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.ui.AlertsManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**A class for managing patient login sessions.
 * Uses SharedPreferences in order to save username-password combinations.
 * @author Dor Samet, Eli Jones, Josh Zagorsky */
public class LoginManager {
	public static String NULL_ID = "NULLID";
	
	private static int PRIVATE_MODE = 0;
	private static boolean isInitialized = false;
	
	// Private things that are encapsulated using functions in this class 
	private static SharedPreferences pref; 
	private static Editor editor;
	private static Context appContext;
		
	// Editor key-strings
	private static final String PREF_NAME = "BeiwePref";
	private static final String KEY_ID = "uid";
	private static final String KEY_PASSWORD = "password";
	private static final String IS_REGISTERED = "IsRegistered";
	private static final String LOGIN_EXPIRATION = "loginExpirationTimestamp";
	private static final String PRIOR_WEEKLY_TIME = "prior_weekly_timer_time";
	private static final String PRIOR_DAILY_TIME = "prior_daily_time";
	private static final String PRIOR_AUDIO_TIME = "prior_audio_time";
	private static final String PRIOR_WEEKLY_STATE = "prior_weekly_state";
	private static final String PRIOR_DAILY_STATE = "prior_daily_state";
	private static final String PRIOR_AUDIO_STATE = "prior_audio_state";
	
	/*#####################################################################################
	################################### Initializing ######################################
	#####################################################################################*/

	/**The publicly accessible initializing function for the LoginManager, initializes the internal variables.
	 * @param context */
	public static void initialize( Context context ) {
		if ( isInitialized ) { return; }
		appContext = context;
		pref = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //sets Shared Preferences private mode
		editor = pref.edit();
		editor.commit();
		isInitialized = true;
	} 

	/*#####################################################################################
	##################################### Booleans ########################################
	#####################################################################################*/

	/** Quick check for login. **/
	public static boolean isLoggedIn(){
		if (pref == null) Log.w("LoginManager", "FAILED AT ISLOGGEDIN");
		// If the current time is earlier than the expiration time, return TRUE; else FALSE
		return (System.currentTimeMillis() < pref.getLong(LOGIN_EXPIRATION, 0)); }
	
	/** Set the login session to expire a fixed amount of time in the future */
	public static void loginOrRefreshLogin() {
		editor.putLong(LOGIN_EXPIRATION, System.currentTimeMillis() + Timer.MILLISECONDS_BEFORE_AUTO_LOGOUT);
		editor.commit();
	}

	/** Set the login session to "expired" */
	public static void logout() {
		editor.putLong(LOGIN_EXPIRATION, 0);
		editor.commit();
	}
	
	/**Getter for the IS_REGISTERED value.
	 * @param value */
	public static boolean isRegistered() { 
		if (pref == null) Log.w("LoginManager", "FAILED AT ISREGISTERED");
		return pref.getBoolean(IS_REGISTERED, false);
	}
	
	/**Setter for the IS_REGISTERED value.
	 * @param value */
	public static void setRegistered(boolean value) { 
		editor.putBoolean(IS_REGISTERED, value);
		editor.commit();
	}

	
	/*######################################################################################
	##################################### Passwords ########################################
	######################################################################################*/


	/**Checks that an input matches valid password requirements. (this only checks length)
	 * Throws up an alert notifying the user if the password is not valid.
	 * @param input
	 * @param activity
	 * @return true or false based on password requirements.*/
	public static boolean passwordMeetsRequirements(String password, Activity currentActivity) {
		// If the password has too few characters, pop up an alert saying so
		int minPasswordLength = 1; // TODO postproduction: set the minPasswordLength to something higher than 1
		if (password.length() < minPasswordLength) {
			String alertMessage = String.format(appContext.getString(R.string.password_too_short), minPasswordLength);
			AlertsManager.showAlert(alertMessage, currentActivity);
			return false;
		}
		return true;
	}


	/**Takes an input string and returns a boolean value stating whether the input matches the current password.
	 * @param input
	 * @return */
	public static boolean checkPassword(String input){ return ( getPassword().equals( EncryptionEngine.safeHash(input) ) ); }
	
	/**Sets a password to a hash of the provided value.
	 * @param password */
	public static void setPassword(String password) {
		editor.putString(KEY_PASSWORD, EncryptionEngine.safeHash(password) );
		editor.commit();
	}

	/*###########################################################################################
	################################### User Credentials ########################################
	###########################################################################################*/

	public static void setLoginCredentials( String userID, String password ) {
		if (editor == null) Log.e("LoginManager.java", "editor is null");
		editor.putString(KEY_ID, userID);
		setPassword(password);
		editor.commit();
	}

	public static String getPassword() { return pref.getString( KEY_PASSWORD, null ); }

	public static String getPatientID() { return pref.getString(KEY_ID, NULL_ID); }

	/*###########################################################################################
	##################################### Zombie Alarms #########################################
	###########################################################################################*/
	// the 9223372036854775807L is the highest value a java Long can contain, this default value only occurs at registration time.
	public static Long getWeeklySurveyAlarmTime() { return pref.getLong(PRIOR_WEEKLY_TIME, 9223372036854775807L ); }
	public static Long getDailySurveyAlarmTime() { return pref.getLong(PRIOR_DAILY_TIME, 9223372036854775807L ); }
	public static Long getAudioAlarmTime() { return pref.getLong(PRIOR_AUDIO_TIME, 9223372036854775807L ); }
	// the state that the notification SHOULD be in.  The default value is only returned at registration, i.e. weekly triggers at registration, the others don't.
	public static Boolean getCorrectDailyNotificationState() { return pref.getBoolean(PRIOR_DAILY_STATE, false ); }
	public static Boolean getCorrectWeeklyNotificationState() { return pref.getBoolean(PRIOR_WEEKLY_STATE, true ); }
	public static Boolean getCorrectAudioNotificationState() { return pref.getBoolean(PRIOR_AUDIO_STATE, false ); }
	
	// setters for weekly alarm time (gets set inside the notification triggering alarm code in timers) 
	public static void setWeeklySurveyAlarm( Long timeCode ) {
		editor.putLong(PRIOR_WEEKLY_TIME, timeCode );
		editor.commit(); }
	public static void setDailySurveyAlarm( Long timeCode ) {
		editor.putLong(PRIOR_DAILY_TIME, timeCode );
		editor.commit(); }	
	public static void setAudioAlarm( Long timeCode ) {
		editor.putLong(PRIOR_AUDIO_TIME, timeCode );
		editor.commit(); }
	
	// setters for the correct current state of survey notifications, i.e. the state a notification SHOULD be in.
	public static void setCorrectWeeklyNotificationState( Boolean bool ) {
		editor.putBoolean(PRIOR_WEEKLY_STATE, bool );
		editor.commit(); }
	public static void setCorrectDailyNotificationState( Boolean bool ) {
		editor.putBoolean(PRIOR_DAILY_STATE, bool );
		editor.commit(); }	
	public static void setCorrectAudioNotificationState( Boolean bool ) {
		editor.putBoolean(PRIOR_AUDIO_STATE, bool );
		editor.commit(); }
}