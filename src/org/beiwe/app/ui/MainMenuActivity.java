package org.beiwe.app.ui;

import org.beiwe.app.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

	private Context appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		// Context declaration
		appContext = getApplicationContext();
		
		// Webview initiation
		
		WebView browser = (WebView) findViewById(R.id.main_menu_pastResults);
		browser.loadUrl("http://www.google.com");
		browser.getSettings().setBuiltInZoomControls(true);
		browser.setInitialScale(1);
	}
	
	public void callHotline(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
	    callIntent.setData(Uri.parse("tel:123456789"));
	    startActivity(callIntent);
	}
	
	public void signOutButton(View v) {
		Intent signOutIntent = new Intent(appContext, LoadingActivity.class);
	    startActivity(signOutIntent);
	    finish();
	}
	
	public void takeSurvey(View v) {
		Toast.makeText(appContext, "Placeholder", Toast.LENGTH_SHORT).show();
	}
	
	public void recordMessage(View v) {
		Toast.makeText(appContext, "Placeholder", Toast.LENGTH_SHORT).show();
	}
}
