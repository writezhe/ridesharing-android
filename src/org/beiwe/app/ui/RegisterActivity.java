package org.beiwe.app.ui;

import java.util.List;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.networking.PostRequestFileUpload;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;


/**Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dori Samet */
@SuppressLint("ShowToast")
public class RegisterActivity extends Activity {

	// Private fields
	private Context appContext;
	private EditText userID;
	private EditText password;
	private EditText passwordRepeat;
	private LoginSessionManager session;
	private ProgressBar bar;
	private String response;
	private Activity mActivity;

	/** Users will go into this activity first to register information on the phone and on the server. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// onCreate set variables
		appContext = getApplicationContext();
		userID = (EditText) findViewById(R.id.userID_box);
		password = (EditText) findViewById(R.id.password_box);
		passwordRepeat = (EditText) findViewById(R.id.repeat_password_box);
		session = new LoginSessionManager(appContext);

		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(userID);
		textFieldKeyboard.makeKeyboardBehave(password);
		textFieldKeyboard.makeKeyboardBehave(passwordRepeat);
	}

	/**Registration sequence begins here, called when the submit button is pressed.
	 * Normally there would be interaction with the server, in order to verify the user ID as well as the phone ID.
	 * Right now it does simple checks to see that the user actually inserted a value.
	 * @param view */
	@SuppressLint("ShowToast")
	public synchronized void registrationSequence(View view) {
		String userIDStr = userID.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		// TODO: Dori/Eli There needs to be more logic here to prevent false registration
		// server side: this should call a function that checks if the user id is a valid id for the study, then create the user, then return 200 ok.
		if(userIDStr.length() == 0) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_user_id), this);
		} else if (passwordStr.length() == 0) { // TODO: Debug - passwords need to be longer..
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_password), this);
		} else if (!passwordRepeatStr.equals(passwordStr)) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.password_mismatch), this);
		} else {
			setActivity(this);
			Log.i("RegisterActivity", "Attempting to create a login session");
			session.createLoginSession(userIDStr, EncryptionEngine.hash(passwordStr));
			setUpProgressBar();
			makeNetworkRequest();
		}

		
		/*
		 * Psuedocode for Registration:
		 * saveStuffToPhone(userIDstr, encryptedPassword) (isRegistered = NO)
		 * Async push to server:
		 * 		doInBackground:
		 * 			parameters(bluetoothID)
		 * 			postRequest(parameters, registerURL)
		 * 			return getResponse()
		 * 
		 * 		onPostExecute:
		 * 			If 200:
		 * 				startActivity(MainMenuActivity)
		 * 			Else:
		 * 				error = NetworkUtilities.handleResponse(response)
		 * 				AlertsManager.displayAlert(error)
		 * 
		 * 		
		 * 
		 */
		
		/*
		 * if user registration is valid
		 * request key
		 * wait on that request
		 * write key file.
		 * run key file test function.
		 * if key tests pass... start background service.
		 */

		//TODO: Eli. add functions in the server to check and return http codes for each case.
		// invalid patient id
		// valid patient id
		// valid patient id, but another device is already registered

		//TODO: Eli/Josh/Eli. handle other types of network error and message display.
		// server errors (500 codes)
		// dns lookup errors
		// I'm a teapot errors?
	}
	
	public void makeNetworkRequest() {
		RegisterPhoneLoader loader = new RegisterPhoneLoader();
		loader.execute();
	}
	
	public void setUpProgressBar() {
		bar = (ProgressBar) findViewById(R.id.progressBar);

	}
	
	private class RegisterPhoneLoader extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute() {
			bar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			String parameters = "&patient_id=" + userID.getText().toString() + 
					"&password="  + password.getText().toString() +  
					"&device_id=" + "test_device"; 
			response = PostRequestFileUpload.make_request_on_async_thread(parameters, "http://beiwe.org/test");
			Log.i("RegisterActivity", "RESPONSE = " + response);
			return null;
		} 
		
		@Override
		protected void onPostExecute(Void result) { // Indentation 2
			bar.setVisibility(View.GONE);
			if (response.equals("200")) { // Indentation 3
				startActivity(new Intent(appContext, DebugInterfaceActivity.class));
				finish();				
			} else { // ...
				getCurrentActivity().runOnUiThread(new Runnable() {
					public void run() {
						AlertsManager.showAlert(NetworkUtilities.handleServerResponses(response), getCurrentActivity()); // Indentation ZOMG... Ewww Urgghhhhh.
					}
				});
			}
		}
	}
	
	public Activity getCurrentActivity() {
		return mActivity;
	}
	
	public void setActivity(Activity activity) {
		mActivity = activity;
	}

}