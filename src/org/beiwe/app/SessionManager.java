package org.beiwe.app;
 
import java.util.HashMap;

import android.annotation.SuppressLint;
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

@SuppressLint("CommitPrefEdits")
public class SessionManager {
    private SharedPreferences pref; 
    private Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;     
    private static final String PREF_NAME = "AndroidHivePref";
    private static final String IS_LOGIN = "IsLoggedIn";
   
    public static final String KEY_NAME = "name";
    public static final String KEY_PASSWORD = "email";
     
    /**
     * This is a constructor method for the session manager class
     * @param context from LoginActivity
     */
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //Shared Preferences is set to private mode
        editor = pref.edit();
    }
     
   /**
    * This creates a new login session. Interacts with the shared preferences
    * @param username
    * @param password
    */
    public void createLoginSession(String username, String password){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }   
     
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything. Used in Login Activity
     * */
    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent intent = new Intent(_context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(intent);
        }
    }
     
     
    // TODO: Decide whether or not we want to keep this method at all...
    /**
     * Get stored session data method. Should not be called at all
     * */
    public HashMap<String, String> getUserDetails(){
    	
    	// TODO: Eventually this will actually be a hash-hash combination
    	HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        return user;
    }
     
    /**
     * Clears session details, and shared preferences.
     * Should be used in main activity (debug) 
     * */
    public void logoutUser(){
        editor.clear();
        editor.commit();
        Intent intent = new Intent(_context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intent);
    }
     
    /**
     * Quick check for login. Used in Login Activity
     * **/
    public boolean isLoggedIn(){
    	Log.i("SessionManager", "" + pref.getBoolean(IS_LOGIN, false));
    	return pref.getBoolean(IS_LOGIN, false);
    }
}