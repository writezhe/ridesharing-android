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
		int minPasswordLength = 1; // TODO postproduction: set the minPasswordLength to something higher than 1
		if (newPassword.length() < minPasswordLength) {
			String alertMessage = String.format(appContext.getString(R.string.password_too_short), minPasswordLength);
			AlertsManager.showAlert(alertMessage, currentActivity);
			return;
		}
		
		doResetPasswordRequest();
	}
	
	
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
