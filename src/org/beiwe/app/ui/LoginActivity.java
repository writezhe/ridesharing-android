package org.beiwe.app.ui;

import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginSessionManager;
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


//TODO: Eli.  Make sure this doc is correct.
/**Ui presents an interface-less loading activity to the user.  Moves user to the correct activity based on application state.
 * Logs the User into the app, handles correct loading timing of various app components.
 * Helper class {@link LoginSessionManager.java}
 * @authors Dor Samet, Eli Jones */

@SuppressLint({ "CommitPrefEdits", "ShowToast" })
public class LoginActivity extends Activity {
	
	private EditText userID;
	private EditText password;
	private LoginSessionManager loginSessionManager;
	private Context appContext;
	
	@Override
	//TODO: Eli. Update behavior to load users choice of activity (for debugging)?
	//TODO: Eli.  research whether one activity can close another activity, that is a confused comment.
	/**If the user is already logged in navigate to the DebugInterfaceActivity {@link DebugInterfaceActivity.java}
	 * Otherwise load Login Screen.
	 * >>>> This functionality is used, because I have yet to figure out how to shut down an activity from another activity. */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		//TODO: Eli/Josh.  Talk to josh about how/when rotate device calls the oncreate method.
		// Private variable set up
		appContext = getApplicationContext();
		loginSessionManager = new LoginSessionManager(appContext);

		if (loginSessionManager.isLoggedIn()) {
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish(); //TODO: Eli. Research exact functionality of Finish()
		} else {
			userID = (EditText) findViewById(R.id.editText1);
			password = (EditText) findViewById(R.id.editText2);

			TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
			textFieldKeyboard.makeKeyboardBehave(userID);
			textFieldKeyboard.makeKeyboardBehave(password);
		}
	}
	
	
	/**IF session is logged in (value in shared prefs), keep the session logged in.
	 * IF session is not logged in, wait for user input.
	 * @param view*/
	public void loginSequence(View view) {
		if (loginSessionManager.isLoggedIn()) {
			Log.i("LoginActivity", "" + loginSessionManager.isLoggedIn());
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		//TODO: Eli. check the finish() statement gets run in all relevant cases.
		} else {
			String userIDString = userID.getText().toString();
			String passwordString = password.getText().toString();
			
			HashMap<String, String> details = loginSessionManager.getUserDetails();
			String prefUserID = details.get(LoginSessionManager.KEY_ID);
			String prefPassword = details.get(LoginSessionManager.KEY_PASSWORD);
			Log.i("LoginActivity", prefUserID);
			Log.i("LoginActivity", prefPassword);

			//Check password length, user id, hashed password validity,
			//TODO: Eli. add check device id.
			if(userIDString.trim().length() == 0) {
				AlertsManager.showAlert(appContext.getString(R.string.invalid_user_id), this);
			} else if ( passwordString.trim().length() == 0 ) { // TODO: CHANGE TO ~6 BEFORE PRODUCTON.
				AlertsManager.showAlert(appContext.getString(R.string.invalid_password), this);
			} else if( !userIDString.equals(prefUserID) ) {
				AlertsManager.showAlert(appContext.getString(R.string.user_id_system_mismatch), this);
			} else if( !EncryptionEngine.safeHash( passwordString).equals( prefPassword ) ) {
				AlertsManager.showAlert(appContext.getString(R.string.password_system_mismatch), this);
			} else {	
				// Unlike registration activity, this one does not check against the server
				//TODO: Eli make createLoginSession return a bool, it will compare the password correctly, drop the above password check logic, solve positioning of finish() to correct resulting problems.  make sure this still works while modularizing the password check code.
				loginSessionManager.createLoginSession( userIDString, EncryptionEngine.safeHash( passwordString ) );
				startActivity( new Intent(appContext, DebugInterfaceActivity.class ) ); // TODO: Eli. there was a "debug" comment here, why?
				finish();
			}
		}
	}
	
	
	/**Move user to the forgot password activity.
	 * @param view */
	public void forgotPassword(View view) {
		startActivity(new Intent(appContext, ForgotPasswordActivity.class));
		finish();
	}
}
