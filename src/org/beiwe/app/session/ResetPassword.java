package org.beiwe.app.session;

import org.beiwe.app.R;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.ui.AlertsManager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Code designed to be used in two very similar Activities: ResetPassword and ForgotPassword.
 * Both Activities have three text-input fields (EditTexts): current password, new password, and
 * confirm new password.
 * This code checks the new password and confirm new password; if they're good, it sends the
 * current password to the server.
 * This differs from other authentication calls in the rest of the app in that current password
 * comes from the text-input field, NOT from the app's storage.  This means that an admin can reset
 * the password on the server, and the user can use that new password to reset the device's
 * password, regardless of what password is currently saved on the device.
 * @author Josh Zagorsky, Dor Samet
 */
public class ResetPassword {
	
	private Activity currentActivity;
	private Context appContext;
	private String hashedCurrentPassword;
	private String newPassword;
	

	public ResetPassword(Activity currentActivity) {
		this.currentActivity = currentActivity;
		this.appContext = currentActivity.getApplicationContext();
	}
	
	
	public void checkInputsAndTryToResetPassword(String currentPassword, String newPassword, String confirmNewPassword) {
		this.hashedCurrentPassword = EncryptionEngine.safeHash(currentPassword);
		this.newPassword = newPassword;

		// If the new passwords don't match, pop up an alert, and do nothing else
		if (!newPassword.equals(confirmNewPassword)) {
			AlertsManager.showAlert(appContext.getString(R.string.password_mismatch), currentActivity);
			return;
		}

		// If the new password has too few characters, pop up an alert, and do nothing else
		if (!LoginManager.passwordMeetsRequirements(newPassword, currentActivity)) {
			return;
		}
		
		// If new password and confirm new password are valid, try resetting them on the server
		doResetPasswordRequest();
	}
	
	/** Runs the network operation to reset the password on the server.*/
	public void doResetPasswordRequest() {
		String url = appContext.getString(R.string.reset_password_url);
		new HTTPAsync(url, currentActivity) {
			
			@Override
			protected Void doInBackground(Void... arg0)  {
				parameters = PostRequest.makeParameter("new_password", newPassword);
				response = PostRequest.httpRequestcode(parameters, url, hashedCurrentPassword);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void arg) {
				super.onPostExecute(arg);
				Log.d("ResetPassword.java", "HTTP response code = " + response);
				
				if (response == 200) {
					// Set the password on the device to the new permanent password
					LoginManager.setPassword(newPassword);
					
					// Set the user to "logged in"
					LoginManager.loginOrRefreshLogin();

					// Show a Toast with a "Success!" message
					String message = appContext.getString(R.string.pass_reset_complete);
					Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
					
					// Kill the activity
					currentActivity.finish();
				}
			}
		};
	}
}
