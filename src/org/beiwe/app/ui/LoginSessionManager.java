package org.beiwe.app.ui;
 
import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
 

/** 
 * A class used to manage login sessions. Uses SharedPreferences in order to save
 * username-password combinations.
 * @author Dori Samet
 *
 */

public class LoginSessionManager {
	
	// Private things that are encapsulated using functions in this class 
	private SharedPreferences pref; 
    private Editor editor;
    private Context appContext;
    private int PRIVATE_MODE = 0;     
    private static final String PREF_NAME = "BeiwePref";
    private static final String IS_LOGIN = "IsLoggedIn";
   
    // Public names for when inspecting the user's details. Used to call from outside the class.
    public static final String KEY_NAME = "username";
    public static final String KEY_PASSWORD = "password";
     
    /**
     * This is a constructor method for the session manager class
     * @param context from LoginActivity
     */
	public LoginSessionManager(Context context){
        this.appContext = context;
        Log.i("SessionManager", "Attempting to get SharedPreferences");
        pref = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //Shared Preferences is set to private mode
        editor = pref.edit();
        editor.commit();
    }
     
   /**
    * This creates a new login session. Interacts with the SharedPreferences.
    * @param username
    * @param password
    */
    public void createLoginSession(String username, String password){
    	editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
        Log.i("SessionManager", pref.getString(KEY_NAME, "Bobby McGee")); // Saved username
        Log.i("SessionManager", pref.getString(KEY_PASSWORD, "DefPassword")); // Saved password
    }   
     
    /**
     * Checks which page the user should scroll to. If there was an active session, the user will
     * be transferred to {@link DebugInterfaceActivity}, otherwise if there was information saved in
     * SharedPreferences, the user will be transferred to {@link LoginActivity}. Otherwise, it is
     * the user's first time, therefore will start with {@link RegisterActivity}.
     * */
    public void checkLogin(){
    	Log.i("SessionManager", "Check if already logged in");
    	if(this.isLoggedIn()) {
    		Intent intent = new Intent(appContext, DebugInterfaceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(intent);
        } else {
        	Log.i("SessionManager", "Check if it is not first time login");
        	if (!this.isLoggedIn() && pref.contains(KEY_NAME)) {
        		Intent intent = new Intent(appContext, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);        	
        	} else {
            	Log.i("SessionManager", "First time logged in");
            	Intent intent = new Intent(appContext, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
        	}
        }
    }
     
    /**
     * A way of encapsulating SharedPreferences and the user's details stored in them.
     * @return
     */
    public HashMap<String, String> getUserDetails(){
       	HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        Log.i("SessionManager", user.toString());
        return user;
    }
     
    /**
     * Clears session details, and SharedPreferences. Should be used in {@link DebugInterfaceActivity}. 
     * Using this function does not send the user back to {@link RegisterActivity}, but to {@link LoginActivity}
     * 
     * */
    public void logoutUser(){
    	editor.putBoolean(IS_LOGIN, false);
    	editor.commit();
    	Intent intent = new Intent(appContext, LoadingActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	
    	appContext.startActivity(intent);
    }
     
    /**
     * Quick check for login. 
     * **/
    public boolean isLoggedIn(){
    	Log.i("SessionManager", "" + pref.getBoolean(IS_LOGIN, false));
    	return pref.getBoolean(IS_LOGIN, false);
    }
}