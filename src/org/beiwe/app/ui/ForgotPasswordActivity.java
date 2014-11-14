package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.session.ResetPassword;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * @author Dor Samet, Eli Jones
 */
public class ForgotPasswordActivity extends Activity {
	//extends a regular activity.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		//TODO: Josh/Eli. make some letters that say "your user ID is blah, press this button to ... query the server for a new password" or something
	}
	
	
	/** calls the reset password HTTPAsync query. */
	public void registerNewPassword(View view) {
		// Get the user's temporary password (they get this from a human admin by calling the hotline)
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

}
