package org.beiwe.app.ui;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginManager;
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


//TODO: Eli.  update doc.
/**Ui presents an interface-less loading activity to the user.  Moves user to the correct activity based on application state.
 * Logs the User into the app, handles correct loading timing of various app components.
 * Helper class {@link LoginManager.java}
 * @authors Dor Samet, Eli Jones */

@SuppressLint({ "CommitPrefEdits", "ShowToast" })
public class LoginActivity extends Activity {
	
	private EditText password;
	private Context appContext;
	
	
	@Override
	//TODO: move logic about determining where to send a user to the loadingActivity.
	/**The login activity Always prompts the user for the password. */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		appContext = getApplicationContext();
		
		password = (EditText) findViewById(R.id.editText2);
		
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(password);
		
		//TODO: Eli+Josh. this should never, ever happen.
		if ( !LoginManager.isRegistered() ) {
			Log.e("LoginActivity", "WOW you fucked something up, this device is not even registered.");
			System.exit(1);
		}
	}
	
	
	/**IF session is logged in (value in shared prefs), keep the session logged in.
	 * IF session is not logged in, wait for user input.
	 * @param view*/
	public void loginSequence(View view) {		
		if ( LoginManager.checkPassword( password.getText().toString() ) ) {
			LoginManager.setLoggedIn(true);
			//TODO: Eli (or Josh), this needs to point at the correct activity.
			startActivity( new Intent( appContext, DebugInterfaceActivity.class ) ); // TODO: Eli. there was a "debug" comment here, why?
			finish();
		}
		AlertsManager.showAlert("Incorrect password", this);
	}
	
	
	/**Move user to the forgot password activity.
	 * @param view */
	public void forgotPassword(View view) {
		startActivity( new Intent(appContext, ForgotPasswordActivity.class) );
		finish();
	}
}
