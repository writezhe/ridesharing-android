package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.EncryptionEngine;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Dor Samet, Eli Jones
 */
public class ForgotPasswordActivity extends Activity {
	//extends a regular activity.

	private String newPassword;
	private String hashedTempPassword;
	private Activity forgotPasswordActivity;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		//TODO: Josh/Eli. make some letters that say "your user ID is blah, press this button to ... query the server for a new password" or something
	}
	
	
	/** calls the reset password HTTPAsync query. */
	public void registerNewPassword(View view) {
		EditText tempPasswordInputField = (EditText) findViewById(R.id.forgotPasswordTempPasswordInput);
		String tempPassword = tempPasswordInputField.getText().toString();
		hashedTempPassword = EncryptionEngine.safeHash(tempPassword);

		EditText newPasswordInput = (EditText) findViewById(R.id.forgotPasswordNewPasswordInput);
		EditText confirmNewPasswordInput = (EditText) findViewById(R.id.forgotPasswordConfirmNewPasswordInput);
		newPassword = newPasswordInput.getText().toString();
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		// If the new passwords don't match, pop up an alert, and do nothing else
		if (!newPassword.equals(confirmNewPassword)) {
			AlertsManager.showAlert(getApplicationContext().getString(R.string.password_mismatch), this);
			return;
		}
		// If the new password has length 0, pop up an alert, and do nothing else
		int minPasswordLength = 1;
		if (newPassword.length() < minPasswordLength) {
			String alertMessage = String.format(getApplicationContext().getString(R.string.password_too_short), minPasswordLength);
			AlertsManager.showAlert(alertMessage, this);
			return;
		}

		forgotPasswordActivity = this;
		doForgotPasswordRequest(getApplicationContext().getString(R.string.reset_password_url));	
	}
	
	
	/**Creates a SimpleAsync to make an HTTP Post Request  
	 * @param url the URL used in the HTTP Post Request*/
	private void doForgotPasswordRequest(final String url) { new HTTPAsync(url, this) {  //This is a retarded spacing hack...

		@Override
		protected Void doInBackground(Void... arg0) {
			parameters = PostRequest.makeParameter( "new_password", newPassword );
			response = PostRequest.httpRequestcode(parameters, url, hashedTempPassword);
			return null;
		}
		
		@Override
		/** If the response from the server is received, the password is set to that value. Period. */
		protected void onPostExecute(Void arg) {
			super.onPostExecute(arg);
			Log.i("ForgotPasswordActivity.java", "HTTP response code = " + response);
			
			//if ( responseString == null || response != -1 ) { return; } //this is the case where the network request has failed.
			if (response == 200) {
				// Set the password on the device to the new permanent password
				LoginManager.setPassword(newPassword);

				// Show a Toast with a "Success!" message
				String message = getApplicationContext().getString(R.string.pass_reset_complete);				
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

				// Kill the activity
				forgotPasswordActivity.finish();
			}
		}
	};}
}
