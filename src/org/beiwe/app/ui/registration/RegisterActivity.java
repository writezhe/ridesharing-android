package org.beiwe.app.ui.registration;

import org.beiwe.app.DeviceInfo;
import org.beiwe.app.PermissionHandler;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundServiceActivity;
import org.beiwe.app.networking.HTTPUIAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.survey.TextFieldKeyboard;
import org.beiwe.app.ui.utils.AlertsManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;


/**Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dor Samet, Eli Jones */

@SuppressLint("ShowToast")
public class RegisterActivity extends RunningBackgroundServiceActivity {
	// Private fields
	private EditText userID;
	private EditText password;
	private String newPassword;
	
	private final static int PERMISSION_CALLBACK = 0; //This callback value can be anything, we are not really using it
	private final static int REQUEST_PERMISSIONS_IDENTIFIER = 1500;
	
	/** Users will go into this activity first to register information on the phone and on the server. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		userID = (EditText) findViewById(R.id.registerUserIdInput);
		password = (EditText) findViewById(R.id.registerTempPasswordInput);
		
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard( getApplicationContext() );
		textFieldKeyboard.makeKeyboardBehave(userID);
		textFieldKeyboard.makeKeyboardBehave(password);
	}

	/**Registration sequence begins here, called when the submit button is pressed.
	 * Normally there would be interaction with the server, in order to verify the user ID as well as the phone ID.
	 * Right now it does simple checks to see that the user actually inserted a value.
	 * @param view */
	public synchronized void registrationSequence(View view) {
		String userIDStr = userID.getText().toString();
		String passwordStr = password.getText().toString();

		EditText newPasswordInput = (EditText) findViewById(R.id.registerNewPasswordInput);
		EditText confirmNewPasswordInput = (EditText) findViewById(R.id.registerConfirmNewPasswordInput);
		
		newPassword = newPasswordInput.getText().toString();
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		// If the user id length is too short, alert the user
		if(userIDStr.length() == 0) {
			AlertsManager.showAlert( getString(R.string.invalid_user_id), this);
			return; }

		// If the new password doesn't match the confirm new password
		else if (!newPassword.equals(confirmNewPassword)) {
			AlertsManager.showAlert( getString(R.string.password_mismatch), this);
			return; }
		
		// If the new password has too few characters, pop up an alert, and do nothing else
		//(note: the user alert is handled internally.)
		if (!PersistentData.passwordMeetsRequirements(newPassword, this) ) { return; }

		// If the password length is too short, alert the user
		else if ( PersistentData.passwordMeetsRequirements(passwordStr, this) ) {
			PersistentData.setLoginCredentials(userIDStr, passwordStr);
//			Log.d("RegisterActivity", "trying \"" + LoginManager.getPatientID() + "\" with password \"" + LoginManager.getPassword() + "\"" );
			doRegister(getApplicationContext().getString(R.string.register_url));
		}
	}
	
	
	/**Implements the server request logic for user, device registration. 
	 * @param url the URL for device registration*/
	private void doRegister(final String url) { new HTTPUIAsync(url, this) {
		@Override
		protected Void doInBackground(Void... arg0) {
			parameters= PostRequest.makeParameter("bluetooth_id", DeviceInfo.getBlootoothMAC() ) +
						PostRequest.makeParameter("new_password", newPassword) +
						PostRequest.makeParameter("phone_number", DeviceInfo.getPhoneNumber() ) + 
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
			return null; //hate
		}
		
		@Override
		protected void onPostExecute(Void arg) {
			if (responseCode == 200) {
				PersistentData.setPassword(newPassword);
				activity.startActivity(new Intent(activity.getApplicationContext(), PhoneNumberEntryActivity.class) );
				activity.finish(); }
			else if (responseCode == 2) {
				AlertsManager.showAlert( getString(R.string.invalid_encryption_key), this.activity );
				super.onPostExecute(arg); }
			else { AlertsManager.showAlert( getString(R.string.couldnt_register), this.activity );
				   super.onPostExecute(arg); }
		}
	};}
	
	/*####################################################################
	###################### Permission Prompting ##########################
	####################################################################*/
	
	private static Boolean prePromptActive = false;
	private static Boolean postPromptActive = false;
	private static Boolean thisResumeCausedByFalseActivityReturn = false;
	private static Boolean aboutToResetFalseActivityReturn = false;
	private static Boolean activityNotVisible = false;

	private void goToSettings() {
		// Log.i("reg", "goToSettings");
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_PERMISSIONS_IDENTIFIER);
    }

	
	@Override
	protected void onResume() {
		//TODO: someone leaves and comes back, but alerts were open.
		// Log.i("reg", "onResume");
		super.onResume();
		activityNotVisible = false;
		if (aboutToResetFalseActivityReturn) {
			aboutToResetFalseActivityReturn = false;
			thisResumeCausedByFalseActivityReturn = false;
			return;
		}
		if ( !PermissionHandler.checkReadSms(getApplicationContext()) && !thisResumeCausedByFalseActivityReturn) {
			if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) ) {
				if (!prePromptActive && !postPromptActive ) { showPostPermissionAlert(this); } 
			}
			else if (!prePromptActive && !postPromptActive ) { showPrePermissionAlert(this); }
		}
		else { DeviceInfo.initialize(getApplicationContext()); }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		activityNotVisible = true;
	};
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Log.i("reg", "onActivityResult. requestCode: " + requestCode + ", resultCode: " + resultCode );
		aboutToResetFalseActivityReturn = true;
    }

	@Override
	public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
		// Log.i("reg", "onRequestPermissionResult");
		if (activityNotVisible) return; //this is identical logical progression to the way it works in SessionActivity.
		for (int i = 0; i < grantResults.length; i++) {
			if ( permissions[i].equals( Manifest.permission.READ_SMS ) ) {
//				Log.i("permiss", "permission return: " + permissions[i]);
				if ( grantResults[i] == PermissionHandler.PERMISSION_GRANTED ) { break; }
				if ( shouldShowRequestPermissionRationale(permissions[i]) ) { showPostPermissionAlert(this); } //(shouldShow... "This method returns true if the app has requested this permission previously and the user denied the request.")
			}
//			else { Log.w("permiss", "permission return: " + permissions[i]); }
		}
	}
	
	/* Message Popping */
	
	public static void showPrePermissionAlert(final Activity activity) {
		// Log.i("reg", "showPreAlert");
		if (prePromptActive) { return; }
		prePromptActive = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Permissions Requirement:");
		builder.setMessage(R.string.permission_registration_read_sms_alert);
		builder.setOnDismissListener( new DialogInterface.OnDismissListener() { @Override public void onDismiss(DialogInterface dialog) {
			activity.requestPermissions(new String[]{ Manifest.permission.READ_SMS }, PERMISSION_CALLBACK );
			prePromptActive = false;
		} } );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface arg0, int arg1) { } } ); //Okay button
		builder.create().show();
	}
	
	public static void showPostPermissionAlert(final RegisterActivity activity) {
		// Log.i("reg", "showPostAlert");
		if (postPromptActive) { return; }
		postPromptActive = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Permissions Requirement:");
		builder.setMessage(R.string.permission_registration_actually_need_sms_alert);
		builder.setOnDismissListener( new DialogInterface.OnDismissListener() { @Override public void onDismiss(DialogInterface dialog) {
			thisResumeCausedByFalseActivityReturn = true;
			activity.goToSettings();
			postPromptActive = false;
		} } );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface arg0, int arg1) {  } } ); //Okay button
		builder.create().show();
	}
	
}