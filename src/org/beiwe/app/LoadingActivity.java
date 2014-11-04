package org.beiwe.app;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.R;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.AlertsManager;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.MainMenuActivity;
import org.beiwe.app.ui.RegisterActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This is a gateway activity - the point of this activity is to navigate in between the three
 * starting activities.
 * 
 * Right now all it does is to call on checkLogin, which is the actual transfer mechanism.
 * 
 * This activity is also designed for splash screens.
 * @author Eli Jones, Dor Samet
 *
 */

public class LoadingActivity extends Activity{
	//extends a regular activity
	
	public static Class loadThisActivity = DebugInterfaceActivity.class;
//	public static Class loadedActivity = MainMenuActivity.class;
	
	
	/**onCreate - right now it just calls on checkLogin() in SessionManager, and moves the activity
	 * to the appropriate page. In the future it could hold a splash screen before redirecting activity. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		
		if ( isAbleToHash() ) {
			if ( BackgroundProcess.getBackgroundHandle() == null ){ 
				//check that the background service is running, if not...
				Log.d("LoadingActivity", "BackgroundHandle null, initializing app components." );
				//Order: DevicInfo, LoginManager, TextFileManager, PostRequest.
				DeviceInfo.initialize( getApplicationContext() );
				LoginManager.initialize( getApplicationContext() );
				TextFileManager.start( getApplicationContext() );
				PostRequest.initialize( getApplicationContext() );
			}
		}
		else { failureExit(); }
		
		//if the device is not registered, push the user to the register activity
		if ( !LoginManager.isRegistered() ){
			Log.i("something", "anything");
			startActivity(new Intent(this, RegisterActivity.class) ); }
		//if device is registered push user to the main menu.
//		else { startActivity(new Intent(this, MainMenuActivity.class) ); }
		else {
			Log.i("something else", "anything");
			startActivity(new Intent(this, loadThisActivity) ); } 
		finish(); //weird, but otherwise it may be possible for the user to actually see the loading screen.
	}

	
	
	/**Tests whether the device can run the hash algorithm we need. 
	 * @return */
	private boolean isAbleToHash() {
		// Runs the unsafe hashing function and catches errors, if it catches errors.
		try {
			EncryptionEngine.unsafeHash("input");
			return true; }
		catch (NoSuchAlgorithmException noSuchAlgorithm) { failureExit(); }
		catch (UnsupportedEncodingException unSupportedEncoding) { failureExit(); }
		return false;
	}

	
	private void failureExit() {
		//TODO: Eli.  Make this an android string.
		AlertsManager.showErrorAlert("This device does not meet minimum specifications for this app, sorry.", this);
		System.exit(1);
	}
}