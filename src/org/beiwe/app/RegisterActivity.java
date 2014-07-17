package org.beiwe.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


/**
 * Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dori Samet
 *
 */
@SuppressLint("ShowToast")
public class RegisterActivity extends Activity {
	
	private Context appContext;
	private EditText username;
	private EditText password;
	private EditText passwordRepeat;
	private SessionManager session;

	/**
	 * Users will go into this activity first to register information on the phone and on the server.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		// onCreate set variables
		appContext = getApplicationContext();
		username = (EditText) findViewById(R.id.username_box);
		password = (EditText) findViewById(R.id.password_box);
		passwordRepeat = (EditText) findViewById(R.id.repeat_password_box);
		session = new SessionManager(appContext);
	}
	
	/**
	 * Registration sequence begins here when the submit button is pressed.
	 * Normally there would be interaction with the server, in order to verify the user ID as well as the phone ID.
	 * Right now it does simple checks to see that the user actually inserted a value
	 * @param view
	 */
	@SuppressLint("ShowToast")
	public void registrationSequence(View view) {
		String usernameStr = username.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		// Logic gauntlet begins here
		if(usernameStr.length() == 0) {
			Utils.showAlert("Invalid username", this);
		} else if (passwordStr.length() == 0) {
			Utils.showAlert("Invalid password", this);
		} else if (passwordRepeatStr.length() == 0 || !passwordRepeatStr.equals(passwordStr)) {
			Utils.showAlert("Passwords mismatch", this);
		} else {
			
			// TODO: Hashing mechanism goes here!!
			Log.i("RegisterActivity", "Attempting to create a login session");
			session.createLoginSession(usernameStr, passwordStr);
			Log.i("RegisterActivity", "Registration complete, attempting to start DebugInterfaceActivity");
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		}
	}
}