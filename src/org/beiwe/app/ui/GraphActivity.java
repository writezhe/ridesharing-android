package org.beiwe.app.ui;

import java.util.HashMap;

import org.apache.http.util.EncodingUtils;
import org.beiwe.app.R;
import org.beiwe.app.R.id;
import org.beiwe.app.R.layout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The activity used to show the graph to the user. This activity currenlty shows the Beiwe website that houses the graph.
 * It also features the options to call the hotline, as well as immediate sign out
 * 
 * @author Dor Samet
 *
 */
public class GraphActivity extends Activity {

	private Context appContext;
	private String beiweGraph = "http://beiwe.org/users";

	/**
	 * Loads the web view by sending an HTTP POST to the website. Currently not in HTTPS
	 * 
	 * Consider removing the Lint warning about the Javascript
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);

		// Context
		appContext = getApplicationContext();

		// Instantiating web view to be embedded in the page
		WebView browser = (WebView) findViewById(R.id.graph_pastResults);
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

		// HTTP Post request set up, using the credentials saved on the phone
		LoginSessionManager sessionManager = new LoginSessionManager(appContext);
		HashMap<String, String> userDetails = sessionManager.getUserDetails();
		beiweGraph += userDetails.get(LoginSessionManager.KEY_ID) + "/graph";
		Log.i("Graph", userDetails.get(LoginSessionManager.KEY_ID));
//		String postData = "patientID=" + userDetails.get(LoginSessionManager.KEY_ID)
//				+ "&pwd=" + userDetails.get(LoginSessionManager.KEY_PASSWORD); TODO: Debug
		String postData = "patient_id=mama&password=aaa";

		browser.postUrl(beiweGraph, EncodingUtils.getBytes(postData, "BASE64"));
	}
}
