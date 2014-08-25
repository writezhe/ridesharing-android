package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.beiwe.app.R;
import org.beiwe.app.storage.EncryptionEngine;
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
	private EditText userId;
	private EditText password;
	private EditText passwordRepeat;
	private LoginSessionManager session;
	
	/**
	 * onCreate method. Nothing interesting happens here...
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		
		// This is the variable assignment section
		appContext = getApplicationContext();
		userId = (EditText) findViewById(R.id.forgot_password_uid);
		password = (EditText) findViewById(R.id.forgot_password_password);
		passwordRepeat = (EditText) findViewById(R.id.forgot_password_password_repeat);
		session = new LoginSessionManager(appContext);
		
		// Make keyboard behavior nicely - when clicking outside of the textbox, keyboard disappears
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(userId);
		textFieldKeyboard.makeKeyboardBehave(password);
		textFieldKeyboard.makeKeyboardBehave(passwordRepeat);
	}
	
	/**
	 *  This happens when a user presses the submit button.
	 *  
	 *  Each time there is an error, such like an incorrect username, the program will throw an alert,
	 *  informing the user of the error.
	 *  
	 *  If the user succeeds in logging in, the activity finishes.
	 * @param view
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void forgotPasswordSequence(View view) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		// Variable assignments
		String userIdStr = userId.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();
		String encryptedPassword = EncryptionEngine.hash(passwordStr);

		// Encapsulated user's details as saved in the SharedPreferences
		HashMap<String, String> details = session.getUserDetails();
		
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(LoginSessionManager.KEY_ID, "Bobby McGee"));
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(LoginSessionManager.KEY_PASSWORD, "Bobby McGee"));
		Log.i("ForgotPassword_getDetails", details.get(LoginSessionManager.KEY_PASSWORD));

		// Cases: username mismatch, userID mismatch, passwords mismatch, and repeat password with actual password mismatch. 
		if (userIdStr.trim().length() <= 0) {
			AlertsManager.showAlert( "Invalid user ID, try again", this );
		} else if( ! ( details.get( LoginSessionManager.KEY_ID ).equals(userIdStr.trim() ) ) ) {
			if( !details.get( LoginSessionManager.KEY_PASSWORD ).equals( encryptedPassword ) ) {
				AlertsManager.showAlert( "Invalid password, try again", this );
			}
		} else if ( ! (passwordRepeatStr.equals( passwordStr) ) ) {
			AlertsManager.showAlert( "Passwords mismatch, try again", this );
		} else { // Start new activity
			session.createLoginSession( userIdStr, encryptedPassword );
			startActivity( new Intent(appContext, LoginActivity.class ) );
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