package org.beiwe.app.session;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.PersistentData;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.AboutActivityLoggedIn;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.registration.ResetPasswordActivity;

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
		PersistentData.initialize(getApplicationContext()); // this function has been rewritten to efficiently handle getting called too much.  Don't worry about it.
	}
	
	/** When onPause is called we need to set the timeout. */
	@Override
	protected void onPause() {
		super.onPause();
		if (backgroundProcess == null) {
			Log.w("sessionactivity", "background process is null, you have a race condition with instantiating the background process.");
			TextFileManager.getDebugLogFile().writeEncrypted("a sessionactivity tried to clear the automatic logout countdown timer, but the background process did not exist.");
		}
		BackgroundService.clearAutomaticLogoutCountdownTimer();
	}
	
	@Override
	/** Sets the logout timer, should trigger whenever onResume is called. */
	protected void doBackgroundDependantTasks() { 
		Log.i("SessionActivity", "printed from SessionActivity");
		authenticateAndLoginIfNecessary();
	}
	
	/** If the user is NOT logged in, take them to the login page */
	protected void authenticateAndLoginIfNecessary() {
		if ( PersistentData.isLoggedIn() ) {
			BackgroundService.startAutomaticLogoutCountdownTimer(); }
		else {
			startActivity(new Intent(this, LoginActivity.class) ); }
	}


	/** Display the LoginActivity, and invalidate the login in SharedPreferences */
	protected void logoutUser() {
		PersistentData.logout();
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
		case R.id.menu_about:
			startActivity(new Intent(getApplicationContext(), AboutActivityLoggedIn.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}