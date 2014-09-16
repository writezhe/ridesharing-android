package org.beiwe.app.ui;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.PostRequestFileUpload;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


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
	public void registrationSequence(View view) {
		String userIDStr = userID.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		// TODO: Dori/Eli There needs to be more logic here to prevent false registration
		// server side: this should call a function that checks if the user id is a valid id for the study, then create the user, then return 200 ok.
		if(userIDStr.length() == 0) {
			AlertsManager.showAlert("Invalid user ID", this);
		} else if (passwordStr.length() == 0) {
			AlertsManager.showAlert("Invalid password", this);
		} else if (passwordRepeatStr.length() == 0 || !passwordRepeatStr.equals(passwordStr)) {
			AlertsManager.showAlert("Passwords mismatch", this);
		} else {
			Log.i("RegisterActivity", "Attempting to create a login session");
			session.createLoginSession(userIDStr, EncryptionEngine.hash(passwordStr));
			pushDataToServer(userIDStr);
			Log.i("RegisterActivity", "Registration complete, attempting to start DebugInterfaceActivity");
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		}

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

	private void pushDataToServer(String userID) {		
		StringBuilder stringBuilder = new StringBuilder();
		try {
			String droidID = DeviceInfo.getAndroidID();
			String bluetoothMAC = DeviceInfo.getBlootoothMAC();
			stringBuilder.append("&droidID=" + droidID + "&btID=" + bluetoothMAC);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		String url = "http://beiwe.org/userinfo";
		String param = "patientID=" + userID + stringBuilder.toString();

		new AsyncPostSender().execute(param, url);
	}
}