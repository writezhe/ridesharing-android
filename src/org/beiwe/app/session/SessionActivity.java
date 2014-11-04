package org.beiwe.app.session;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.ui.LoginActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SessionActivity extends Activity {
	protected boolean isLoginScreen = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ( BackgroundProcess.getBackgroundHandle() == null ) {
			startService( new Intent( this.getApplicationContext(), BackgroundProcess.class ) );
		}
		bounceToLogin();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bounceToLogin();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		loginStartTimer();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		loginStartTimer();
	}
	

	protected void loginStartTimer(){
		Log.d("sessionmanager", "trying timer");
		if ( BackgroundProcess.getBackgroundHandle() != null ){
			Log.d("sessionmanager", "actually doing timer");
			BackgroundProcess.getBackgroundHandle().restartTimeout();
			LoginManager.setLoggedIn(true);
		}
	}
	

	protected void bounceToLogin() {
		if ( !LoginManager.isLoggedIn() ) {
			startActivity( new Intent(this, LoginActivity.class) );
		}
	}
}