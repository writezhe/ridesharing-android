package org.beiwe.app;

import java.util.HashMap;

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
 * A class used to log in to the app. Uses a helper class {@link SessionManager.java}
 * @author Dori Samet
 *
 */

@SuppressLint({ "CommitPrefEdits", "ShowToast" })
public class LoginActivity extends Activity {

	private EditText userName;
	private EditText passWord;
	private SessionManager session;
	private Context appContext;
	
	@Override
	
	/**
	 * onCreate method. Logic that goes behind this method -
	 * IF the session is logged in (AKA shared preferences hold values) - keep the session logged in.
	 * ELSE The session is not logged in and we should wait for user input.
	 * Waiting has its own logic that WILL BE CHANGED because we need to hash user-password combos.
	 * 
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Private variable set up
		appContext = getApplicationContext();
		session = new SessionManager(appContext);
		userName = (EditText) findViewById(R.id.editText1);
		passWord = (EditText) findViewById(R.id.editText2);
		
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(userName);
		textFieldKeyboard.makeKeyboardBehave(passWord);
	}
	
	
	public void loginSequence(View view) {
		if (session.isLoggedIn()) {
			Log.i("LoginActivity", "" + session.isLoggedIn());
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
		} else {
			// TODO: Hashing mechanism goes here for username password combination
			String username = userName.getText().toString();
			String password = passWord.getText().toString();
			
			HashMap details = session.getUserDetails();
			
			if(username.trim().length() > 0 && password.trim().length() > 0){
				if(!details.containsKey(username)) {
					
				}
				if(username.equals("test") && password.equals("test")){ // Needs to be hashed
					session.createLoginSession(username, password);
					startActivity(new Intent(appContext, DebugInterfaceActivity.class));
					finish();
				} 
				else {Toast.makeText(appContext, appContext.getResources().getString(R.string.incorrect_user_pass_combo), 5).show();} 
			} 
			else {Toast.makeText(appContext, "Login Failed", 5).show();} // In case login completely fails
		}
	}
	
	public void forgotPassword(View view) {
		startActivity(new Intent(appContext, ForgotPassword.class));
	}
}
