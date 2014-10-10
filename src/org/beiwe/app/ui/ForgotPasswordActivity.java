package org.beiwe.app.ui;

import java.util.HashMap;

import org.beiwe.app.R;
import org.beiwe.app.R.id;
import org.beiwe.app.R.layout;
import org.beiwe.app.networking.AsyncPostSender;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
/**
 * 
 * @author Dor Samet
 *
 */
public class ForgotPasswordActivity extends Activity {

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
		String encryptedPassword = EncryptionEngine.safeHash(passwordStr);
		// TODO: Change to longer length...
		if (passwordStr.length() < 0) {
			AlertsManager.showAlert("Invalid password", this);
		} else if (!passwordStr.equals(passwordRepeatStr)) {
			AlertsManager.showAlert("Passwords mismatch", this);
		} else {
			HashMap<String, String> details = session.getUserDetails();
			// TODO: Dori. We need to send the raw password for testing. Obviously, this will change before production, but for now this is what happens
			session.createLoginSession(details.get(LoginSessionManager.KEY_ID), encryptedPassword);
			new AsyncPostSender("http://beiwe.org/forgot_password", this, session).execute();
		}
	}
}
//}
