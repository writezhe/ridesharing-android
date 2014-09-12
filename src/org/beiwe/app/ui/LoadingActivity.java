package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;

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
 * @author Dori Samet
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
		DeviceInfo info = new DeviceInfo(appContext);

		if (isAbleToHash()) {
			session.checkLogin();
			// TODO: Dori. Splash Screen goes here.
			finish();
		} else {
			System.exit(0);
		}
	}

	private boolean isAbleToHash() {
		// Check for existence of libraries:
		try {
			MessageDigest hasher = MessageDigest.getInstance("SHA-256");
			hasher.update("testtesttest".getBytes("UTF-8"));
			return true;
		} catch (NoSuchAlgorithmException noSuchAlgorithm) {
			Log.i("LoadingActivity", "Cannot run the hasher due to unsupported encryption engine - exiting app");
			return false;
		} catch (UnsupportedEncodingException unSupportedEncoding) {
			Log.i("LoadingActivity", "Cannot run the hasher due to unsupported encodin - exiting app");
			return false;
		}
	}
}