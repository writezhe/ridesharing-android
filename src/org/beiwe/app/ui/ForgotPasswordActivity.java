package org.beiwe.app.ui;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.networking.AsyncPostSender;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.survey.TextFieldKeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
/**
 * @author Dor Samet, Eli Jones
 */
public class ForgotPasswordActivity extends Activity {

	private Context appContext;

	private EditText newPassword;
	private EditText newPasswordRepeat;
	private String hashedNewPassword;
//	private String oldPasswordhash; // = LoginSessionManager.getPassword();
	
	//TODO: Josh. (and maybe Eli, but I am working on other logic stuff sooo...
	// This activity should display the user's id, it should probably also only have a single input for the reset key provided by the survey administrators.
	// On a successful password reset it should immediately send the user to the make a new password activity.
	// The make new password activity should probably have a button that says return to app.
	// Though it is unlikely, this gets very complicated if a user gets a notification for a survey/recording, 
	//   fails to log in, does a password reset without ever leaving the app, resets their password, and then expects to take that survey...
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		//TODO: Josh/Eli. make some letters that say "your user ID is blah, press this button to ... query the server for a new password" or something
		
		// Variable assignment
		appContext = getApplicationContext();
		newPassword = (EditText) findViewById(R.id.forgot_password_enter_password);
		newPasswordRepeat = (EditText) findViewById(R.id.forgot_password_enter_password_repeat);
	}
	
	/** calls the reset password HTTPAsync query. */
	private void registerNewPassword(View view) {
		async_thing("http://beiwe.org/forgot_password");
	}
	
	//  ######  Stub code, working on the new SimpleAsync class.  #### 
	
	/**Creates an SimpleAsync to make a HTTP Post Request  
	 * @param url */
	private void async_thing(final String url) { new HTTPAsync(url, this) {  //This is a retarded spacing hack...

		@Override
		protected Void doInBackground(Void... arg0) {
			
			parameters = PostRequest.makeParameter( "new_password", hashedNewPassword );
			responseString = PostRequest.asyncRequestString( parameters, url );
			//TODO: make this function return the current password for the user.
			return null; //hate.
		}
		
		@Override
		/**
		 * if the response from the server is received, the password is set to that value. Period.
		 */
		protected void onPostExecute(Void arg) {
			super.onPostExecute(arg);
			Log.i("ForgotPasswordActivity", "old password hash: " + LoginManager.getPassword() + " new password(unhashed): " + responseString);
			
			
			if ( responseString == null ) { return; } //this is the case where the network request has failed.
			
			LoginManager.setPassword(responseString);
			
			//TODO: Eli/Josh.  Throw an alert here saying "password has been successfully reset, they should try to log in with the new password, then send them to the login activity.
			activity.startActivity( new Intent(activity.getApplicationContext(), LoginActivity.class) );
			activity.finish();
		}
	};}
}