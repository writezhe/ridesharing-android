package org.beiwe.app.networking;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.HttpPost;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class NetworkUtilities {
		
	private Context appContext;
	private static String patientID;
	private static String password;

	/**Upload must be initialized with an appContext before they can access the wifi state or upload a _file_.
	 * @param some applicationContext */
	public NetworkUtilities(Context applicationContext) {
		this.appContext = applicationContext;
		
		LoginSessionManager session = new LoginSessionManager(appContext);
		patientID = session.getUserDetails().get(LoginSessionManager.KEY_ID);
		Log.i("NetworkUtilities", patientID);
		password = session.getUserDetails().get(LoginSessionManager.KEY_PASSWORD);
	}
	
	public void startNetworkUtilities(Context applicationContext) { new NetworkUtilities(applicationContext); }
		
	//#######################################################################################
	//#############################  WIFI STATE #############################################
	//#######################################################################################
	
	/**Return TRUE if WiFi is connected; FALSE otherwise
	 * @return boolean value of whether the wifi is on and connected. */
	//TODO: Josh.  Can you update the comment above: does it return wifi is on or wifi is on + internet is available.  (this may require a bit of testing on a phone?)
	public Boolean getWifiState() {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected(); 
	}

	//#######################################################################################
	//#############################  GETTERS ################################################
	//#######################################################################################

	//todo: remove this hardcoded...
	public static String getPatientID() {
//		return "steve";
		return patientID;
		
	}
	
	// TODO: Eli - Explain security implication of hashing password AGAIN
	//	also remove this hardcoding.
	public static String getUserPassword() {
//		return EncryptionEngine.hash(password);
		return EncryptionEngine.safeHash(password);
	}


	//#######################################################################################
	//################################## FILES ##############################################
	//#######################################################################################

	/**Loop through all files on the phone, and for each one, try to upload it
	 * to the server. If upload is successful, delete the file's local copy. */
	public void uploadAllFiles() {

		// Run the HTTP POST on a separate, non-blocking thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<HttpPost> thread = new Callable<HttpPost>() {
			@Override
			public HttpPost call() {
				String[] files = TextFileManager.getAllUploadableFiles();

				for (String fileName : files) {
					try {
						Log.i("Upload.java", "Trying to upload file: " + fileName);
						tryToUploadAndThenDeleteFile(fileName);
					}
					catch (Exception e) {
						Log.i("Upload.java", "Failed to upload file: " + fileName);
						e.printStackTrace();
					}
				}
				Log.i("Upload.java", "Finished upload loop");				

				return null;
			}
		};
		//this is how you... "submit" a thread.
		executor.submit(thread);
	}


	/**Try to upload a file to the server, and if successful, delete the local
	 * (on-phone) copy of the file to save space, keep security, and not have
	 * to upload it again
	 * @param filename the short name (not the full path) of the file to upload */
	private void tryToUploadAndThenDeleteFile(String filename) {
		//TODO: Josh! Only try to upload if the WiFi is connected
		if (tryToUploadFile(filename)) {
			TextFileManager.delete(filename);
		}
	}


	/**Try to upload a file to the server
	 * @param filename the short name (not the full path) of the file to upload
	 * @return TRUE if the server reported "200 OK"; FALSE otherwise */
	private Boolean tryToUploadFile(String filename) {
		try {
			// Get the filePath, and the file
			String filePath = appContext.getFilesDir() + "/" + filename;
			File file = new File(filePath);

			// Try to upload the file via a Multipart POST request
			URL uploadUrl = new URL(appContext.getResources().getString(R.string.data_upload_url));
			PostRequest postRequest = new PostRequest();
			if (postRequest.doPostRequestFileUpload(file, uploadUrl) == 200) {
				// If the request was successful (returned "200 OK"), return TRUE
				return true;
			}
			else {
				// If the request failed (returned something other than 200), return FALSE
				return false;
			}
		}
		catch (IOException e) {
			// If the upload failed for any reason, return FALSE
			Log.i("Upload", "Failed to upload file " + filename + ". Raised exception " + e.getCause());
			e.printStackTrace();
			return false;
		}
	}

	//#######################################################################################
	//############################### UTILITY FUNCTIONS #####################################
	//#######################################################################################

	
	/**
	 * This is a method used as an intermediate in order to shorten the length of logic trees.
	 * Method checks a given response code sent from the server, and then returns a string corresponding to the code,
	 * in order to display that to the user.
	 * 
	 * @param responseCode
	 * @return String to be displayed on the Alert in case of a problem
	 */
	public static String handleServerResponses (String responseCode) {
		if (responseCode.equals("200")) {return "OK";}
		else if (responseCode.equals("403")) {return "Patient ID did not match Password on the server";}
		else if (responseCode.equals("405")) {return "Phone is not registered to this user. Please contact research staff";}
		else if (responseCode.equals("502")) { return "Please connect to the internet and try again";}
		else { return "Internal server error..."; }
	}
	
	public static String makeParameter(String key, String value){
		return key + "=" + value;
	}
	
	public static String makeDefaultParameters() {
		StringBuilder sentParameters = new StringBuilder();
		sentParameters.append( makeParameter("patient_id", getPatientID() ) + "&"
//				+ makeParameter("password", getUserPassword() ) + "&"
				+ makeParameter("password", "aaa") + "&"
				+ makeParameter("device_id", DeviceInfo.getAndroidID() ) );
		return sentParameters.toString();
	}
	
	public static String makeFirstTimeParameters() {
		StringBuilder sentParameters = new StringBuilder();
		sentParameters.append( makeParameter("bluetooth_id", DeviceInfo.getBlootoothMAC() ) );
		return sentParameters.append("&" + makeDefaultParameters()).toString();
	}
}
