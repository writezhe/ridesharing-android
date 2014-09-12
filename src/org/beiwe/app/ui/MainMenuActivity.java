package org.beiwe.app.ui;

import java.util.HashMap;

import org.apache.http.util.EncodingUtils;
import org.beiwe.app.GraphActivity;
import org.beiwe.app.R;
import org.beiwe.app.survey.AudioRecorderActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
/**
 * The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Hotline, and Sign out
 * @author user
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
	    // TODO: Josh? What is the hotline number?
		callIntent.setData(Uri.parse("tel:123456789"));
	    startActivity(callIntent);
	}


	/* ***********************************************************************
	 *****                   Activity transfer intents                ********
	 *********************************************************************** */
	public void signOutButton(View v) {
		Intent signOutIntent = new Intent(appContext, LoadingActivity.class);
	    LoginSessionManager session = new LoginSessionManager(appContext);
	    session.logoutUser();
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
}
