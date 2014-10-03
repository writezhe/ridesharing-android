package org.beiwe.app.ui;

import java.util.List;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;
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
	
	// Private static field
	private static Activity mActivity;


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
			session.createLoginSession(userIDStr, EncryptionEngine.hash(passwordStr));
			setUpProgressBar();
			NetworkUtilities util = new NetworkUtilities(appContext);
			makeNetworkRequest();
			Log.i("RegisterActivity", "Attempting to create a login session");
			Log.i("RegisterActivity", userIDStr);
		}
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
			String parameters = NetworkUtilities.makeFirstTimeParameters();
			response = PostRequest.make_request_on_async_thread(parameters, "http://beiwe.org/register_user");
			return null;
		} 
		
		@Override
		protected void onPostExecute(Void result) { // Indentation 2
			bar.setVisibility(View.GONE);
			if (response.equals("200")) { // Indentation 3
				session.setRegistered(true);
				startActivity(new Intent(appContext, DebugInterfaceActivity.class));
				finish();
			} else { 				
				getCurrentActivity().runOnUiThread(new Runnable() {
					public void run() {
						AlertsManager.showAlert(NetworkUtilities.handleServerResponses(response), getCurrentActivity()); // Indentation ZOMG... Ewww Urgghhhhh.
					}
				});
			}
		}
	}
	
	public static Activity getCurrentActivity() {
		return mActivity;
	}
	
	public static void setActivity(Activity activity) {
		mActivity = activity;
	}
}