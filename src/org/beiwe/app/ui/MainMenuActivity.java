package org.beiwe.app.ui;

import java.util.HashMap;

import org.apache.http.util.EncodingUtils;
import org.beiwe.app.R;

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

public class MainMenuActivity extends Activity {

	private Context appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		// Context declaration
		appContext = getApplicationContext();
		
		// Instantiating web view to be embedded in the page
		WebView browser = (WebView) findViewById(R.id.main_menu_pastResults);
		WebSettings browserSettings = browser.getSettings();
		browser.setWebViewClient(new WebViewClient() {
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		            view.loadUrl(url);
		            return true;
		            }
		    });
		
		// Enable Javascript to display the graph, as well as initial scale
		browserSettings.setJavaScriptEnabled(true);
		browser.setInitialScale(200);
		
		// Http starts here
		LoginSessionManager sessionManager = new LoginSessionManager(appContext);
		HashMap userDetails = sessionManager.getUserDetails();
		String postData = "patientID=" + userDetails.get(LoginSessionManager.KEY_ID)
				+ "&pwd=" + userDetails.get(LoginSessionManager.KEY_PASSWORD);
		
		browser.postUrl("http://beiwe.org/graph", EncodingUtils.getBytes(postData, "BASE64"));
	}
	
	public void callHotline(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
	    // TODO: What is the hotline number?
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
