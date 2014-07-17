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
	 * onCreate method. Nothing interesting happens here :)
	 * 
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Private variable set up
		appContext = getApplicationContext();
		session = new SessionManager(appContext);
		
		if (session.isLoggedIn()) {
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		} else {
			userName = (EditText) findViewById(R.id.editText1);
			passWord = (EditText) findViewById(R.id.editText2);
		
			TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
			textFieldKeyboard.makeKeyboardBehave(userName);
			textFieldKeyboard.makeKeyboardBehave(passWord);
		}
	}
	

	/**
	 * Logic that goes behind this method -
	 * IF the session is logged in (AKA shared preferences hold values) - keep the session logged in.
	 * ELSE The session is not logged in and we should wait for user input.
	 * 
	 * Notice there is a direct access to SharedPreferences.
	 * @param view
	 */
	public void loginSequence(View view) {
		if (session.isLoggedIn()) {
			Log.i("LoginActivity", "" + session.isLoggedIn());
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		} else {
			// TODO: Hashing mechanism goes here for username password combination
			String username = userName.getText().toString();
			String password = passWord.getText().toString();
			
			HashMap<String, String> details = session.getUserDetails();
			String prefUsername = details.get(SessionManager.KEY_NAME);
			String prefPassword = details.get(SessionManager.KEY_PASSWORD);
			Log.i("LoginActivity", prefUsername);
			Log.i("LoginActivity", prefPassword);
			
			// Logic begins here
			if(username.trim().length() > 0 && password.trim().length() > 0){
				if(!username.equals(prefUsername)) {
					Utils.showAlert("Invalid username", this);
					Toast.makeText(appContext, "Invalid username", 5).show();
				}
				else if(password.equals(prefPassword)){ 
					session.createLoginSession(username, password);
					startActivity(new Intent(appContext, DebugInterfaceActivity.class));
					finish();
				}else {Utils.showAlert("Incorrect username password combination", this);}
			} else {Utils.showAlert("Login Failed", this);} // In case login completely fails
		}
	}
	
	/**
	 * Switch to the forgot password screen, without losing the saved state.
	 * @param view
	 */
	public void forgotPassword(View view) {
		startActivity(new Intent(appContext, ForgotPassword.class));
		finish();
	}
}
