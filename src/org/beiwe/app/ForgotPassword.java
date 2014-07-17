package org.beiwe.app;

import java.util.HashMap;

import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A class to manage users who forgot their passwords
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
	private Button submit;
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
		submit = (Button) findViewById(R.id.forgot_password_submit_button);
		session = new SessionManager(appContext);
		
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(username);
		textFieldKeyboard.makeKeyboardBehave(userId);
		textFieldKeyboard.makeKeyboardBehave(password);
		textFieldKeyboard.makeKeyboardBehave(passwordRepeat);

	}
	

	public void forgotPasswordSequence(View v) {
		String usernameStr = username.getText().toString();
		String userIdStr = userId.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		HashMap<String, String> details = session.getUserDetails();

		// Gauntlet begins here 
		// TODO: this logic is incorrect, check out tomorrow!
		if(usernameStr.trim().length() <= 0 || details.containsKey(usernameStr)) {
			Utils.showAlert("Invalid username, try again",  this);
		} else if (userIdStr.trim().length() <= 0) {
			Utils.showAlert("Invalid user ID, try again", this);
		} else if (passwordStr.trim().length() <= 0) {
			if(details.get(usernameStr) != null && details.get(usernameStr) != passwordStr) {
				Utils.showAlert("Invalid password, try again", this);
			}
		} else if (!passwordRepeatStr.equals(passwordStr)) {
			Utils.showAlert("Passwords mismatch, try again", this);
		} else {
			session.createLoginSession(usernameStr, passwordStr);
			Intent intent = new Intent(appContext, LoginActivity.class);
            intent.putExtra("User Logged in", true);
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		}
	}
}
