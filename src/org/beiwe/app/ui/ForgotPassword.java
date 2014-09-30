package org.beiwe.app.ui;

import java.util.HashMap;

import org.beiwe.app.R;
import org.beiwe.app.R.id;
import org.beiwe.app.R.layout;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ForgotPassword extends Activity {
	
	private Context appContext;
	private LoginSessionManager session;
	
	private EditText newPassword;
	private EditText newPasswordRepeat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);
		
		// Variable assignment
		appContext = getApplicationContext();
		newPassword = (EditText) findViewById(R.id.forgot_password_enter_password);
		newPasswordRepeat = (EditText) findViewById(R.id.forgot_password_enter_password_repeat);
		session = new LoginSessionManager(appContext);
		
		// Make keyboard behavior
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(newPassword);
		textFieldKeyboard.makeKeyboardBehave(newPasswordRepeat);


	}
	
	/** This method is used when trying to access the app after a patient loses their password.
	 * 
	 * The user calls the researchers in order for them to reset their password. Afterwards, the patient will insert their password to the
	 * noted fields, and click submit. If the password is invalid, or mismatches with the repeated password, then the app will post
	 * an alert notifying the patient. Otherwise, the app will try to upload the hashed password to the server, and see if the password
	 * the user entered matches the one on the server. If we receive a 200, the patient will be logged in with their new password now
	 * being their log in password. It is advised that the patient will change their password using the reset password option given in the app.
	 * 
	 * Assumptions:
	 * 		- The researchers will know how to operate the system, and know how to generate a new answer
	 * 		- The researchers will notify the patient of their new password
	 * 		- The researchers know how to identify their patients
	 * 
	 * @param view */
	public void registerNewPassword(View view) {
		
		// Variable assignments
		String passwordStr = newPassword.getText().toString();
		String passwordRepeatStr = newPasswordRepeat.getText().toString();
		String encryptedPassword = EncryptionEngine.hash(passwordStr);
		// TODO: Change to longer length...
		if (passwordStr.length() < 0) {
			AlertsManager.showAlert("Invalid password", this);
		} else if (!passwordStr.equals(passwordRepeatStr)) {
			AlertsManager.showAlert("Passwords mismatch", this);
		} else {
			// User entered passwords that match - time to check the encrypted password against the server
//			String response = NetworkUtilities.checkPasswordsIdentical(session.getUserDetails().get(LoginSessionManager.KEY_ID), encryptedPassword);
//			if (response == "502") {
				AlertsManager.showAlert("Connection timed out. Check you are connected to the internet", this);
//			} else if (response == "403") {
				// Received something other than 200 OK, something is wrong.
				AlertsManager.showAlert("The passwords you entered is not the password that is in the database. Please contact a researcher", this);
//			} else if (response == "405") {
				AlertsManager.showAlert("This phone is not the phone registered with this user. Please contact the research team", this);
//			} else {
				// Everything checks out - begin log in session
				HashMap<String, String> details = session.getUserDetails();
				session.createLoginSession(details.get(LoginSessionManager.KEY_ID), encryptedPassword);
				Intent mainMenu = new Intent(appContext, LoadingActivity.class);
				mainMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mainMenu);
				finish();
			}
		}
	}
//}
