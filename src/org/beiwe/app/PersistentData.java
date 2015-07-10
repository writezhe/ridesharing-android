package org.beiwe.app;

import org.beiwe.app.R;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.ui.utils.AlertsManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**A class for managing patient login sessions.
 * Uses SharedPreferences in order to save username-password combinations.
 * @author Dor Samet, Eli Jones, Josh Zagorsky */
public class PersistentData {
	public static String NULL_ID = "NULLID";
	private static final long MAX_LONG = 9223372036854775807L;

	private static int PRIVATE_MODE = 0;
	private static boolean isInitialized = false;

	// Private things that are encapsulated using functions in this class 
	private static SharedPreferences pref; 
	private static Editor editor;
	private static Context appContext;

	/**  Editor key-strings */
	private static final String PREF_NAME = "BeiwePref";
	private static final String KEY_ID = "uid";
	private static final String KEY_PASSWORD = "password";
	private static final String IS_REGISTERED = "IsRegistered";
	private static final String LOGIN_EXPIRATION = "loginExpirationTimestamp";
	private static final String PCP_PHONE_KEY = "primary_care";
	private static final String PASSWORD_RESET_NUMBER_KEY = "reset_number";

	private static final String ACCELEROMETER = "accelerometer";
	private static final String GPS = "gps";
	private static final String CALLS = "calls";
	private static final String TEXTS = "texts";
	private static final String WIFI = "wifi";
	private static final String BLUETOOTH = "bluetooth";
	private static final String POWER_STATE = "power_state";

	private static final String ACCELEROMETER_OFF_DURATION_SECONDS = "accelerometer_off_duration_seconds";
	private static final String ACCELEROMETER_ON_DURATION_SECONDS = "accelerometer_on_duration_seconds";
	private static final String BLUETOOTH_ON_DURATION_SECONDS = "bluetooth_on_duration_seconds";
	private static final String BLUETOOTH_TOTAL_DURATION_SECONDS = "bluetooth_total_duration_seconds";
	private static final String BLUETOOTH_GLOBAL_OFFSET_SECONDS = "bluetooth_global_offset_seconds";
	private static final String CHECK_FOR_NEW_SURVEYS_FREQUENCY_SECONDS = "check_for_new_surveys_frequency_seconds";
	private static final String CREATE_NEW_DATA_FILES_FREQUENCY_SECONDS = "create_new_data_files_frequency_seconds";
	private static final String GPS_OFF_DURATION_SECONDS = "gps_off_duration_seconds";
	private static final String GPS_ON_DURATION_SECONDS = "gps_on_duration_seconds";
	private static final String SECONDS_BEFORE_AUTO_LOGOUT = "seconds_before_auto_logout";
	private static final String UPLOAD_DATA_FILES_FREQUENCY_SECONDS = "upload_data_files_frequency_seconds";
	private static final String VOICE_RECORDING_MAX_TIME_LENGTH_SECONDS = "voice_recording_max_time_length_seconds";
	private static final String WIFI_LOG_FREQUENCY_SECONDS = "wifi_log_frequency_seconds";


