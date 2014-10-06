package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.networking.NetworkUtilities;
import org.beiwe.app.networking.AsyncPostSender;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
//	private ProgressBar bar;
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
 
		if(userIDStr.length() == 0) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_user_id), this); }
		
		else if (passwordStr.length() == 0) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_password), this);}
		
		else if ( !passwordRepeatStr.equals(passwordStr) ) {
			AlertsManager.showAlert(appContext.getResources().getString(R.string.password_mismatch), this);	}
		
		 // TODO: Dori. add a check for minimum length.
		
		else {
			setActivity(this);
			session.createLoginSession(userIDStr, EncryptionEngine.safeHash(passwordStr));
			
			//TODO: Dori/Eli.  the initializeNetworkUtilities call should be done on the loading screen.
			NetworkUtilities.initializeNetworkUtilities(appContext);
			makeNetworkRequest();
			Log.i("RegisterActivity", "creating login session: " + userIDStr);
		}
	}
	
	public void makeNetworkRequest() {
		AsyncPostSender loader = new AsyncPostSender("http://beiwe.org/register_user", getCurrentActivity(), session);
		loader.execute();
	}
	
	public static Activity getCurrentActivity() { return mActivity; }
	
	public static void setActivity(Activity activity) { mActivity = activity; }
}