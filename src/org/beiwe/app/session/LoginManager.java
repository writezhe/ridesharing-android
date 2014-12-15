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


/**A class used to manage login sessions. Uses SharedPreferences in order to save
 * username-password combinations.
 * @author Dor Samet, Eli Jones
 * 
 *  
 *  
 *  */

public class LoginManager {

	private static int PRIVATE_MODE = 0;

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
	
	/*#####################################################################################
	######################### Constructor and Initializing ################################
	#####################################################################################*/

	/**Constructor method for the session manager class
	 * @param context */
	private LoginManager(Context context){
		appContext = context;
		pref = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //sets Shared Preferences private mode
		editor = pref.edit();
		editor.commit();
	}

	public static void initialize( Context context ) { new LoginManager(context); } 

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

	public static boolean isRegistered() { 
		if (pref == null) Log.w("LoginManager", "FAILED AT ISREGISTERED");
		return pref.getBoolean(IS_REGISTERED, false); }
	
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
		// Improvement idea: set more password requirements (must have both letters and numbers)
		return true;
	}


	/**Takes an input string and returns a boolean value stating whether the input matches the current password.
	 * @param input
	 * @return */
	public static boolean checkPassword(String input){
		return ( getPassword().equals( EncryptionEngine.safeHash(input) ) );
	}

	public static void setPassword(String password) {
		editor.putString(KEY_PASSWORD, EncryptionEngine.safeHash(password) );
		editor.commit();
	}

	public static void setPasswordDirectly(String password){
		editor.putString(KEY_PASSWORD, password);
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

	public static String getPatientID() { return pref.getString(KEY_ID, "NULLID"); }

}
