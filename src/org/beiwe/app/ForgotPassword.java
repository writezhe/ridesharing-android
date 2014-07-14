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

		submit.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("ShowToast")
			@Override
			public void onClick(View v) {
				String usernameStr = username.getText().toString();
				String userIdStr = userId.getText().toString();
				String passwordStr = password.getText().toString();
				String passwordRepeatStr = passwordRepeat.getText().toString();
				
				HashMap<String, String> details = session.getUserDetails();
				
				// Gauntlet begins here 
				if(usernameStr.trim().length() <= 0 || details.containsKey(usernameStr)) {
					Toast.makeText(appContext, "Invalid username, try again", 5).show();
				} else if (userIdStr.trim().length() <= 0) {
					Toast.makeText(appContext, "Invalid user ID, try again", 5).show();
				} else if (passwordStr.trim().length() <= 0) {
					if(details.get(usernameStr) != null && details.get(usernameStr) != passwordStr) {
						Toast.makeText(appContext, "Invalid password, try again", 5).show();
					}
				} else if (!passwordRepeatStr.equals(passwordStr)) {
					Toast.makeText(appContext, "Passwords mismatch, try again", 5).show();
				} else {
					session.createLoginSession(usernameStr, passwordStr);
					startActivity(new Intent(appContext, DebugInterfaceActivity.class));
					finish();
				}
			}
		});
		
	}
}
