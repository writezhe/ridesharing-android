package org.beiwe.app.ui.registration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundServiceActivity;
import org.beiwe.app.networking.HTTPUIAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.survey.TextFieldKeyboard;
import org.beiwe.app.ui.utils.AlertsManager;

import static org.beiwe.app.networking.PostRequest.addWebsitePrefix;


/**Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dor Samet, Eli Jones, Josh Zagorsky */

@SuppressLint("ShowToast")
public class RegisterActivity extends RunningBackgroundServiceActivity {
	private EditText serverUrlInput;
	private EditText userIdInput;
	private EditText tempPasswordInput;
	private EditText newPasswordInput;
	private EditText confirmNewPasswordInput;

	/** Users will go into this activity first to register information on the phone and on the server. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		serverUrlInput = (EditText) findViewById(R.id.serverUrlInput);
		userIdInput = (EditText) findViewById(R.id.registerUserIdInput);
		tempPasswordInput = (EditText) findViewById(R.id.registerTempPasswordInput);
		newPasswordInput = (EditText) findViewById(R.id.registerNewPasswordInput);
		confirmNewPasswordInput = (EditText) findViewById(R.id.registerConfirmNewPasswordInput);
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(getApplicationContext());
		textFieldKeyboard.makeKeyboardBehave(serverUrlInput);
		textFieldKeyboard.makeKeyboardBehave(userIdInput);
		textFieldKeyboard.makeKeyboardBehave(tempPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(newPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(confirmNewPasswordInput);

		// TODO: can we ensure that DeviceInfo is still initialized when it's called below?
		DeviceInfo.initialize(getApplicationContext());
	}


	/** Registration sequence begins here, called when the submit button is pressed.
	 * @param view */
	public synchronized void registerButtonPressed(View view) {
		String serverUrl = serverUrlInput.getText().toString();
		String userID = userIdInput.getText().toString();
		String tempPassword = tempPasswordInput.getText().toString();
		String newPassword = newPasswordInput.getText().toString();
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		if (serverUrl.length() == 0) {
			// If the study URL is empty, alert the user
			AlertsManager.showAlert(getString(R.string.url_too_short), getString(R.string.couldnt_register), this);
		} else if (userID.length() == 0) {
			// If the user id length is too short, alert the user
			AlertsManager.showAlert(getString(R.string.invalid_user_id), getString(R.string.couldnt_register), this);
			return;
		} else if (tempPassword.length() < 1) {
			// If the temporary registration password isn't filled in
			AlertsManager.showAlert(getString(R.string.empty_temp_password), getString(R.string.couldnt_register), this);
		} else if (!PersistentData.passwordMeetsRequirements(newPassword)) {
			// If the new password has too few characters
			String alertMessage = String.format(getString(R.string.password_too_short), PersistentData.minPasswordLength());
			AlertsManager.showAlert(alertMessage, getString(R.string.couldnt_register), this);
			return;
		} else if (!newPassword.equals(confirmNewPassword)) {
			// If the new password doesn't match the confirm new password
			AlertsManager.showAlert(getString(R.string.password_mismatch), getString(R.string.couldnt_register), this);
			return;
		} else {
			PersistentData.setServerUrl(serverUrl);
			PersistentData.setLoginCredentials(userID, tempPassword);
			// Log.d("RegisterActivity", "trying \"" + LoginManager.getPatientID() + "\" with password \"" + LoginManager.getPassword() + "\"" );
			tryToRegisterWithTheServer(addWebsitePrefix(getApplicationContext().getString(R.string.register_url)), newPassword);
		}
	}
	
	
	/**Implements the server request logic for user, device registration. 
	 * @param url the URL for device registration*/
	private void tryToRegisterWithTheServer(final String url, final String newPassword) {
		final Activity currentActivity = this;

		new HTTPUIAsync(url, this) {
			@Override
			protected Void doInBackground(Void... arg0) {
				parameters= PostRequest.makeParameter("bluetooth_id", DeviceInfo.getBluetoothMAC() ) +
							PostRequest.makeParameter("new_password", newPassword) +
							PostRequest.makeParameter("phone_number", ((RegisterActivity) activity).getHashedPhoneNumber() ) +
							PostRequest.makeParameter("device_id", DeviceInfo.getAndroidID() ) +
							PostRequest.makeParameter("device_os", "Android") +
							PostRequest.makeParameter("os_version", DeviceInfo.getAndroidVersion() ) +
							PostRequest.makeParameter("hardware_id", DeviceInfo.getHardwareId() ) +
							PostRequest.makeParameter("brand", DeviceInfo.getBrand() ) +
							PostRequest.makeParameter("manufacturer", DeviceInfo.getManufacturer() ) +
							PostRequest.makeParameter("model", DeviceInfo.getModel() ) +
							PostRequest.makeParameter("product", DeviceInfo.getProduct() ) +
							PostRequest.makeParameter("beiwe_version", DeviceInfo.getBeiweVersion() );
				responseCode = PostRequest.httpRegister(parameters, url);
				return null;
			}
		
			@Override
			protected void onPostExecute(Void arg) {
				super.onPostExecute(arg);
				if (responseCode == 200) {
					PersistentData.setPassword(newPassword);
					activity.startActivity(new Intent(activity.getApplicationContext(), PhoneNumberEntryActivity.class) );
					activity.finish();
				} else {
					AlertsManager.showAlert(responseCode, getString(R.string.couldnt_register), currentActivity);
				}
			}
		};
	}
	

	/**This is the fuction that requires SMS permissions.  We need to supply a (unique) identifier for phone numbers to the registration arguments.
	 * @return */
	private String getHashedPhoneNumber() {
		return EncryptionEngine.hashPhoneNumber("");
	}
}
