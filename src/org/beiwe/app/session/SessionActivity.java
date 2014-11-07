package org.beiwe.app.session;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.R;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.ResetPasswordActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SessionActivity extends Activity {
	
	protected boolean isLoginScreen = false;
	
	/*####################################################################
	########################## Log in Logic ##############################
	####################################################################*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService( new Intent( this.getApplicationContext(), BackgroundProcess.class ) );
	}
	
	/**
	 * onResume() is always called when the activity opens.
	 * If the Activity gets created, onCreate gets called, and then onResume.
	 * If the Activity was already created but was paused, onResume gets called.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		authenticateAndLoginIfNecessary();
		tryToStartAutomaticLogoutCountdownTimer();
	}
	

	protected void tryToStartAutomaticLogoutCountdownTimer(){
		Log.d("sessionmanager", "trying timer");
		if ( BackgroundProcess.getBackgroundHandle() != null ){
			Log.d("sessionmanager", "actually doing timer");
			BackgroundProcess.resetAutomaticLogoutCountdownTimer(); }
		else {
			Log.e("sessionmanager", "BackgroundProcess is not working!");
			System.exit(1);
		}
	}
	

	protected void authenticateAndLoginIfNecessary() {
		if ( !LoginManager.isLoggedIn() ) {
			startActivity( new Intent(this, LoginActivity.class) );
		}
	}
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.common_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_about:
			// TODO Josh: create an about-the-app page/activity.
			return true;
		case R.id.menu_call_hotline:
			callHotline();
			return true;
		case R.id.menu_change_password:
			startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
			return true;
		case R.id.menu_signout:
			LoginManager.setLoggedIn(false);
			startActivity( new Intent(this, LoginActivity.class) );
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	protected void callHotline() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = (String) getApplicationContext().getResources().getText(R.string.hotline_phone_number);
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
	    startActivity(callIntent);		
	}
}