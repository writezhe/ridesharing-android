package org.beiwe.app.networking;

import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.ui.AlertsManager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

/**
 * 
 * @author Dor Samet
 *
 */

public class AsyncPostSender extends AsyncTask<Void, Void, Void>{
		
	// Private fields
	private int response;
	private String url;
	private Activity activity;
	private LoginSessionManager session;
	private View bar;
	private String newPassword = null;
	
	/* ************************************************************************
	 * **************************** Constructor *******************************
	 * ************************************************************************/
	/** This constructor is used for normal post requests, as well as the registration requests*/
	public AsyncPostSender(String url, Activity activity, LoginSessionManager session) {
		this.url = url;
		this.activity = activity;
		this.session = session;
	}
	
	/** The same as the previous constructor, but used to assign a new password*/
	public AsyncPostSender(String url, Activity activity, LoginSessionManager session, String newPassword) {
		this.url = url;
		this.activity = activity;
		this.session = session;
		this.newPassword = newPassword;
		Log.i("Async", "Created");
	}
	
	/* ************************************************************************
	 * ************************* Convenience Functions ************************
	 * ********************************************************************** */
	public void setupProgressBar() {
		bar = (ProgressBar) activity.findViewById(R.id.progressBar);
	}
	
	/* ************************************************************************
	 * ************** Functions that deal with logging in *********************
	 * ********************************************************************** */
	
	// Set up the progress bar
	@Override
	protected void onPreExecute() {
		setupProgressBar();
		bar.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * Check what kind of post request needs to be sent, depending on the URL ending.
	 * URL endings that need special care are register_user (uses bluetooth ID field) 
	 * and set_password (uses new_password field).
	 */
	// 
	@Override
	protected Void doInBackground(Void... arg0) {
		String parameters;
		if (url.endsWith("register_user")) {
			Log.i("AsyncPostSender", "Register User Post Request");
			parameters = NetworkUtilities.makeFirstTimeParameters();
			response = PostRequest.make_register_request_on_async_thread(parameters, url);
		} else if (url.endsWith("set_password")){
			Log.i("AsyncPostSender", "Reset Password Post Request");
			parameters = NetworkUtilities.makeResetPasswordParameters(newPassword);
			response = PostRequest.make_post_request_on_async_thread(parameters, url);
		} else {
			Log.i("AsyncPostSender", "Normal Post Request");
			parameters = NetworkUtilities.makeDefaultParameters();
			response = PostRequest.make_post_request_on_async_thread(parameters, url);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		bar.setVisibility(View.GONE);
		
		// If the response is 200 and the session is not registered, set it to be true
		if (response == 200) { 
			if ( !session.isRegistered() ) {
				session.setRegistered(true);
			}
			// If the user wants to reset their password, log them in using the new password
			if (newPassword != null) {
				HashMap<String, String> details = session.getUserDetails();
				session.createLoginSession(details.get(LoginSessionManager.KEY_ID), EncryptionEngine.safeHash(newPassword));
				newPassword = null;
			}
			// TODO: When this goes to production - change DebugInterfaceActivity to MainMenuActivity.
			activity.startActivity(new Intent(activity.getApplicationContext(), DebugInterfaceActivity.class));
			activity.finish();
		} else { 				
			// If the server did not send back a 200 OK, then display the error message to the user
			alertUser(activity);
		}
	}
	
	/**
	 * Pops up an alert with the interpreted message from the server, according to the 
	 * response code received
	 * @param activity
	 */
	private void alertUser(final Activity activity) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				AlertsManager.showAlert(NetworkUtilities.handleServerResponseCodes(response), activity);
			}
		});
	}
}