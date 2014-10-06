package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.beiwe.app.R;
import org.beiwe.app.networking.AsyncPostSender;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
public class ResetPasswordActivity extends Activity {
	
	private Context appContext;
	private EditText oldPass;
	private EditText newPassword;
	private EditText newPasswordRepeat;
	private LoginSessionManager session;
	
	/**
	 * onCreate method. Nothing interesting happens here...
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		
		// This is the variable assignment section
		appContext = getApplicationContext();
		oldPass = (EditText) findViewById(R.id.reset_password_old_password);
		newPassword = (EditText) findViewById(R.id.reset_password_password);
		newPasswordRepeat = (EditText) findViewById(R.id.reset_password_password_repeat);
		session = new LoginSessionManager(appContext);
		
		// Make keyboard behavior nicely - when clicking outside of the textbox, keyboard disappears
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(oldPass);
		textFieldKeyboard.makeKeyboardBehave(newPassword);
		textFieldKeyboard.makeKeyboardBehave(newPasswordRepeat);
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
	public void resetPasswordSequence(View view) {
		// Encapsulated user's details as saved in the SharedPreferences
		HashMap<String, String> details = session.getUserDetails();
		
		// Old password, and old password hashed
		String oldPassStr = oldPass.getText().toString();
		String oldPassStrHash = EncryptionEngine.safeHash(oldPassStr);
		
		// New password that will be pushed to the server
		String newPasswordStr = newPassword.getText().toString();
		String newPasswordRepeatStr = newPasswordRepeat.getText().toString();

		// Cases: username mismatch, userID mismatch, passwords mismatch, and repeat password with actual password mismatch. 
		if(oldPassStr.length() == 0 || !oldPassStrHash.equals(details.get(LoginSessionManager.KEY_PASSWORD))) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_old_password), this);
		} else if (newPasswordStr.length() == 0) { // TODO: Debug - passwords need to be longer..
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_password), this);
		} else if (!newPasswordRepeatStr.equals(newPasswordStr)) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.password_mismatch), this);
		} else {
			Log.i("ResetPassword", "Attempting to create a login session");
			session.createLoginSession(details.get(LoginSessionManager.KEY_ID), EncryptionEngine.safeHash(newPasswordStr));
			makeResetPasswordThread(newPasswordStr);
			Log.i("ResetPassword", "Password Reset successfully. Returning to previous activity");
//			AlertsManager.showAlert(appContext.getResources().getString(R.string.pass_reset_complete), this);
//			finish();
		}
	}
	private void makeResetPasswordThread(String newPassword) {
		new AsyncPostSender("http://beiwe.org/set_password", this, session, EncryptionEngine.safeHash(newPassword)).execute();		
	}
	
//	/**
//	 * This happens when the user presses "back".
//	 * 
//	 * Moves the user back from this activity to the login activity.
//	 * Will change this logic if I find out how to close an activity from another activity.
//	 */
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    if (keyCode == KeyEvent.KEYCODE_BACK) {
//	        startActivity(new Intent(appContext, DebugInterfaceActivity.class));
//	        finish();
//	        return true;
//	    }
//	    return super.onKeyDown(keyCode, event);
//	}
}	