package org.beiwe.app.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.beiwe.app.DeviceInfo;

import android.util.Log;

//potentially useful:
// http://stackoverflow.com/questions/5643704/reusing-ssl-sessions-in-android-with-httpclient


public class PostRequestFileUpload {
	
	static String twoHyphens = "--";
	static String boundary = "gc0p4Jq0M2Yt08jU534c0p";
	static String newLine = "\n";
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
//		String patientID = NetworkUtilities.getPatientID();
//		String password = NetworkUtilities.getUserPassword();
//		String androidInfo = DeviceInfo.getAndroidID();
		
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
	
	
	public static void make_request( final String url ) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> new_thread = new Callable<Integer>() {
			
			@Override
			public Integer call() throws Exception {
				sendPostRequest( new URL(url) );
				return 5;
			}
		};
		
		executor.submit(new_thread);
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
		
//		connection.addRequestProperty("patient_id", patientID);
//		connection.addRequestProperty("password", password);
//		connection.addRequestProperty("device_id", androidInfo);
//		
		connection.setDoOutput(true);
		
		String steve= "patient_id=" + "test" + "&password="  + "password" +  "&device_id=" + "test_device" ;
		
//		String steve= "patient_id=" +  + "&password="  +  +  "&device_id=" + ;
//		connection.addRequestProperty("patient_id", "test");
//		connection.addRequestProperty("password", "password");
//		connection.addRequestProperty("device_id", "test_device");
		
		
//		// Create the POST request as a DataOutputStream to the HttpURLConnection
//		int responseCode = connection.getResponseCode();
//		String responseMessage = connection.getResponseMessage();
//		connection.disconnect();
//		return responseMessage;
////		
		DataOutputStream request = new DataOutputStream(connection.getOutputStream());
		request.writeUTF(steve);
		request.flush();
		request.close();
		
		
		Log.i("POSTREQUESTFILEUPLOAD", "MESSAGE = " + connection.getResponseMessage());
		Log.i("POSTREQUESTFILEUPLOAD", "CODE = " + connection.getResponseCode());
		
		return "6";
	}
	
//	/**
//	 * A method used to not block running UI thread. Calls for a connection on a separate Callable (thread...).
//	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
//	 * 
//	 * @param parameters
//	 * @param connection
//	 * @return serverResponseCode
//	 */
//	private static String serverResponse (final HttpURLConnection connection) {
//		// -----  In-line definition of the callable ----- 
//		
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		Callable<Integer> new_thread = new Callable<Integer>() {
//			@Override
//			public Integer call() throws Exception {
//				
//				
//				// Get HTTP Response
//				Log.i("POSTREQUESTFILEUPLOAD", "RESPONSE = " + connection.getResponseMessage());
//				return connection.getResponseCode();
//			}
//		};
//		
//		executor.submit(new_thread);
//		
//		// ----- Actual code starts here ----- 
//		String response = "";
//		try {
//			Log.e("PostRequestFileUpload", "line preceding call() function");
//			Thread.sleep(1000, 0);
//			response = "" + new_thread.call();
//			Log.i("ResponseCode", response);
//			return response;
//		} catch (Exception e) {
//			Log.e("PostRequestFileUpload", "did not get proper response from call() function.");
//			e.printStackTrace(); 
//			throw new NullPointerException();
//		}
//	}
}
