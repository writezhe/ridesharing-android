package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.networking.NetworkUtility;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * This is a gateway activity - the point of this activity is to navigate in between the three
 * starting activities.
 * 
 * Right now all it does is to call on checkLogin, which is the actual transfer mechanism.
 * 
 * This activity is also designed for splash screens.
 * @author Dor Samet
 *
 */

public class LoadingActivity extends Activity{

	// Private objects
	private LoginSessionManager session;
	private Context appContext;

	/**
	 * onCreate - right now it just calls on checkLogin() in SessionManager, and moves the activity
	 * to the appropriate page. In the future it could hold a splash screen before redirecting activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		appContext = getApplicationContext();
		session = new LoginSessionManager(appContext);

		// Instantiating DeviceInfo
		//TODO: Eli. Change this to a static initializer function.
		DeviceInfo info = new DeviceInfo(appContext);

		if ( isAbleToHash() ) {
			try { BackgroundProcess.getBackgroundHandle(); } 
			catch (NullPointerException e) {
				Log.i("LoadingActivity", e.getMessage() );
				TextFileManager.start(appContext);
				PostRequest.initializePostRequest(appContext, session);
			}
			startActivity( session.login() );
			// TODO: Josh, start activities from here instead of from LoginSessionManager.java
			/* switch ( session.checkLogin() ) {
			 * case (LoginSessionManager.caseCode1) : startActivity(new Intent(RegisterActivity));
			 * case (LoginSessionManager.caseCode2) : startActivity(new Intent(LoginActivity));
			 * case (LoginSessionManager.caseCode3) : startActivity(new Intent(MainMenuActivity));
			 */
			finish();
		} else { failureExit(); }
	}

	private boolean isAbleToHash() {
		// Runs the unsafe hashing function and catches errors, if it catches errors.
		try {
			EncryptionEngine.unsafeHash("input");
			return true; }
		catch (NoSuchAlgorithmException noSuchAlgorithm) {
			failureExit(); }
		catch (UnsupportedEncodingException unSupportedEncoding) {
			failureExit(); }
		return false;
	}

	private void failureExit() {
		//TODO: Eli.  Make this an android string.
		AlertsManager.showErrorAlert("This device does not meet minimum specifications for this app, sorry.", this);
		System.exit(1);
	}
}