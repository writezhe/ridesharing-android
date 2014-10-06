package org.beiwe.app.networking;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.beiwe.app.storage.TextFileManager;

import android.util.Log;

//potentially useful:
// http://stackoverflow.com/questions/5643704/reusing-ssl-sessions-in-android-with-httpclient


public class PostRequest {

	static String twoHyphens = "--";
	static String boundary = "gc0p4Jq0M2Yt08jU534c0p";
	static String newLine = "\n"; //we will use unix-style new lines
	static String attachmentName = "file";

	/*##################################################################################
	 ############################ Public Wrappers ######################################
	 #################################################################################*/
	
	/**A method used to not block running UI thread. Calls for a connection on a separate Callable (thread...).
	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
	 * @param parameters
	 * @param connection
	 * @return serverResponseCode */
	public static String make_register_request(final String parameters, final String url ) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> new_thread = new Callable <Integer> () {
			//#FIXME: Dori.  this is entirely unhandled, there is no response back to the regular code based on execution of the callable tast, anything using this should swap over to the async task.
			@Override
			public Integer call() throws Exception { return doRegisterRequest( parameters, new URL(url) ); }
		};
		executor.submit(new_thread);
		return "5";
	}
	
	/**A method used to not block running UI thread. Calls for a connection on a separate Callable (thread...).
	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
	 * @param parameters
	 * @param connection
	 * @return serverResponseCode */
	public static String make_post_request(final String parameters, final String url ) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> new_thread = new Callable <Integer> () {
			//#FIXME: Dori.  this is entirely unhandled, there is no response back to the regular code based on execution of the callable tast, anything using this should swap over to the async task.
			@Override
			public Integer call() throws Exception { return doPostRequest( parameters, new URL(url) ); }
		};
		executor.submit(new_thread);
		return "5";
	}
	
	//FIXME: Dori.  I thought we were using the async request(s) below, not the callable request(s) above, but we are currently using both.
	// If we need both, document what each is for.
	
	public static int make_register_request_on_async_thread( String parameters, String url ) {
		try {
			return doRegisterRequest( parameters, new URL(url) ); }
		catch (MalformedURLException e) {
			Log.e("PosteRequestFileUpload", "malformed URL");
			e.printStackTrace(); 
			return 0; }
		catch (IOException e) {
			Log.e("PostRequest","Unable to establish network connection");
			return 502; }
	}
	
	public static int make_post_request_on_async_thread( String parameters, String url ) {
		try {
			return doPostRequest( parameters, new URL(url) ); }
		catch (MalformedURLException e) {
			Log.e("PosteRequestFileUpload", "malformed URL");
			e.printStackTrace(); 
			return 0; }
		catch (IOException e) {
			Log.e("PostRequest","Unable to establish network connection");
			return 502; }
	}
	
	/*##################################################################################
	 ############################ Private Workers ######################################
	 #################################################################################*/
	
	/**Creates an HTTP connection with common settings (reduces code clutter).
	 * @param url a URL object
	 * @return a new HttpURLConnection with common settings
	 * @throws IOException This function can throw 2 kinds of IO exceptions: IOExeptions ProtocolException*/
	private static HttpURLConnection setupHTTP( URL url) throws IOException {
		// Create a new HttpURLConnection and set its parameters
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");
		//TODO: Dori/Eli/Josh. Test if the timeouts below are compatible with the file upload function.  There is no reason they *shouldn't*, but it is untested.
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(5000);
		return connection;
	}
	
	
	/** Constructs and sends a multipart HTTP POST request with a file attached
	 * Based on http://stackoverflow.com/a/11826317
	 * @param file the File to be uploaded
	 * @param uploadUrl the destination URL that receives the upload
	 * @return HTTP Response code as int
	 * @throws IOException */
	public static int doPostRequestFileUpload(File file, URL uploadUrl) throws IOException {		
	
		HttpURLConnection connection = setupHTTP( uploadUrl );
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		
		// Get the POST request as from the HttpURLConnection, add the filename as a parameter
		DataOutputStream request = new DataOutputStream(connection.getOutputStream());
		request.writeBytes(twoHyphens + boundary + newLine);
		request.writeBytes("Content-Disposition: form-data; name=\""
				+ attachmentName + "\";filename=\"" + file.getName() + "\"" + newLine + newLine);

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

		// Add closing boundary etc. to mark the end of the POST request 
		request.writeBytes(newLine + twoHyphens + boundary + twoHyphens + newLine);
		request.flush();
		request.close();

		// Get HTTP Response
		Log.i("POSTREQUESTFILEUPLOAD", "RESPONSE = " + connection.getResponseMessage());
		return connection.getResponseCode();
	}

	
	public static int doPostRequest(String parameters, URL url) throws IOException {
		HttpURLConnection connection = setupHTTP(url);
		Log.i("PostRequest", "parameters:" + parameters );
		
		DataOutputStream request = new DataOutputStream(connection.getOutputStream());
		request.write( parameters.getBytes() );
		request.flush();
		request.close();
		return connection.getResponseCode();
	}
	
	
	public static int doRegisterRequest(String parameters, URL url) throws IOException {
		HttpURLConnection connection = setupHTTP(url);
		Log.i("PostRequest", "parameters:" + parameters );
		
		DataOutputStream request = new DataOutputStream(connection.getOutputStream());
		request.write( parameters.getBytes() );
		request.flush();
		request.close();
		Log.i("WRITE DOWN KEY", "" + TextFileManager.getKeyFile().read().length());

		// Only write to the keyfile if receiving a 200 OK and the file has nothing in it and parameters have bluetooth_id field
		//check for a key file in the response?
		//TODO: lets just make a separate copy of this function with this in there
		createKey( readResponse(connection) );
		return connection.getResponseCode();
	}
	
	
	/*##################################################################################
	 ######################### Convenience Functions ###################################
	 #################################################################################*/

	
	/**Checks a string to see if it is an RSA key file, if so it writes it to the Key File. 
	 * @param response */
	private static void createKey(String response){
		if ( !response.toString().startsWith("MIIBI") ) {
			throw new NullPointerException (" Wrong encryption key !!!" ); }  //TODO: Dori.  Why does this throw an exception?  I assume the answer is "for debugging?
		Log.i( "POSTREQUEST", "Received a key: " + response.toString() );
		TextFileManager.getKeyFile().write( response.toString() );
	}
	
	
	/**Reads in the response data from an HttpURLConnection, returns it as a String.
	 * @param connection an HttpURLConnection
	 * @return a String containing return data
	 * @throws IOException */
	private static String readResponse(HttpURLConnection connection) throws IOException {
		DataInputStream inputStream = new DataInputStream( connection.getInputStream() );
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream ) );
		String line;
		StringBuilder response = new StringBuilder();
		while ( (line = reader.readLine() ) != null) { response.append(line); }
		return response.toString();
	}
}
