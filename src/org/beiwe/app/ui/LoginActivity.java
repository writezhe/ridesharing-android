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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A class used to log in to the app. Uses a helper class {@link LoginSessionManager.java}
 * @author Dori Samet
 *
 */

@SuppressLint({ "CommitPrefEdits", "ShowToast" })
public class LoginActivity extends Activity {

	private EditText userID;
	private EditText password;
	private LoginSessionManager session;
	private Context appContext;
	
	@Override
	
	/**
	 * onCreate method. If the user is already logged in for some reason, navigate to the {@link DebugInterfaceActivity.java}
	 * Otherwise, run normally.
	 * 
	 * This functionality is used, because I have yet to figure out how to shut down an activity from another activity.
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Private variable set up
		appContext = getApplicationContext();
		session = new LoginSessionManager(appContext);
		
		if (session.isLoggedIn()) {
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		} else {
			userID = (EditText) findViewById(R.id.editText1);
			password = (EditText) findViewById(R.id.editText2);
		
			TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
			textFieldKeyboard.makeKeyboardBehave(userID);
			textFieldKeyboard.makeKeyboardBehave(password);
		}
	}
	

	/**
	 * Logic that goes behind this method -
	 * IF the session is logged in (AKA shared preferences hold values) - keep the session logged in.
	 * ELSE The session is not logged in and we should wait for user input.
	 * 
	 * Notice there is a direct access to SharedPreferences.
	 * @param view
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void loginSequence(View view) {
		if (session.isLoggedIn()) {
			Log.i("LoginActivity", "" + session.isLoggedIn());
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		} else {
			// Strings that have to do with username and password
			String userIDString = userID.getText().toString();
			String passwordString = password.getText().toString();
			String encryptedPassword = EncryptionEngine.hash(passwordString);
			
			HashMap<String, String> details = session.getUserDetails();
			String prefUserID = details.get(LoginSessionManager.KEY_ID);
			String prefPassword = details.get(LoginSessionManager.KEY_PASSWORD);
			Log.i("LoginActivity", prefUserID);
			Log.i("LoginActivity", prefPassword);
			
			// Logic begins here
			if(userIDString.trim().length() > 0 && passwordString.trim().length() > 0){
				if(!userIDString.equals(prefUserID)) {
					AlertsManager.showAlert("User ID does not match the one in the system. Try again", this);
				}
				else if(encryptedPassword.equals(prefPassword)){ 
					session.createLoginSession(userIDString, encryptedPassword);
					Upload.pushDataToServer(userIDString, encryptedPassword);
					startActivity(new Intent(appContext, DebugInterfaceActivity.class));
					finish();
				} else { AlertsManager.showAlert("Incorrect user ID and password combination", this);}
			} else { AlertsManager.showAlert("Login Failed due to a field being too short", this); } // In case login completely fails
		}
	}
	
	/**
	 * Switch to the forgot password screen.
	 * @param view
	 */
	public void forgotPassword(View view) {
		startActivity(new Intent(appContext, ForgotPassword.class));
		finish();
	}
}
