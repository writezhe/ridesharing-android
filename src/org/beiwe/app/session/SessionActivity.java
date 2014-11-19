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
	
//	@Override 
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//	}
	
	@Override
	protected void onResume() {
		super.onResume();
		authenticateAndLoginIfNecessary();
	}
	

	@Override
	protected void onPause() {
		super.onPause();
		BackgroundProcess.clearAutomaticLogoutCountdownTimer();
	}


	/** If the user is NOT logged in, take them to the login page */
	protected void authenticateAndLoginIfNecessary() {
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logged_in_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_change_password:
			Log.i("SessionActivity.java", "Called menu_change_password");
			startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
			return true;
		case R.id.menu_signout:
			Log.i("SessionActivity.java", "Called menu_signout");
			logoutUser();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}