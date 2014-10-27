package org.beiwe.app.networking;

import org.beiwe.app.R;
import org.beiwe.app.ui.AlertsManager;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class HTTPAsync extends AsyncTask<Void, Void, Void> {
	//Private UI element
	private View alertSpinner;
	
	// Common variables
	protected String url;
	protected String parameters = "";
	protected Activity activity;
	protected int response = -1;
	
	
	public HTTPAsync(String url, Activity activity) {
		this.url = url;
		this.activity = activity;
		this.execute(); //Wow, you can do this?
	}
	
	
	// Set up the progress bar
	@Override
	protected void onPreExecute() {
		alertSpinner = (ProgressBar) activity.findViewById(R.id.progressBar);
		alertSpinner.setVisibility(View.VISIBLE);
	}
	
	
	//code should override the doInBackground function for their own needs, code should not call super.
	@Override
	protected Void doInBackground(Void... arg0) {
		Log.e("AsyncPostRequest", "You are not using this right, exiting program for your own good");
		System.exit(1);
		return null;  //whhhyyy java.  just why.
	}
	
	
	//code should override the onPostExecute function for their own needs, code sholud call super.
	@Override
	protected void onPostExecute(Void arg) {
		alertSpinner.setVisibility(View.GONE);
		alertUser();
	}
	
	
	/**Pops up an alert with the interpreted message from the server, according to the 
	 * response code received
	 * @param the response HTTP code from the PostRequest */
	protected void alertUser() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (response == -1) Log.e("SimpleAsync", "WOAH WOAH WOAH, YOU ARE FAILING TO HANDLE THE RESPONSE VARIABLE.");
				if (response != 200) AlertsManager.showAlert(PostRequest.handleServerResponseCodes(response), activity);
			}
		} );
	}
	
	
}