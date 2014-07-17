package org.beiwe.app;

import java.util.HashMap;

import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * A class to manage users who forgot their passwords. For future references, this should work very similar to
 * the register class, and will work with the server.
 * 
 * @author Dori Samet
 *
 */

@SuppressLint("ShowToast")
public class ForgotPassword extends Activity {
	
	private Context appContext;
	private EditText username;
	private EditText userId;
	private EditText password;
	private EditText passwordRepeat;
	private SessionManager session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		
		appContext = getApplicationContext();
		username = (EditText) findViewById(R.id.forgot_password_username);
		userId = (EditText) findViewById(R.id.forgot_password_uid);
		password = (EditText) findViewById(R.id.forgot_password_password);
		passwordRepeat = (EditText) findViewById(R.id.forgot_password_password_repeat);
		session = new SessionManager(appContext);
		
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(username);
		textFieldKeyboard.makeKeyboardBehave(userId);
		textFieldKeyboard.makeKeyboardBehave(password);
		textFieldKeyboard.makeKeyboardBehave(passwordRepeat);
		
		HashMap<String, String> details = session.getUserDetails();
		
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(SessionManager.KEY_NAME, "Bobby McGee"));
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(SessionManager.KEY_PASSWORD, "Bobby McGee"));
		Log.i("ForgotPassword_getDetails", details.get(SessionManager.KEY_NAME));

	}
	
	/**
	 *  This happens when a user presses the submit button.
	 *  
	 *  Each time there is an error, such like an incorrect username, the program will throw an alert,
	 *  informing the user of the error.
	 *  
	 *  If the user succeeds in logging in, the activity finishes.
	 * @param v
	 */
	public void forgotPasswordSequence(View v) {
		// Variable assignments
		
		String usernameStr = username.getText().toString();
		String userIdStr = userId.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		HashMap<String, String> details = session.getUserDetails();
		
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(SessionManager.KEY_NAME, "Bobby McGee"));
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(SessionManager.KEY_PASSWORD, "Bobby McGee"));
		Log.i("ForgotPassword_getDetails", details.get(SessionManager.KEY_PASSWORD));

		// Gauntlet begins here 
		if(!(details.get(SessionManager.KEY_NAME).equals(usernameStr.trim()))) {
			Utils.showAlert("Invalid username, try again",  this);
		} else if (userIdStr.trim().length() <= 0) {
			Utils.showAlert("Invalid user ID, try again", this);
		} else if(!(details.get(SessionManager.KEY_NAME).equals(usernameStr.trim()))) {
			if(details.get(SessionManager.KEY_PASSWORD) != null && details.get(usernameStr) != passwordStr) {
				Utils.showAlert("Invalid password, try again", this);
			}
		} else if (!(passwordRepeatStr.equals(passwordStr))) {
			Utils.showAlert("Passwords mismatch, try again", this);
		} else {
			session.createLoginSession(usernameStr, passwordStr);
			startActivity(new Intent(appContext, LoginActivity.class));
			finish();
		}
	}
	
	/**
	 * This happens when the user presses "back".
	 * 
	 * Moves the user back from this activity to the login activity.
	 * Will change this logic if I find out how to close an activity from another activity.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        startActivity(new Intent(appContext, LoginActivity.class));
	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}	