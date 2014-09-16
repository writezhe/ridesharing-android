package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.Upload;
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
	public void forgotPasswordSequence(View view) {
		// Variable assignments
		String userIdStr = userId.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		// Encapsulated user's details as saved in the SharedPreferences
		HashMap<String, String> details = session.getUserDetails();
		
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(LoginSessionManager.KEY_ID, "Bobby McGee"));
		Log.i("ForgotPassword", appContext.getSharedPreferences("BeiwePref", 0).getString(LoginSessionManager.KEY_PASSWORD, "Bobby McGee"));
		Log.i("ForgotPassword_getDetails", details.get(LoginSessionManager.KEY_PASSWORD));

		// Cases: username mismatch, userID mismatch, passwords mismatch, and repeat password with actual password mismatch. 
		if(userIdStr.length() == 0) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_user_id), this);
		} else if (passwordStr.length() == 0) { // TODO: Debug - passwords need to be longer..
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_password), this);
		} else if (!passwordRepeatStr.equals(passwordStr)) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.password_mismatch), this);
		} else {
			Log.i("RegisterActivity", "Attempting to create a login session");
			session.createLoginSession(userIdStr, EncryptionEngine.hash(passwordStr));
			Upload.pushDataToServer(userIdStr, EncryptionEngine.hash(passwordStr));
			Log.i("RegisterActivity", "Registration complete, attempting to start DebugInterfaceActivity");
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
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