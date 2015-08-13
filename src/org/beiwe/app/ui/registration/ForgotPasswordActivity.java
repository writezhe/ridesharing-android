package org.beiwe.app.ui.registration;

import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundServiceActivity;
import org.beiwe.app.session.ResetPassword;
import org.beiwe.app.storage.PersistentData;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Dor Samet, Eli Jones
 */
public class ForgotPasswordActivity extends RunningBackgroundServiceActivity {
	// extends RunningBackgroundProcessActivity
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		/* Add the user's Patient ID to the heading in the activity, so the user can tell it to the
		 * administrator when the user calls the research assistant asking for a temporary password. */
		TextView title = (TextView) findViewById(R.id.forgotPasswordTitle);
		String titleWithIdResource = getApplicationContext().getString(R.string.forgot_password_title_with_id);
		String instructionsWithId = String.format(titleWithIdResource, PersistentData.getPatientID());
		title.setText(instructionsWithId);
	}
	
	
	/** calls the reset password HTTPAsync query. */
	public void registerNewPassword(View view) {
		// Get the user's temporary password (they get this from a human admin by calling the research assistant)
		EditText tempPasswordInputField = (EditText) findViewById(R.id.forgotPasswordTempPasswordInput);
		String tempPassword = tempPasswordInputField.getText().toString();

		// Get the new, permanent password the user wants
		EditText newPasswordInput = (EditText) findViewById(R.id.forgotPasswordNewPasswordInput);
		String newPassword = newPasswordInput.getText().toString();

		// Get the confirmation of the new, permanent password (should be the same as the previous field)
		EditText confirmNewPasswordInput = (EditText) findViewById(R.id.forgotPasswordConfirmNewPasswordInput);
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		/* Pass all three to the ResetPassword class, which will check validity, and, if valid,
		 * reset the permanent password */
		ResetPassword resetPassword = new ResetPassword(this);
		resetPassword.checkInputsAndTryToResetPassword(tempPassword, newPassword, confirmNewPassword);
	}
	
	public void callResetPassword(View view){
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = PersistentData.getPasswordResetNumber();
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
	    startActivity(callIntent);
	}
}
