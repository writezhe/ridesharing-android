package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.session.SessionActivity;
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
 * A class to manage users who forgot their passwords. For future references, this should work very similar to
 * the register class, and will work with the server.
 * 
 * @author Dor Samet
 *
 */

@SuppressLint("ShowToast")
public class ResetPasswordActivity extends SessionActivity {
	// extends SessionActivity
	private Context appContext;
	private EditText oldPass;
	private EditText newPassword;
	private EditText newPasswordRepeat;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		
		// This is the variable assignment section
		appContext = getApplicationContext();
		oldPass = (EditText) findViewById(R.id.reset_password_old_password);
		newPassword = (EditText) findViewById(R.id.reset_password_password);
		newPasswordRepeat = (EditText) findViewById(R.id.reset_password_password_repeat);
		
		// Make keyboard behavior nicely - when clicking outside of the textbox, keyboard disappears
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(oldPass);
		textFieldKeyboard.makeKeyboardBehave(newPassword);
		textFieldKeyboard.makeKeyboardBehave(newPasswordRepeat);
	}
	
	
	
	//TODO: Eli. update this doc
	/** Each time there is an error, such like an incorrect username, the program will throw an alert,
	 *  informing the user of the error.
	 *  
	 *  If the user succeeds in logging in, the activity finishes.
	 * @param view
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException */
	public void resetPasswordSequence(View view) {
		// Old password, and old password hashed
		String oldPassStr = oldPass.getText().toString();
		
		// New password that will be pushed to the server
		String newPasswordStr = newPassword.getText().toString();
		String newPasswordRepeatStr = newPasswordRepeat.getText().toString();

		// Cases: passwords mismatch, and repeat password with actual password mismatch.
		//make sure the old password matches, then...
		if ( LoginManager.checkPassword( oldPassStr ) ) {
			
			//check that both inputs are identical
			if ( !newPasswordRepeatStr.equals(newPasswordStr) ) {
				AlertsManager.showAlert(appContext.getResources().getString(R.string.password_mismatch), this);
				return;
			}
			//check that the proposed password is a valid password
			if ( LoginManager.validatePassword(newPasswordStr, this) ){
				Log.i("debugging", "validated password");
				doResetPassword("http://beiwe.org/set_password", newPasswordStr);
				//note: the reset password function expects a plaintext, unhashed password.
			}
			return;
		}
		AlertsManager.showAlert(appContext.getResources().getString(R.string.invalid_old_password), this);
		
				
	}
	
	
	private void doResetPassword(String url, final String newPassword) { new HTTPAsync(url, this) {
		@Override
		protected Void doInBackground(Void... arg0) {
			parameters = PostRequest.makeParameter( "new_password", newPassword );
			Log.i("debugging", "about to send post request");
			response = PostRequest.httpRequestcode( parameters, url );
			return null; //haaaate
		}
				
		@Override
		protected void onPostExecute(Void arg) {
			if (response == 200) { 
				LoginManager.setPassword(newPassword);
				this.activity.finish();
			}
			//TODO: Josh/Eli.  change to the app string thing.
			AlertsManager.showAlert("could not reset password", activity);
			super.onPostExecute(arg);
		}
	}; }
}	