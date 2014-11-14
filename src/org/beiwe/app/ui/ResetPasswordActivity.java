package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.session.ResetPassword;
import org.beiwe.app.session.SessionActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
	}
	
	
	/** calls the reset password HTTPAsync query. */
	public void registerNewPassword(View view) {
		// Get the user's current password
		EditText currentPasswordInputField = (EditText) findViewById(R.id.resetPasswordCurrentPasswordInput);
		String currentPassword = currentPasswordInputField.getText().toString();

		// Get the new, permanent password the user wants
		EditText newPasswordInput = (EditText) findViewById(R.id.resetPasswordNewPasswordInput);
		String newPassword = newPasswordInput.getText().toString();
		
		// Get the confirmation of the new, permanent password (should be the same as the previous field)
		EditText confirmNewPasswordInput = (EditText) findViewById(R.id.resetPasswordConfirmNewPasswordInput);
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		/* Pass all three to the ResetPassword class, which will check validity, and, if valid,
		 * reset the permanent password */
		ResetPassword resetPassword = new ResetPassword(this);
		resetPassword.checkInputsAndTryToResetPassword(currentPassword, newPassword, confirmNewPassword);
	}

}
