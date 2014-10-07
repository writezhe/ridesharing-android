package org.beiwe.app.networking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

public class AsyncPostSender extends AsyncTask<Void, Void, Void>{
	
	//TODO: Dori.  Document.
	
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
	public AsyncPostSender(String url, Activity activity, LoginSessionManager session) {
		this.url = url;
		this.activity = activity;
		this.session = session;
	}
	
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
	
	@Override
	protected void onPreExecute() {
		setupProgressBar();
		bar.setVisibility(View.VISIBLE);
	}
	
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
		if (response == 200) { 
			if ( !session.isRegistered() ) {
				session.setRegistered(true);
			}
			if (newPassword != null) {
				HashMap<String, String> details = session.getUserDetails();
				session.createLoginSession(details.get(LoginSessionManager.KEY_ID), EncryptionEngine.safeHash(newPassword));
				newPassword = null;
			}
			// TODO: When this goes to production - change DebugInterfaceActivity to MainMenuActivity.
			activity.startActivity(new Intent(activity.getApplicationContext(), DebugInterfaceActivity.class));
			activity.finish();
		} else { 				
			alertUser(activity);
		}
	}
	
	private void alertUser(final Activity activity) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				AlertsManager.showAlert(NetworkUtilities.handleServerResponseCodes(response), activity);
			}
		});
	}
}