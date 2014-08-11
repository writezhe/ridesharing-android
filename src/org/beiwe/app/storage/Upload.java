package org.beiwe.app.storage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.HttpGet;
import org.beiwe.app.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Upload {
	
	private Context appContext;
	
	
	public Upload(Context applicationContext) {
		this.appContext = applicationContext;
	}
	
	
	/**
	 * Return TRUE if WiFi is connected; FALSE otherwise
	 * @return
	 */
	public Boolean getWifiState() {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected(); 
	}
	
	
	/**
	 * Loop through all files on the phone, and for each one, try to upload it
	 * to the server. If upload is successful, delete the file's local copy. 
	 */
	public void uploadAllFiles() {
		
		// FIXME: is this a GET, not a POST?
	    // Run the HTTP GET on a separate, non-blocking thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<HttpGet> thread = new Callable<HttpGet>() {
			@Override
			public HttpGet call() {
				String[] files = TextFileManager.getAllFilesSafely();
				
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
		executor.submit(thread);
	}
	

	/**
	 * Try to upload a file to the server, and if successful, delete the local
	 * (on-phone) copy of the file to save space, keep security, and not have
	 * to upload it again
	 * @param filename the short name (not the full path) of the file to upload
	 */
	private void tryToUploadAndThenDeleteFile(String filename) {
		if (tryToUploadFile(filename)) {
			TextFileManager.delete(filename);
		}
	}
	
	
	/**
	 * Try to upload a file to the server
	 * @param filename the short name (not the full path) of the file to upload
	 * @return TRUE if the server reported "200 OK"; FALSE otherwise
	 */
	private Boolean tryToUploadFile(String filename) {
		try {
			// Get the filePath, and the file
			String filePath = appContext.getFilesDir() + "/" + filename;
			File file = new File(filePath);

			// Try to upload the file via a Multipart POST request
			URL uploadUrl = new URL(appContext.getResources().getString(R.string.data_upload_url));
			PostRequestFileUpload postRequest = new PostRequestFileUpload();
			if (postRequest.sendPostRequest(file, uploadUrl) == 200) {
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

}
