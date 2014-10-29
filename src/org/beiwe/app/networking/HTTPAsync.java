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
	protected String responseString = null; //if this variable is still null after an attempt to execute then the request failed.
	
	
	public HTTPAsync(String url, Activity activity) {
		this.url = url;
		this.activity = activity;
		this.execute(); //Wow, you can do this?
	}
	
	
	// Set up the progress bar
	@Override
	protected void onPreExecute() {
		alertSpinner = (ProgressBar) activity.findViewById(R.id.progressBar);
		if (alertSpinner != null) alertSpinner.setVisibility(View.VISIBLE);
	}
	
	
	//code should override the doInBackground function for their own needs, code should not call super.
	@Override
	protected Void doInBackground(Void... arg0) {
		Log.e("AsyncPostRequest", "You are not using this right, exiting program for your own good");
		System.exit(1);
		return null; //Hate.
	}
	
	
	//code should override the onPostExecute function for their own needs, code sholud call super.
	@Override
	protected void onPostExecute(Void arg) {
		if (alertSpinner != null) alertSpinner.setVisibility(View.GONE);
		alertUser();
	}
	
	
	/**Pops up an alert with the interpreted message from the server, according to the 
	 * response code received
	 * @param the response HTTP code from the PostRequest */
	protected void alertUser() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				//TODO: Eli...  test this...
				if (response == -1) AlertsManager.showAlert(responseCodeAlert( Integer.parseInt(responseString) ), activity); 
				else if (response != 200) AlertsManager.showAlert( responseCodeAlert(response), activity);
			}
		} );
	}
	
	/**Checks a given response code sent from the server, and then returns a string corresponding to the code,
	 * in order to display that to the user.
	 * @param responseCode
	 * @return String to be displayed on the Alert in case of a problem	 */
	public static String responseCodeAlert(int responseCode) {
		if (responseCode == 200) {return "OK";}
		else if (responseCode == 403) { return "Patient ID did not match Password on the server";}
		else if (responseCode == 405) { return "Phone is not registered to this user. Please contact research staff";}
		else if (responseCode == 502) { return "Please connect to the internet and try again";}
		//TODO: Eli. investigate what response code = 1 means in java? python?
		else if (responseCode == 1) { return "Someone misconfigured the server, please contact staff";}
		else { return "Internal server error..."; }
	}
	
}