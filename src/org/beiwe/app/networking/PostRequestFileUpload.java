package org.beiwe.app.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	
	/**
	 * A method used to not block running UI thread. Calls for a connection on a separate Callable (thread...).
	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
	 * 
	 * @param parameters
	 * @param connection
	 * @return serverResponseCode
	 */
	public static String make_request(final String parameters, final String url ) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> new_thread = new Callable<Integer>() {
			//#FIXME: this is entirely unhandled, there is no response back to the regular code based on execution of the callable tast, anything using this should swap over to the async task.
			@Override
			public Integer call() throws Exception {
				doPostRequest( parameters, new URL(url) );
				return 0;
			}
		};
		executor.submit(new_thread);
		return "5";
	}
	
	public static String make_request_on_async_thread( String parameters, String url ) {
		
		try {
			return doPostRequest( parameters, new URL(url) );
		} catch (MalformedURLException e) {
			Log.e("PosteRequestFileUpload", "malformed URL");
			e.printStackTrace(); //
		}
		return ""; //FIXME: Eli/Dori.  This is terrible, 
	}
	
	private static String doPostRequest(String parameters, URL uploadUrl)  {
		
		// Create a new HttpURLConnection and set its parameters
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) uploadUrl.openConnection();
		
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");		
					
			Log.i("POSTRequest", NetworkUtilities.makeDefaultParameters() );
				
			DataOutputStream request = new DataOutputStream(connection.getOutputStream());
			request.writeUTF( NetworkUtilities.makeDefaultParameters() );
			request.writeUTF( parameters );
			request.flush();
			request.close();
			
			Log.i("POSTREQUESTFILEUPLOAD", "MESSAGE = " + connection.getResponseMessage());
			Log.i("POSTREQUESTFILEUPLOAD", "CODE = " + connection.getResponseCode());
			
			InputStreamReader response = new InputStreamReader(connection.getInputStream());
			int c;
			StringBuilder builder = new StringBuilder();
			while ((c = response.read()) != -1) {
				builder.append((char) c);
				Log.i("POSTREQUEST", builder.toString());
			}
			
			return builder.toString();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
}
