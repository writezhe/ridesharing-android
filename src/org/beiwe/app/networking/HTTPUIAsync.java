package org.beiwe.app.networking;

import org.beiwe.app.R;
import org.beiwe.app.ui.utils.AlertsManager;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

/**HTTPAsync is a... special AsyncTask for handling network (HTTP) requests using our PostRequest class.
 * HTTPAsync handles the asynchronous requirement for UI threads, and automatically handles user
 * notification for the well defined HTTP errors.
 * 
 * HTTPAsync objects start executing on instantiation. While working it pops up an android UI spinner.
 * If the spinner UI element ("progressBar"?) is not declared in the activity's manifest it will instead run "silently" 
 * 
 * Inside your overridden doInBackground function you must assign the HTTP return value to either response (as an int)
 * or responseString (as a String, this string must have a length of 3.).
 * 
 * @author Eli */
public class HTTPUIAsync extends HTTPAsync {
	//Private UI element
	private View alertSpinner;
	// Common variables
	protected Activity activity;
	protected int response = -1;
	protected String responseString = null; //if this variable is still null after an attempt to execute then the request failed.
	
	/**An HTTPAsyc instance will begin execution immediately upon instantiation.
	 * @param url a string containing The URL with which you will connect. 
	 * @param activity The current visible activity */
	public HTTPUIAsync(String url, Activity activity) {
		super(url);
		this.activity = activity;
		this.execute(); //Wow, you can do this?
	}
	
	/** You may want to override the onPreExecute function (your pre-logic should occur outside
	 * the instantiation of the HTTPAsync instance), if you do you should call super.onPreExecute()
	 * as the first line in your custom logic. This is when the spinner will appear.*/
	@Override
	protected void onPreExecute() {
		alertSpinner = (ProgressBar) activity.findViewById(R.id.progressBar);
		if (alertSpinner != null) alertSpinner.setVisibility(View.VISIBLE);
	}
	
	/** Your code should override doInBackground function, do NOT call super.doInBackground().*/
	@Override
	protected Void doInBackground(Void... arg0) {
		Log.e("AsyncPostRequest", "You are not using this right, exiting program for your own good");
		System.exit(1);
		return null; //Hate.
	}
	
	/** Your code should override the onPostExecute function, call super.onPostExecute(), and handle
	 * any additional special response and user notification logic required by your code.*/
	@Override
	protected void onPostExecute(Void arg) {
		if (alertSpinner != null) alertSpinner.setVisibility(View.GONE);
		alertUser();
	}
	
	/**Pops up an alert with the interpreted message from the server, according to the 
	 * response code received.  These response codes and messages are specific to the app,
	 * and may not have identical meaning to the (strict) HTTP spec.
	 * @param the response HTTP code from the PostRequest */
	protected void alertUser() {   activity.runOnUiThread(new Runnable() {
		public void run() {
			if (response == -1 && responseString == null) {
				Log.e("HTTPAsync", "WARNING: the response and responseString variables were never set, HTTPAsync is unable to handle user notification.");
				return;
			}
			if ((response == -1) && (responseString.length() == 3)) {
				try {
					//when a PostRequest 
					AlertsManager.showAlert(responseCodeAlert( Integer.parseInt(responseString) ), activity);   }
				catch (Exception e) {
					AlertsManager.showAlert(responseCodeAlert( 1 ), activity); } }
			else if (response != 200) {
				AlertsManager.showAlert(responseCodeAlert(response), activity); }
		} }	);
	}
}