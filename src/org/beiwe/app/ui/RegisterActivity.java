package org.beiwe.app.ui;

import org.beiwe.app.DeviceInfo;
import org.beiwe.app.LoadingActivity;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.QuestionsDownloader;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


/**Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dor Samet, Eli Jones */

@SuppressLint("ShowToast")
public class RegisterActivity extends RunningBackgroundProcessActivity {
	// extends RunningBackgroundProcessActivity

	// Private fields
	private EditText userID;
	private EditText password;
	private String newPassword;

	/** Users will go into this activity first to register information on the phone and on the server. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		userID = (EditText) findViewById(R.id.registerUserIdInput);
		password = (EditText) findViewById(R.id.registerTempPasswordInput);

		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard( getApplicationContext() );
		textFieldKeyboard.makeKeyboardBehave(userID);
		textFieldKeyboard.makeKeyboardBehave(password);
	}

	/**Registration sequence begins here, called when the submit button is pressed.
	 * Normally there would be interaction with the server, in order to verify the user ID as well as the phone ID.
	 * Right now it does simple checks to see that the user actually inserted a value.
	 * @param view */
	@SuppressLint("ShowToast")
	public synchronized void registrationSequence(View view) {
		String userIDStr = userID.getText().toString();
		String passwordStr = password.getText().toString();

		EditText newPasswordInput = (EditText) findViewById(R.id.registerNewPasswordInput);
		EditText confirmNewPasswordInput = (EditText) findViewById(R.id.registerConfirmNewPasswordInput);
		newPassword = newPasswordInput.getText().toString();
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		// If the user id length is too short, alert the user
		if(userIDStr.length() == 0) {
			AlertsManager.showAlert( getString(R.string.invalid_user_id), this); }

		// If the new password doesn't match the confirm new password
		else if (!newPassword.equals(confirmNewPassword)) {
			AlertsManager.showAlert( getString(R.string.password_mismatch), this); }
		
		// If the new password has too few characters, pop up an alert, and do nothing else
		if (!LoginManager.passwordMeetsRequirements(newPassword, this)) {
			return; }

		// If the password length is too short, alert the user
		else if ( LoginManager.passwordMeetsRequirements(passwordStr, this) ) {
			
			LoginManager.setLoginCredentials(userIDStr, passwordStr);
			
			Log.d("RegisterActivity", "trying \"" + LoginManager.getPatientID() + "\" with password \"" + LoginManager.getPassword() + "\"" );
			doRegister(getApplicationContext().getString(R.string.register_url));
		}
	}
	
	
	//Aww yeuh. 
	private void doRegister(final String url) { new HTTPAsync(url, this) {
		@Override
		protected Void doInBackground(Void... arg0) {
			parameters = PostRequest.makeParameter("bluetooth_id", DeviceInfo.getBlootoothMAC()) +
					PostRequest.makeParameter("new_password", newPassword);
			response = PostRequest.httpRegister(parameters, url);
			return null; //hate
		}
		
		@Override
		protected void onPostExecute(Void arg) {
			if (response == 200) { 
				LoginManager.setRegistered(true);
				LoginManager.setPassword(newPassword);
				LoginManager.loginOrRefreshLogin();

				// Download the survey questions and schedule the surveys
				QuestionsDownloader downloader = new QuestionsDownloader(activity.getApplicationContext());
				downloader.downloadJsonQuestions();

				backgroundProcess.startTimers();

				/* Create new data files, because now the app now has a patientID to prepend to
				 * those files' names, instead of NULL_ID */
				TextFileManager.makeNewFilesForEverything();

				// Start the Main Screen Activity, and kill the RegisterActivity screen
				activity.startActivity(new Intent(activity.getApplicationContext(), LoadingActivity.loadThisActivity) );
				activity.finish();
			}
			else if (response == 2) {
				AlertsManager.showAlert( "Received an invalid encryption key, please contact your administrator.", this.activity );
				super.onPostExecute(arg);
			}
			else {
				AlertsManager.showAlert( getString(R.string.couldnt_register), this.activity );
				super.onPostExecute(arg);
			}
		}
	};}
}