package org.beiwe.app.ui;

import org.apache.http.util.EncodingUtils;
import org.beiwe.app.R;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.session.SessionActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The activity that shows the graph to the user. Displays the Beiwe webpage that houses the graph.
 * It also features the options to call the hotline, as well as immediate sign out
 * 
 * @author Dor Samet
 */
@SuppressLint("SetJavaScriptEnabled")
public class GraphActivity extends SessionActivity {
	//extends SessionActivity

	/**
	 * Loads the web view by sending an HTTP POST to the website. Currently not in HTTPS
	 * 
	 * Consider removing the Lint warning about the Javascript
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);

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

		String postData = PostRequest.securityParameters(null);
		String graphUrl = getApplicationContext().getString(R.string.graph_url);
		browser.postUrl(graphUrl, EncodingUtils.getBytes(postData, "BASE64"));
	}

}
