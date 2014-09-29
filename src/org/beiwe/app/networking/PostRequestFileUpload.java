package org.beiwe.app.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import org.beiwe.app.DeviceInfo;

import android.util.Log;

public class PostRequestFileUpload {
	
	static String twoHyphens = "--";
	static String boundary = "gc0p4Jq0M2Yt08jU534c0p";
	static String newLine = "\r\n";
	static String attachmentName = "file";
	
	
	/**
	 * Constructs and sends a multipart HTTP POST request with a file attached
	 * Based on http://stackoverflow.com/a/11826317
	 * @param file the File to be uploaded
	 * @param uploadUrl the destination URL that receives the upload
	 * @return HTTP Response code as int
	 * @throws IOException
	 */
	public static int sendPostRequest(File file, URL uploadUrl) throws IOException {		
		
		// Variables
		String patientID = NetworkUtilities.getPatientID();
		String password = NetworkUtilities.getUserPassword();
		String androidInfo = DeviceInfo.getAndroidID();
		
		// Create a new HttpURLConnection and set its parameters
		HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		
		// Create the POST request as a DataOutputStream to the HttpURLConnection
		DataOutputStream request = new DataOutputStream(connection.getOutputStream());
		
		// Add the filename as a parameter to the POST request 
		request.writeBytes(twoHyphens + boundary + newLine);
		request.writeBytes("Content-Disposition: form-data; name=\""
				+ attachmentName + "\";filename=\"" + file.getName() + "\"" + newLine);
		request.writeBytes(newLine);
		
		// Read in data from the file, and pour it into the POST request
		DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
		int data;
		try {
			while( (data = inputStream.read()) != -1)
				request.write((char)data);
		}
		catch (IOException e) {
			Log.i("Upload", "read error in " + file.getName());
			e.printStackTrace();
			throw e;
		}
		inputStream.close();
		
		// Add a closing boundary and other data to mark the end of the POST request 
		request.writeBytes(newLine);
		request.writeBytes(twoHyphens + boundary + twoHyphens + newLine);
		
		request.flush();
		request.close();
		
		// Get HTTP Response
		Log.i("POSTREQUESTFILEUPLOAD", "RESPONSE = " + connection.getResponseMessage());
		return connection.getResponseCode();
	}
	
	
	public static String sendPostRequest(URL uploadUrl) throws IOException {
		
		// Variables
		String patientID = NetworkUtilities.getPatientID();
		String password = NetworkUtilities.getUserPassword();
		String androidInfo = DeviceInfo.getAndroidID();

//		String parameters = "patientID=" + patientID + "&pwd=" + password + "&androidID=" + androidInfo;
		// Create a new HttpURLConnection and set its parameters
		HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setRequestProperty("patiendID", patientID);
		connection.setRequestProperty("pwd", password);
		connection.setRequestProperty("androidID", androidInfo);

		
		// Create the POST request as a DataOutputStream to the HttpURLConnection
		return serverResponse(connection);
	}
	
	/**
	 * A method used to not block running UI thread. Calls for a connection on a separate Callable (thread...).
	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
	 * 
	 * @param parameters
	 * @param connection
	 * @return serverResponseCode
	 */
	private static String serverResponse (final HttpURLConnection connection) {
		// -----  In-method definition of the new thread ----- 
		Callable<Integer> thread = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				DataOutputStream request = new DataOutputStream(connection.getOutputStream());
				request.flush();
				request.close();
				
				// Get HTTP Response
				Log.i("POSTREQUESTFILEUPLOAD", "RESPONSE = " + connection.getResponseMessage());
				return connection.getResponseCode();
			}
		};
		
		// ----- Actual code starts here ----- 
		String response = "403";
		try {
			response = "" + thread.call();
			Log.i("ResponseCode", response);
			return response;
		} catch (Exception e) {
			e.printStackTrace(); 
			return response;
		}
	}
}
