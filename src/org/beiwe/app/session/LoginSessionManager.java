	package org.beiwe.app.session;
 
import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.RegisterActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
 

/**A class used to manage login sessions. Uses SharedPreferences in order to save
 * username-password combinations.
 * @author Dor Samet */

public class LoginSessionManager {
	
	// Private things that are encapsulated using functions in this class 
	private SharedPreferences pref; 
    private Editor editor;
    private Context appContext;
    public static int PRIVATE_MODE = 0;     
    public static final String PREF_NAME = "BeiwePref";
    private static final String IS_LOGIN = "IsLoggedIn";
   
    // Public names for when inspecting the user's details. Used to call from outside the class.
    public static final String KEY_ID = "uid";
    public static final String KEY_PASSWORD = "password";
	public static final String IS_REGISTERED = "IsRegistered";
     
    
	/**Constructor method for the session manager class
     * @param context */
	public LoginSessionManager(Context context){
        this.appContext = context;
        pref = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //sets Shared Preferences private mode
        editor = pref.edit();
        editor.commit();
    }
    
	
   /** Creates a new login session. Interacts with the SharedPreferences.
    * @param userID
    * @param password */
    public void createLoginSession(String userID, String password){
    	editor.putBoolean(IS_REGISTERED, true);
    	editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_ID, userID);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }
     
    
    /**Checks which page the user should scroll to. If there was an active session, the user will
     * be transferred to {@link DebugInterfaceActivity}, otherwise if there was information saved in
     * SharedPreferences, the user will be transferred to {@link LoginActivity}. Otherwise, it is
     * the user's first time, therefore will start with {@link RegisterActivity}. */
    public Intent checkLogin(){
    	Class debug = RegisterActivity.class;
//    	Class debug = LoginActivity.class;
//    	Class debug = DebugInterfaceActivity.class;
//    	Class debug = MainMenuActivity.class;
    	Log.i("LoginSessionManager", "Already logged in: " + isRegistered() );

    	if(this.isLoggedIn()) {
    		// If already logged in, take user to the main menu screen
    		// TODO: before launch, uncomment this line:
    		//Intent intent = new Intent(appContext, MainMenuActivity.class);
    		return new Intent(appContext, debug);
        } else {
        	if (this.isRegistered()) {
        		// If not logged in, but has registered, take user to the login screen
        		return new Intent(appContext, LoginActivity.class);
        	} else {
        		// If not logged in and hasn't registered, take user to registration screen
            	Log.i("LoginSessionManager", "First time logged in");
            	// TODO: DEBUG CODE. uncomment this line:
            	//Intent intent = new Intent(appContext, RegisterActivity.class);
            	return new Intent(appContext, debug);
        	}
        }
    }
    
     
    /**A way of encapsulating SharedPreferences and the user's details stored in them.
     * @return user details */
    public HashMap<String, String> getUserDetails(){
       	HashMap<String, String> userDetails = new HashMap<String, String>();
        userDetails.put(KEY_ID, pref.getString(KEY_ID, null));
        userDetails.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        //TODO: Eli. Determine why this is triggering twice when bypassing the login screen for the debug activity.
        Log.i("LoginSessionManager", "user details: " + userDetails.toString());
        return userDetails;
    }
    
    
    /**Clears session details and SharedPreferences.  Sends user to {@link LoginActivity} */
    public void logoutUser(){
    	editor.putBoolean(IS_LOGIN, false);
        editor.commit();
        Intent intent = new Intent(appContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
    }
    
	/** logs user out without forcing an activity change. */
	public void logoutUserPassive() {
		editor.putBoolean(IS_LOGIN, false);
		editor.commit();
	}
    	
    /** Quick check for login. **/
    public boolean isLoggedIn(){ return pref.getBoolean(IS_LOGIN, false); }
    
    public boolean isRegistered() { return pref.getBoolean(IS_REGISTERED, false); }
	
	public void setRegistered(boolean value) { editor.putBoolean(IS_LOGIN, value); }
	

}