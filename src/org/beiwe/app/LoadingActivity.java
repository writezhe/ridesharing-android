package org.beiwe.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

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

	private SessionManager session;
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
		session = new SessionManager(appContext);
		
		session.checkLogin();
		// Splash Screen goes here.
		finish();
	}
}
