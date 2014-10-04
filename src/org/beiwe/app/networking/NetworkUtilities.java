package org.beiwe.app.networking;


import java.io.File;
import java.io.IOException;
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
		
	private static Context appContext;
	private static String patientID;
	private static String password;

	/**Upload must be initialized with an appContext before they can access the wifi state or upload a _file_.
	 * @param some applicationContext */
	private NetworkUtilities(Context applicationContext) {
		appContext = applicationContext;
		//TODO: Dori.  Why do we need a NEW loginsessionmanager here? 
		LoginSessionManager session = new LoginSessionManager(appContext);
		patientID = session.getUserDetails().get(LoginSessionManager.KEY_ID);
		password = session.getUserDetails().get(LoginSessionManager.KEY_PASSWORD);
		Log.i("NetworkUtilities", patientID);
	}
	
	/** Simply runs the constructor, using the applcationContext to grab variables.  Idempotent. */
	public static void initializeNetworkUtilities(Context applicationContext) { new NetworkUtilities(applicationContext); }
	private static String getUserPassword() { return EncryptionEngine.safeHash(password); }
	
	//#######################################################################################
	//#############################  WIFI STATE #############################################
	//#######################################################################################
	
	/**Return TRUE if WiFi is connected; FALSE otherwise
	 * @return boolean value of whether the wifi is on and network connectivity is available. */
	public static Boolean getWifiState() {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected(); 
	}

	//#######################################################################################
	//################################## FILES ##############################################
	//#######################################################################################

	/** Uploads all available files on a separate thread. */
	public static void uploadAllFiles() {
		// Run the HTTP POST on a separate thread
		if ( !getWifiState() ) { return; }
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		Callable<HttpPost> thread = new Callable<HttpPost>() {
			@Override
			public HttpPost call() {
				doTryUploadDelete( TextFileManager.getAllUploadableFiles() ) ;
				return null;
			}
		};
		executor.submit(thread);
	}
	
	/** For each file name given tries to upload that file.  If successful it then deletes the file.*/
	public static void doTryUploadDelete(String[] files) {
		for (String fileName : files) {
			try {
				if ( tryToUploadFile(fileName) ) { TextFileManager.delete(fileName); }
			}
			catch (Exception e) {
				Log.i("NetworkUtilities", "Failed to upload file: " + fileName);
				e.printStackTrace(); }
		}
		Log.i("NetworkUtilities", "Finished upload loop.");				
	}
	
	
	/**Try to upload a file to the server
	 * @param filename the short name (not the full path) of the file to upload
	 * @return TRUE if the server reported "200 OK"; FALSE otherwise */
	private static Boolean tryToUploadFile(String filename) {
		try {
			URL uploadUrl = new URL(appContext.getResources().getString(R.string.data_upload_url));
			File file = new File( appContext.getFilesDir() + "/" + filename );

			if ( PostRequest.doPostRequestFileUpload(file, uploadUrl) == 200) {
				// request was successful (returned "200 OK"), return TRUE
				return true; }
			else {
				// request failed (returned something other than 200), return FALSE
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

	/**This is a method used as an intermediate in order to shorten the length of logic trees.
	 * Method checks a given response code sent from the server, and then returns a string corresponding to the code,
	 * in order to display that to the user.
	 * @param responseCode
	 * @return String to be displayed on the Alert in case of a problem	 */
	public static String handleServerResponseCodes(String responseCode) {
		if (responseCode.equals("200")) {return "OK";}
		else if (responseCode.equals("403")) {return "Patient ID did not match Password on the server";}
		else if (responseCode.equals("405")) {return "Phone is not registered to this user. Please contact research staff";}
		else if (responseCode.equals("502")) { return "Please connect to the internet and try again";}
		else { return "Internal server error..."; }
	}
	
	public static String makeDefaultParameters() {
		StringBuilder sentParameters = new StringBuilder();
		sentParameters.append( makeParameter("patient_id", patientID ) +
//				makeParameter("password", getUserPassword() ) +
				makeParameter("password", "aaa") +
				makeParameter("device_id", DeviceInfo.getAndroidID() ) );
		return sentParameters.toString();
	}
	
	public static String makeFirstTimeParameters() {
		StringBuilder sentParameters = new StringBuilder();
		sentParameters.append( makeParameter("bluetooth_id", DeviceInfo.getBlootoothMAC() ) );
		return sentParameters.append("&" + makeDefaultParameters()).toString();
	}
	
	public static String makeParameter(String key, String value){ return key + "=" + value + "&"; }
}
