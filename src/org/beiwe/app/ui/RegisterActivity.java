package org.beiwe.app.ui;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
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


/**
 * Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dori Samet
 *
 */
@SuppressLint("ShowToast")
public class RegisterActivity extends Activity {
	
	// Private fields
	private Context appContext;
	private EditText userID;
	private EditText password;
	private EditText passwordRepeat;
	private LoginSessionManager session;

	/**
	 * Users will go into this activity first to register information on the phone and on the server.
	 */
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
	
	/**
	 * Registration sequence begins here when the submit button is pressed.
	 * Normally there would be interaction with the server, in order to verify the user ID as well as the phone ID.
	 * Right now it does simple checks to see that the user actually inserted a value
	 * @param view
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressLint("ShowToast")
	public void registrationSequence(View view) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String userIDStr = userID.getText().toString();
		String passwordStr = password.getText().toString();
		String passwordRepeatStr = passwordRepeat.getText().toString();

		// Logic gauntlet begins here
		// TODO: Dori/Eli There needs to be more logic here to prevent false registration
		if(userIDStr.length() == 0) {
			AlertsManager.showAlert("Invalid user ID", this);
		} else if (passwordStr.length() == 0) {
			AlertsManager.showAlert("Invalid password", this);
		} else if (passwordRepeatStr.length() == 0 || !passwordRepeatStr.equals(passwordStr)) {
			AlertsManager.showAlert("Passwords mismatch", this);
		} else {
			Log.i("RegisterActivity", "Attempting to create a login session");
			session.createLoginSession(userIDStr, EncryptionEngine.hash(passwordStr));
			try {
				int response = pushDataToServer(userIDStr, passwordStr);
			} catch (IOException e) {
				Log.i("RegisterActivity", "Connection Failed..");
			}
			Log.i("RegisterActivity", "Registration complete, attempting to start DebugInterfaceActivity");
			startActivity(new Intent(appContext, DebugInterfaceActivity.class));
			finish();
		}
	}
	
	private int pushDataToServer(String userID, String password) throws IOException, NoSuchAlgorithmException {		
		// Getting IDs
		String droidID = DeviceInfo.getAndroidID();
		String bluetoothMAC = DeviceInfo.getBlootoothMAC();
		
		// TODO: Eli/Dori/Josh. What is the URL we are sending the data to?
		URL url = new URL("http://beiwe.org/userinfo");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String param = "patientID=" + userID + "&pwd=" + password + "&droidID=" + droidID + "&btID=" + bluetoothMAC;
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", "" + Integer.toString(param.getBytes().length));
		conn.setUseCaches (false);
		
		// Start sending data
		conn.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
		outputStream.writeBytes(param);
		outputStream.flush();
		outputStream.close();
		
		// Read response
		Log.i("POSTREGISTRATION", "RESPONSE = " + conn.getResponseMessage());
		return conn.getResponseCode();
		
	}
}