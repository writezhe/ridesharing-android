package org.beiwe.app.ui;

import org.beiwe.app.LoadingActivity;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.survey.AudioRecorderActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
/**
 * The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Hotline, and Sign out
 * @author Dor Samet
 *
 */
public class MainMenuActivity extends Activity {

	private Context appContext;	 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		// Context declaration
		appContext = getApplicationContext();
	}
	
	/**
	 * Calls the hotline
	 * @param v
	 */
	public void callHotline(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = (String) getApplicationContext().getResources().getText(R.string.hotline_phone_number);
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
	    startActivity(callIntent);
	}


	/* ***********************************************************************
	 *****                   Activity transfer intents                ********
	 *********************************************************************** */
	public void signOutButton(View v) {
		Intent signOutIntent = new Intent(appContext, LoadingActivity.class);
		LoginManager.logoutUser();
		startActivity(signOutIntent);
	    finish();
	}
	
	public void graphResults(View v) {
		Intent graphIntent = new Intent(appContext, GraphActivity.class);
		startActivity(graphIntent);
	}
	
	public void recordMessage(View v) {
		Intent audioRecordingIntent = new Intent(appContext, AudioRecorderActivity.class);
		startActivity(audioRecordingIntent);
	}
	
	public void resetPassword(View v) {
		Intent resetPasswordIntent = new Intent(appContext, ResetPasswordActivity.class);
		startActivity(resetPasswordIntent);
	}
}
