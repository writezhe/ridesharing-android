package org.beiwe.app.networking;

import java.util.HashMap;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginManager;
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

//FIXME: This needs to be changed entirely. extend and modify asynctask to make the loading menu happen, internal variables.


public class AsyncPostSender extends AsyncTask<Void, Void, Void> {
	
	// Common variables
	private int response;
	private String url;
	private View alertSpinner;
	

	private Activity activity;  //TODO: Eli. determine if we can kill this
	//TODO: Eli. Kill these
	private String newPassword = null;

	
	/** This constructor is used for normal post requests, as well as the registration requests*/
	public AsyncPostSender(String url, Activity activity) {
		this.url = url;
		this.activity = activity;
	}
	
	/** The same as the previous constructor, but used to assign a new password*/
	public AsyncPostSender(String url, Activity activity, String newPassword) {
		this.url = url;
		this.activity = activity;
		this.newPassword = newPassword;
	}
	
	
	
	
	// Set up the progress bar
	@Override
	protected void onPreExecute() {
		alertSpinner = (ProgressBar) activity.findViewById(R.id.progressBar);
		alertSpinner.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * Check what kind of post request needs to be sent, depending on the URL ending.
	 * URL endings that need special care are register_user (uses bluetooth ID field) 
	 * and set_password (uses new_password field). */
	@Override
	protected Void doInBackground(Void... arg0) {
		String parameters = "";  //TODO: Move parameters up to class variable?
		
		if (url.endsWith("register_user")) {
			parameters = PostRequest.makeParameter("bluetooth_id", DeviceInfo.getBlootoothMAC() );
			response = PostRequest.asyncRegisterHandler(parameters, url);
		} else if ( url.endsWith("set_password") ) {
			parameters = PostRequest.makeParameter( "new_password", newPassword );			
			response = PostRequest.asyncPostHandler( parameters, url );
		} else {
			response = PostRequest.asyncPostHandler( parameters, url );
		}
		return null;  //whhhyyy java.  just why.
	}
	
	@Override
	protected void onPostExecute(Void result) {
		alertSpinner.setVisibility(View.GONE);
		
		// If the response is 200 and the session is not registered, set it to be true
		if (response == 200) { 
			if ( !LoginManager.isRegistered() ) {
				LoginManager.setRegistered(true);
			}
			// If the user wants to reset their password, log them in using the new password
			if (newPassword != null) {
//				LoginManager.setLoginCredentialsAndLogIn( LoginManager.getPatientID(), EncryptionEngine.safeHash(newPassword));
				newPassword = null;
			}
			// FIXME: Eli. This is terrible, change it.
			//  old comment: "When this goes to production - change DebugInterfaceActivity to MainMenuActivity."
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
				AlertsManager.showAlert(responseCodeAlert(response), activity);
			}
		});
	}
	
	//only exists to make code compile, this will be deleted.
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