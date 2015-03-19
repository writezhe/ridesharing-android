package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class PhoneNumberEntry extends RunningBackgroundProcessActivity {
	private EditText primaryCarePhone;
	private EditText passwordResetPhone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_number_entry);
		primaryCarePhone = (EditText) findViewById(R.id.passwordResetNumber);
		passwordResetPhone = (EditText) findViewById(R.id.primaryCareNumber);

		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard( getApplicationContext() );
		textFieldKeyboard.makeKeyboardBehave(primaryCarePhone);
		textFieldKeyboard.makeKeyboardBehave(passwordResetPhone);
	}
	
	public void checkAndPromptConsent(View view) {
		String primary = primaryCarePhone.getText().toString();
		String reset = passwordResetPhone.getText().toString();
		
		Log.e("primary", "\"" + primary +"\"");
		Log.e("reset", "\"" + reset + "\"");
		if (primary == null || primary.length() == 0 || reset == null || reset.length() == 0 ){
			AlertsManager.showAlert( "You must enter values for the two phone numbers above.", this );
			return;
		}
		
		LoginManager.setPrimaryCareNumber(primary);
		LoginManager.setPasswordResetNumber(reset);
		startActivity(new Intent(getApplicationContext(), ConsentForm.class));
		finish();
	}
}
