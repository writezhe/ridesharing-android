package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;



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
			
			HashMap<String, String> details = session.getUserDetails();
			String prefUserID = details.get(LoginSessionManager.KEY_ID);
			String prefPassword = details.get(LoginSessionManager.KEY_PASSWORD);
			Log.i("LoginActivity", prefUserID);
			Log.i("LoginActivity", prefPassword);

			// Logic begins here
			if(userIDString.trim().length() == 0) {
				AlertsManager.showAlert(appContext.getString(R.string.invalid_user_id), this);
			} else if (passwordString.trim().length() == 0) { // TODO: Debug - passwords need to be longer..
				AlertsManager.showAlert(appContext.getString(R.string.invalid_password), this);
			} else if(!userIDString.equals(prefUserID)) {
				AlertsManager.showAlert(appContext.getString(R.string.user_id_system_mismatch), this);
			} else if( !EncryptionEngine.hash( passwordString).equals( prefPassword ) ) {
				AlertsManager.showAlert(appContext.getString(R.string.password_system_mismatch), this);
			} else {	
				session.createLoginSession( userIDString, EncryptionEngine.hash( passwordString ) );
//				NetworkUtilities.pushIdentifyingData( userIDString, EncryptionEngine.hash( passwordString ) );
				startActivity( new Intent(appContext, DebugInterfaceActivity.class ) ); // TODO: Dori. Debug
				finish();
			}
		}
	}

	/**
	 * Switch to the forgot password screen.
	 * @param view
	 */
	public void forgotPassword(View view) {
		startActivity(new Intent(appContext, ResetPassword.class));
		finish();
	}
}
