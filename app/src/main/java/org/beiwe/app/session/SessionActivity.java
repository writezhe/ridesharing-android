package org.beiwe.app.session;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.PermissionHandler;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundServiceActivity;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.registration.ResetPasswordActivity;
import org.beiwe.app.ui.user.AboutActivityLoggedIn;
import org.beiwe.app.ui.user.GraphActivity;
import org.beiwe.app.ui.user.LoginActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**All Activities in the app WHICH REQUIRE THE USER TO BE LOGGED IN extend this Activity.
 * If the user is not logged in, he/she is bumped to a login screen.
 * This Activity also extends RunningBackgroundServiceActivity, which makes the app's key
 * services run before the interface is allowed to interact with it.
 * @author Eli Jones, Josh Zagorsky */
public class SessionActivity extends RunningBackgroundServiceActivity {
	
	/*####################################################################
	########################## Log-in Logic ##############################
	####################################################################*/
	
	/** when onResume is called we need to authenticate the user and
	 * bump them to the login screen if they have timed out. */
	@Override
	protected void onResume() {
		super.onResume();
		PersistentData.initialize(getApplicationContext()); // this function has been rewritten to efficiently handle getting called too much.  Don't worry about it.
		checkPermissionsLogic();
	}
	
	/** When onPause is called we need to set the timeout. */
	@Override
	protected void onPause() {
		super.onPause();
		activityNotVisible = true;
		if (backgroundService != null) {
			//If an activity is active there is a countdown to bump a user to a login screen after
			// some amount of time (setting is pushed by study).  If we leave the session activity
			// we need to cancel that action.
			//This issue has occurred literally once ever (as of February 27 2016) but the prior
			// behavior was broken and caused the app to crash.  Really, this state is incomprehensible
			// (activity is open an mature enough that onPause can occur, yet the background service
			// has not started?) so a crash does at least reboot Beiwe into a functional state,
			// but that obviously has its own problems.  Updated code should merely be bad UX as
			// a user could possibly get bumped to the login screen from another app.
			BackgroundService.clearAutomaticLogoutCountdownTimer(); }
		else { Log.w("SessionActivity bug","the background service was not running, could not cancel UI bump to login screen."); }
	}
	
