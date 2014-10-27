package org.beiwe.app.ui;

import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.R.id;
import org.beiwe.app.R.layout;
import org.beiwe.app.networking.AsyncPostSender;
import org.beiwe.app.networking.NetworkUtility;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
/**
 * 
 * @author Dor Samet
 *
 */
public class ForgotPasswordActivity extends Activity {

	private Context appContext;

	private EditText newPassword;
	private EditText newPasswordRepeat;
	private String hashedNewPassword;
	private String oldPasswordhash; // = LoginSessionManager.getPassword();
	
	//TODO: Josh. (and maybe Eli, but I am working on other logic stuff sooo...
	// This activity should display the user's id, it should probably also only have a single input for the reset key provided by the survey administrators.
	// On a successful password reset it should immediately send the user to the make a new password activity.
	// The make new password activity should probably have a button that says return to app.
	// Though it is unlikely, this gets very complicated if a user gets a notification for a survey/recording, 
	//   fails to log in, does a password reset without ever leaving the app, resets their password, and then expects to take that survey...
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		// Variable assignment
		appContext = getApplicationContext();
		newPassword = (EditText) findViewById(R.id.forgot_password_enter_password);
		newPasswordRepeat = (EditText) findViewById(R.id.forgot_password_enter_password_repeat);
		
		// Make keyboard behavior
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(newPassword);
		textFieldKeyboard.makeKeyboardBehave(newPasswordRepeat);
		
	}
	
	/** This method is used when trying to access the app after a patient loses their password.
	 * Users DO NOT have the ability to "reset" their password if they forget it.
	 * The user must contact the study administrators and ask them to reset the password, and the
	 * administrators provide them with a new password.
	 * The user navigates to the forgot password activity and enters this new password.
	 * The user will then be able to change their password. */
	private void registerNewPassword(View view) {
		//FIXME: this absolutely needs to get a response from the asyncpostsender
		// Variable assignments
		String passwordStr = newPassword.getText().toString();
		String passwordRepeatStr = newPasswordRepeat.getText().toString();
		String encryptedPassword = EncryptionEngine.safeHash(passwordStr);
		// TODO: Eli. make sure that checks here do not conflict with randomly generated passwords from the server.  Change length check 
		if (passwordStr.length() < 0) {
			AlertsManager.showAlert("Invalid password", this);
		} else if (!passwordStr.equals(passwordRepeatStr)) {
			AlertsManager.showAlert("Passwords mismatch", this);
		} else {
			LoginSessionManager.createLoginSession( LoginSessionManager.getPatientID(), encryptedPassword);
			new AsyncPostSender("http://beiwe.org/forgot_password", this).execute();
		}
	}
	
	//  ######  Stub code, working on the new SimpleAsync class.  #### 
	
	/**Creates an SimpleAsync to make a HTTP Post Request  
	 * @param url */
	private void async_thing(final String url) { new HTTPAsync(url, this) {  //This is a retarded spacing hack...

		@Override
		protected Void doInBackground(Void... arg0) {
			
			parameters = PostRequest.makeParameter( "new_password", hashedNewPassword );
			response = PostRequest.asyncPostHandler( parameters, url );
			return null; //hate.
		}
		
		@Override
		protected void onPostExecute(Void arg) {
			if (response == 200){
				LoginSessionManager.createLoginSession( LoginSessionManager.getPatientID(), hashedNewPassword);
				activity.startActivity(new Intent(activity.getApplicationContext(), DebugInterfaceActivity.class));
				activity.finish();
				return;
			}
			
			super.onPostExecute(arg);
		}
	};}
}