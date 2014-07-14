package org.beiwe.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
	private Button login;
	private SessionManager session;
	
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
		session = new SessionManager(getApplicationContext());
		userName = (EditText) findViewById(R.id.editText1);
		passWord = (EditText) findViewById(R.id.editText2);
		login = (Button) findViewById(R.id.button1);
		
		// Logic for log in starts here
		if (session.isLoggedIn()) {
			Log.i("LoginActivity", "" + session.isLoggedIn());
			startActivity(new Intent(this, DebugInterfaceActivity.class) );
			finish();
		} else {
			login.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO: Hashing mechanism goes here for username password combination
					String username = userName.getText().toString();
					String password = passWord.getText().toString();
					if(username.trim().length() > 0 && password.trim().length() > 0){
						if(username.equals("test") && password.equals("test")){ // Needs to be hashed
							session.createLoginSession(username, password);
							Intent i = new Intent(getApplicationContext(), DebugInterfaceActivity.class);
							startActivity(i);
							finish();
						} 
						else {Toast.makeText(getApplicationContext(), "Incorrect user-password combination", 5).show();} 
					} else {Toast.makeText(getApplicationContext(), "Login Failed", 5).show();}
				}
			});
		}
	}
}
