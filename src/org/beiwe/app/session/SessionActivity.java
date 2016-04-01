package org.beiwe.app.session;

import java.util.Iterator;
import java.util.Map;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.PermissionHandler;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundServiceActivity;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.ui.registration.RegisterActivity;
import org.beiwe.app.ui.registration.ResetPasswordActivity;
import org.beiwe.app.ui.user.AboutActivityLoggedIn;
import org.beiwe.app.ui.user.GraphActivity;
import org.beiwe.app.ui.user.LoginActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
		if (backgroundService == null) {
			Log.w("sessionactivity", "background service is null, you have a race condition with instantiating the background service.");
			TextFileManager.getDebugLogFile().writeEncrypted("a sessionactivity tried to clear the automatic logout countdown timer, but the background service did not exist.");
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
	private static Boolean thisResumeCausedByFalseActivityReturn = false;
	private static Boolean aboutToResetFalseActivityReturn = false;
	private static Boolean activityNotVisible = false;
	public Boolean isAudioRecorderActivity() { return false; }
	
	private void goToSettings(Integer permissionIdentifier) {
		Log.i("sessionActivity", "goToSettings");
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, permissionIdentifier);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("sessionActivity", "onActivityResult. requestCode: " + requestCode + ", resultCode: " + resultCode );
		aboutToResetFalseActivityReturn = true;
    }
	
	@Override
	public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
		Log.i("sessionActivity", "onRequestPermissionResult");
		if (!activityNotVisible) checkPermissionsLogic();
	}
	
	protected void checkPermissionsLogic() {
		//gets called as part of onResume
		//TODO: someone leaves and comes back, but alerts were open.
		activityNotVisible = false;
		Log.i("sessionactivity", "checkPermissionsLogic");

		Log.i("sessionActivity", "prePromptActive: " + prePromptActive);
		Log.i("sessionActivity", "postPromptActive: " + postPromptActive);
		Log.i("sessionActivity", "thisResumeCausedByFalseActivityReturn: " + thisResumeCausedByFalseActivityReturn);
		Log.i("sessionActivity", "aboutToResetFalseActivityReturn: " + aboutToResetFalseActivityReturn);
		
		if (aboutToResetFalseActivityReturn) {
			aboutToResetFalseActivityReturn = false;
			thisResumeCausedByFalseActivityReturn = false;
			return;
		}
		
		if ( !thisResumeCausedByFalseActivityReturn ) {
			String permission = PermissionHandler.getNextPermission( getApplicationContext(), this.isAudioRecorderActivity() );
			if (permission == null) { return; }
			Log.d("sessionActivity", "shouldShowRequestPermissionRationale "+ permission +": " + shouldShowRequestPermissionRationale( permission ) );
			if (shouldShowRequestPermissionRationale( permission ) ) {
				if (!prePromptActive && !postPromptActive ) { showBumpingPermissionAlert(this, PermissionHandler.getBumpingPermissionMessage(permission),
																							permission, PermissionHandler.permissionMap.get(permission) ); } 
			}
			else if (!prePromptActive && !postPromptActive ) { showRegularPermissionAlert(this, PermissionHandler.getNormalPermissionMessage(permission),
																							permission, PermissionHandler.permissionMap.get(permission)); }
		}
	}
	
	/* Message Popping */
	
	public static void showRegularPermissionAlert(final Activity activity, final String message, final String permission, final Integer permissionCallback) {
		Log.i("sessionActivity", "showPreAlert");
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
	
	public static void showBumpingPermissionAlert(final SessionActivity activity, final String message, final String permission, final Integer permissionCallback) {
		Log.i("sessionActivity", "showPostAlert");
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
}