	@Override
	/** Sets the logout timer, should trigger whenever onResume is called. */
	protected void doBackgroundDependentTasks() {
		// Log.i("SessionActivity", "printed from SessionActivity");
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
		menu.findItem(R.id.menu_call_clinician).setTitle(PersistentData.getCallClinicianButtonText());
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
		case R.id.view_survey_answers:
			startActivity(new Intent(getApplicationContext(), GraphActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	/*####################################################################
	###################### Permission Prompting ##########################
	####################################################################*/
	
	private static Boolean prePromptActive = false;
	private static Boolean postPromptActive = false;
	private static Boolean powerPromptActive = false; 
	private static Boolean thisResumeCausedByFalseActivityReturn = false;
	private static Boolean aboutToResetFalseActivityReturn = false;
	private static Boolean activityNotVisible = false;
	public Boolean isAudioRecorderActivity() { return false; }
	
	private void goToSettings(Integer permissionIdentifier) {
		// Log.i("sessionActivity", "goToSettings");
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, permissionIdentifier);
    }
	
	@TargetApi(23)
	private void goToPowerSettings(Integer powerCallbackIdentifier) {
		// Log.i("sessionActivity", "goToSettings");
        @SuppressLint("BatteryLife") Intent powerSettings = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
        powerSettings.addCategory(Intent.CATEGORY_DEFAULT);
        powerSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(powerSettings, powerCallbackIdentifier);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Log.i("sessionActivity", "onActivityResult. requestCode: " + requestCode + ", resultCode: " + resultCode );
		aboutToResetFalseActivityReturn = true;
    }
	
	@Override
	public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
		// Log.i("sessionActivity", "onRequestPermissionResult");
		if (!activityNotVisible) checkPermissionsLogic();
	}
	
	protected void checkPermissionsLogic() {
		//gets called as part of onResume,
		activityNotVisible = false;
		// Log.i("sessionactivity", "checkPermissionsLogic");
		// Log.i("sessionActivity", "prePromptActive: " + prePromptActive);
		// Log.i("sessionActivity", "postPromptActive: " + postPromptActive);
		// Log.i("sessionActivity", "thisResumeCausedByFalseActivityReturn: " + thisResumeCausedByFalseActivityReturn);
		// Log.i("sessionActivity", "aboutToResetFalseActivityReturn: " + aboutToResetFalseActivityReturn);
		
		if (aboutToResetFalseActivityReturn) {
			aboutToResetFalseActivityReturn = false;
			thisResumeCausedByFalseActivityReturn = false;
			return;
		}
		
		if ( !thisResumeCausedByFalseActivityReturn ) {
			String permission = PermissionHandler.getNextPermission( getApplicationContext(), this.isAudioRecorderActivity() );
			if (permission == null) { return; }
			
			if (!prePromptActive && !postPromptActive && !powerPromptActive) {
				if (permission == PermissionHandler.POWER_EXCEPTION_PERMISSION ) {
					showPowerManagementAlert(this, getString(R.string.power_management_exception_alert), 1000); 
					return;
				}
				// Log.d("sessionActivity", "shouldShowRequestPermissionRationale "+ permission +": " + shouldShowRequestPermissionRationale( permission ) );
				if (shouldShowRequestPermissionRationale( permission ) ) {
					if (!prePromptActive && !postPromptActive ) { showBumpingPermissionAlert(this, PermissionHandler.getBumpingPermissionMessage(permission),
																								   PermissionHandler.permissionMap.get(permission) ); } 
				}
				else if (!prePromptActive && !postPromptActive ) { showRegularPermissionAlert(this, PermissionHandler.getNormalPermissionMessage(permission),
																						  permission, PermissionHandler.permissionMap.get(permission)); }
			}
		}
	}
	
	/* Message Popping */
	
	public static void showRegularPermissionAlert(final Activity activity, final String message, final String permission, final Integer permissionCallback) {
		// Log.i("sessionActivity", "showPreAlert");
		if (prePromptActive) { return; }
		prePromptActive = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Permissions Requirement:");
		builder.setMessage(message);
		builder.setOnDismissListener( new DialogInterface.OnDismissListener() { @Override public void onDismiss(DialogInterface dialog) {
			activity.requestPermissions(new String[]{ permission }, permissionCallback );
			prePromptActive = false;
		} } );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface arg0, int arg1) { } } ); //Okay button
		builder.create().show();
	}
	
	public static void showBumpingPermissionAlert(final SessionActivity activity, final String message, final Integer permissionCallback) {
		// Log.i("sessionActivity", "showPostAlert");
		if (postPromptActive) { return; }
		postPromptActive = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Permissions Requirement:");
		builder.setMessage(message);
		builder.setOnDismissListener( new DialogInterface.OnDismissListener() { @Override public void onDismiss(DialogInterface dialog) {
			thisResumeCausedByFalseActivityReturn = true;
			activity.goToSettings(permissionCallback);
			postPromptActive = false;
		} } );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface arg0, int arg1) {  } } ); //Okay button
		builder.create().show();
	}
	
	public static void showPowerManagementAlert(final SessionActivity activity, final String message, final Integer powerCallbackIdentifier) {
		Log.i("sessionActivity", "power alert");
		if (powerPromptActive) { return; }
		powerPromptActive = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Permissions Requirement:");
		builder.setMessage(message);
		builder.setOnDismissListener( new DialogInterface.OnDismissListener() { @Override public void onDismiss(DialogInterface dialog) {
			Log.d("power management alert", "bumping");
			thisResumeCausedByFalseActivityReturn = true;
			activity.goToPowerSettings(powerCallbackIdentifier);
			powerPromptActive = false;
		} } );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface arg0, int arg1) {  } } ); //Okay button
		builder.create().show();
	}
}