	//TODO: Eli. part of arbitrary number of surveys, see zombie alarms below.
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
		editor.commit(); }

	/** Set the login session to "expired" */
	public static void logout() {
		editor.putLong(LOGIN_EXPIRATION, 0);
		editor.commit(); }

	/**Getter for the IS_REGISTERED value.
	 * @param value */
	public static boolean isRegistered() { 
		if (pref == null) Log.w("LoginManager", "FAILED AT ISREGISTERED");
		return pref.getBoolean(IS_REGISTERED, false); }

	/**Setter for the IS_REGISTERED value.
	 * @param value */
	public static void setRegistered(boolean value) { 
		editor.putBoolean(IS_REGISTERED, value);
		editor.commit(); }

	//TODO: Eli.  hook into these toggls during registration.
	public static boolean getAccelerometerEnabled(){ return pref.getBoolean(ACCELEROMETER, false); }
	public static boolean getGpsEnabled(){ return pref.getBoolean(GPS, false); }
	public static boolean getCallsEnabled(){ return pref.getBoolean(CALLS, false); }
	public static boolean getTextsEnabled(){ return pref.getBoolean(TEXTS, false); }
	public static boolean getWifiEnabled(){ return pref.getBoolean(WIFI, false); }
	public static boolean getBluetoothEnabled(){ return pref.getBoolean(BLUETOOTH, false); }
	public static boolean getPowerStateEnabled(){ return pref.getBoolean(POWER_STATE, false); }

	public static void setAccelerometerEnabled(boolean enabled) {
		editor.putBoolean(ACCELEROMETER, enabled);
		editor.commit(); }
	public static void setGpsEnabled(boolean enabled) {
		editor.putBoolean(GPS, enabled);
		editor.commit(); }
	public static void setCallsEnabled(boolean enabled) {
		editor.putBoolean(CALLS, enabled);
		editor.commit(); }
	public static void setTextsEnabled(boolean enabled) {
		editor.putBoolean(TEXTS, enabled);
		editor.commit(); }
	public static void setWifiEnabled(boolean enabled) {
		editor.putBoolean(WIFI, enabled);
		editor.commit(); }
	public static void setBluetoothEnabled(boolean enabled) {
		editor.putBoolean(BLUETOOTH, enabled);
		editor.commit(); }
	public static void setPowerStateEnabled(boolean enabled) {
		editor.putBoolean(POWER_STATE, enabled);
		editor.commit(); }

	public static void setAccelerometerOffDurationSeconds(long seconds) {
		editor.putLong(ACCELEROMETER_OFF_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setAccelerometerOnDurationSeconds(long seconds) {
		editor.putLong(ACCELEROMETER_ON_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setBluetoothOnDurationSeconds(long seconds) {
		editor.putLong(BLUETOOTH_ON_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setBluetoothTotalDurationSeconds(long seconds) {
		editor.putLong(BLUETOOTH_TOTAL_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setBluetoothGlobalOffsetSeconds(long seconds) {
		editor.putLong(BLUETOOTH_GLOBAL_OFFSET_SECONDS, seconds);
		editor.commit(); }
	public static void setCheckForNewSurveysFrequencySeconds(long seconds) {
		editor.putLong(CHECK_FOR_NEW_SURVEYS_FREQUENCY_SECONDS, seconds);
		editor.commit(); }
	public static void setCreateNewDataFilesFrequencySeconds(long seconds) {
		editor.putLong(CREATE_NEW_DATA_FILES_FREQUENCY_SECONDS, seconds);
		editor.commit(); }
	public static void setGpsOffDurationSeconds(long seconds) {
		editor.putLong(GPS_OFF_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setGpsOnDurationSeconds(long seconds) {
		editor.putLong(GPS_ON_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setSecondsBeforeAutoLogout(long seconds) {
		editor.putLong(SECONDS_BEFORE_AUTO_LOGOUT, seconds);
		editor.commit(); }
	public static void setUploadDataFilesFrequencySeconds(long seconds) {
		editor.putLong(UPLOAD_DATA_FILES_FREQUENCY_SECONDS, seconds);
		editor.commit(); }
	public static void setVoiceRecordingMaxTimeLengthSeconds(long seconds) {
		editor.putLong(VOICE_RECORDING_MAX_TIME_LENGTH_SECONDS, seconds);
		editor.commit(); }
	public static void setWifiLogFrequencySeconds(long seconds) {
		editor.putLong(WIFI_LOG_FREQUENCY_SECONDS, seconds);
		editor.commit(); }

	public static long getAccelerometerOffDurationSeconds() { return pref.getLong(ACCELEROMETER_OFF_DURATION_SECONDS, MAX_LONG); }
	public static long getAccelerometerOnDurationSeconds() { return pref.getLong(ACCELEROMETER_ON_DURATION_SECONDS, MAX_LONG); }
	public static long getBluetoothOnDurationSeconds() { return pref.getLong(BLUETOOTH_ON_DURATION_SECONDS, MAX_LONG); }
	public static long getBluetoothTotalDurationSeconds() { return pref.getLong(BLUETOOTH_TOTAL_DURATION_SECONDS, MAX_LONG); }
	public static long getBluetoothGlobalOffsetSeconds() { return pref.getLong(BLUETOOTH_GLOBAL_OFFSET_SECONDS, MAX_LONG); }
	public static long getCheckForNewSurveysFrequencySeconds() { return pref.getLong(CHECK_FOR_NEW_SURVEYS_FREQUENCY_SECONDS, MAX_LONG); }
	public static long getCreateNewDataFilesFrequencySeconds() { return pref.getLong(CREATE_NEW_DATA_FILES_FREQUENCY_SECONDS, MAX_LONG); }
	public static long getGpsOffDurationSeconds() { return pref.getLong(GPS_OFF_DURATION_SECONDS, MAX_LONG); }
	public static long getGpsOnDurationSeconds() { return pref.getLong(GPS_ON_DURATION_SECONDS, MAX_LONG); }
	public static long getSecondsBeforeAutoLogout() { return pref.getLong(SECONDS_BEFORE_AUTO_LOGOUT, MAX_LONG); }
	public static long getUploadDataFilesFrequencySeconds() { return pref.getLong(UPLOAD_DATA_FILES_FREQUENCY_SECONDS, MAX_LONG); }
	public static long getVoiceRecordingMaxTimeLengthSeconds() { return pref.getLong(VOICE_RECORDING_MAX_TIME_LENGTH_SECONDS, MAX_LONG); }
	public static long getWifiLogFrequencySeconds() { return pref.getLong(WIFI_LOG_FREQUENCY_SECONDS, MAX_LONG); }

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
	#################################### Contact Numbers ########################################
	###########################################################################################*/

	public static String getPrimaryCareNumber() {
		return pref.getString(PCP_PHONE_KEY, "");
	}
	public static void setPrimaryCareNumber( String phoneNumber) {
		editor.putString(PCP_PHONE_KEY, phoneNumber );
		editor.commit();
	}

	public static String getPasswordResetNumber() {
		return pref.getString(PASSWORD_RESET_NUMBER_KEY, "");
	}
	public static void setPasswordResetNumber( String phoneNumber ){
		editor.putString(PASSWORD_RESET_NUMBER_KEY, phoneNumber );
		editor.commit();
	}

	/*###########################################################################################
	##################################### Zombie Alarms #########################################
	###########################################################################################*/
	//TODO: Eli. Reimplement, make these work for arbitrary number of surveys.

	// the 9223372036854775807L is the highest value a java Long can contain, this default value only occurs at registration time.
	public static Long getWeeklySurveyAlarmTime() { return pref.getLong(PRIOR_WEEKLY_TIME, MAX_LONG ); }
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