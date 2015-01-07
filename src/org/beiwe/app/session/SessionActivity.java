package org.beiwe.app.session;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.ResetPasswordActivity;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * All Activities in the app WHICH REQUIRE THE USER TO BE LOGGED IN extend this Activity.
 * If the user is not logged in, he/she is bumped to a login screen.
 * This Activity also extends RunningBackgroundProcessActivity, which makes the app's key
 * services run before the interface is allowed to interact with it.
 * 
 * @author Eli Jones, Josh Zagorsky
 */
public class SessionActivity extends RunningBackgroundProcessActivity {
	
	/*####################################################################
	########################## Log-in Logic ##############################
	####################################################################*/
	
	/** when onResume is called we need to authenticate the user and
	 * bump them to the login screen if they have timed out. */
	@Override
	protected void onResume() {
		super.onResume();
		LoginManager.initialize(getApplicationContext()); // yeah this function has been rewritten to handle getting called too much.
		authenticateAndLoginIfNecessary();
	}
	
	/** When onPause is called we need to set the timeout. */
	@Override
	protected void onPause() {
		super.onPause();
		if (backgroundProcess == null) Log.w("sessionactivity 1", "background process is null, you have a race condition with instantiating the background process.");
		BackgroundProcess.clearAutomaticLogoutCountdownTimer();
	}

	/*FIXME: We have some sort of race condition here that can be replicated by doing the following.
	 * open the app, login, scroll down to the audio recording button and tap it.
	 * go to the task switcher and swipe away the app, then immediately go up to the audio recording notification and tap it.
	 * the app will try to open, and then crash.
	 * 
	 * sessionactivity 2
	 */

	/** If the user is NOT logged in, take them to the login page */
	protected void authenticateAndLoginIfNecessary() {
		if (backgroundProcess == null) Log.w("sessionactivity 2", "background process is null, you have a race condition with instantiating the background process.");
		
		if (LoginManager.isLoggedIn()) {
			BackgroundProcess.startAutomaticLogoutCountdownTimer();
		}
		else {
			startActivity(new Intent(this, LoginActivity.class));
		}
	}


	/** Display the LoginActivity, and invalidate the login in SharedPreferences */
	protected void logoutUser() {
		LoginManager.logout();
		startActivity(new Intent(this, LoginActivity.class));
	}
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/
	
	/** Sets up the contents of the menu button. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logged_in_menu, menu);
		return true;
	}

	/** Sets up the behavior of the items in the menu. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_change_password:
			startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
			return true;
		case R.id.menu_signout:
			logoutUser();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}