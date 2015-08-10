package org.beiwe.app.networking;

import android.os.AsyncTask;
import android.util.Log;

//TODO: Low priority: Eli. Redoc.

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
public class HTTPAsync extends AsyncTask<Void, Void, Void> {
	
	protected String url;
	protected String parameters = "";
	protected int response = -1;
	protected String responseString = null; //if this variable is still null after an attempt to execute then the request failed.
	
	/**An HTTPAsyc instance will begin execution immediately upon instantiation.
	 * @param url a string containing The URL with which you will connect. 
	 * @param activity The current visible activity */
	public HTTPAsync(String url) { this.url = url; }
	
	/** Your code should override doInBackground function, do NOT call super.doInBackground().*/
	@Override
	protected Void doInBackground(Void... arg0) {
		Log.e("AsyncPostRequest", "You are not using this right, exiting program for your own good");
		System.exit(1);
		return null; //Hate.
	}
	
	/** Your code should override the onPostExecute function, and handle
	 * any additional special response and user notification logic.
	 * If you do not override the app will log any bad responses from the HTTP request.*/
	@Override
	protected void onPostExecute(Void arg) { alertSystem(); }
	
	/**Does the logging operation executed in onPostExecute.*/
	protected void alertSystem() {
		if ( (response == -1) && (responseString.length() > 3) ) {
			//TODO: Eli. find a better way to do this/determine if we actually need to do this. whatever we do, document it.  This is spamming logcat.
			Log.w("HTTPAsync", "DOING SOMETHING DANGEROUS");
			try { response = Integer.parseInt( responseString.substring(0, 3) ); }
			catch (NumberFormatException e) { Log.w("HTTPAsync", "NOPE, DIDN'T WORK."); }
		}
		
		if (response == -1 && responseString == null) {
			Log.e("HTTPAsync", "WARNING: the response and responseString variables were never set, HTTPAsync is unable check validity."); }
		else if ((response == -1) && (responseString.length() == 3)) {		
			Log.e("HTTPAsync", responseCodeAlert( Integer.parseInt(responseString)));  }
		else if (response != 200) {
			Log.e("HTTPAsync", responseCodeAlert(response)); }
	}
	
	
	//TODO: Low priority. Eli/Josh.  move these error messages to strings.xml
	/**Checks a given response code sent from the server, and then returns a string corresponding to that code's meaning.
	 * @param responseCode
	 * @return String to be displayed on the Alert in case of a problem	 */
	public static String responseCodeAlert(int responseCode) {
		if (responseCode == 200) {return "OK";}
		else if (responseCode == 403) { return "The patient ID or password did not match the patient ID or password on the server";}
		else if (responseCode == 405) { return "Phone is not registered to this user. Please contact research staff";}
		else if (responseCode == 502) { return "Please connect to the internet and try again";}
		else if (responseCode == 1) { return "Someone misconfigured the server, please contact staff";}
		else { return "An unknown error occured."; }
	}
}