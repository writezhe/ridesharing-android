package org.beiwe.app.networking;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import org.beiwe.app.CrashHandler;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.storage.SetDeviceSettings;
import org.beiwe.app.storage.TextFileManager;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

/** PostRequest is our class for handling all HTTP operations we need; they are all in the form of HTTP post requests. 
 * All HTTP connections are HTTPS, and automatically include a password and identifying information. 
 * @author Josh, Eli, Dor */

//TODO: Low priority. Eli. clean this up and update docs. It does not adequately state that it puts into any request automatic security parameters, and it is not obvious why some of the functions exist (minimal http thing)
public class PostRequest {
	private static Context appContext;	

	/**Uploads must be initialized with an appContext before they can access the wifi state or upload a _file_. */
	private PostRequest( Context applicationContext ) { appContext = applicationContext; }

	/** Simply runs the constructor, using the applcationContext to grab variables.  Idempotent. */
	public static void initialize(Context applicationContext) { new PostRequest(applicationContext); }


	/*##################################################################################
	 ##################### Publicly Accessible Functions ###############################
	 #################################################################################*/


	/**For use with Async tasks.
	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
	 * @param parameters HTTP parameters
	 * @return serverResponseCode */
	public static int httpRegister( String parameters, String url ) {
		try {
			return doRegisterRequest( parameters, new URL(url) ); }
		catch (MalformedURLException e) {
			Log.e("PostRequestFileUpload", "malformed URL");
			e.printStackTrace(); 
			return 0; }
		catch (IOException e) {
			e.printStackTrace();
			Log.e("PostRequest","Network error: " + e.getMessage());
			return 502; }
	}


	/**For use with Async tasks.
	 * Makes an HTTP post request with the provided URL and parameters, returns the server's response code from that request
	 * @param parameters HTTP parameters
	 * @return an int of the server's response code from the HTTP request */
	public static int httpRequestcode( String parameters, String url, String newPassword ) {
		try {
			return doPostRequestGetResponseCode( parameters, new URL(url), newPassword ); }
		catch (MalformedURLException e) {
			Log.e("PosteRequestFileUpload", "malformed URL");
			e.printStackTrace(); 
			return 0; }
		catch (IOException e) {
			Log.e("PostRequest","Unable to establish network connection");
			return 502; }
	}

	/**For use with Async tasks.
	 * Makes an HTTP post request with the provided URL and parameters, returns a string of the server's entire response. 
	 * @param parameters HTTP parameters
	 * @param urlString a string containing a url
	 * @return a string of the contents of the return from an HTML request.*/
	//TODO: Eli. low priority. investigate the android studio warning about making this a package local function
	public static String httpRequestString(String parameters, String urlString)  {
		try { return doPostRequestGetResponseString( parameters, urlString ); }
		catch (IOException e) {
			Log.e("PostRequest error", "Download File failed with exception: " + e);
			e.printStackTrace();
			throw new NullPointerException("Download File failed."); }
	}
	
	/*##################################################################################
	 ################################ Common Code ######################################
	 #################################################################################*/

	/**Creates an HTTP connection with minimal settings.  Some network funcitonality
	 * requires this minimal object.
	 * @param url a URL object
	 * @return a new HttpsURLConnection with minimal settings applied
	 * @throws IOException This function can throw 2 kinds of IO exceptions: IOExeptions and ProtocolException*/
	private static HttpsURLConnection minimalHTTP(URL url) throws IOException {
		// Create a new HttpsURLConnection and set its parameters
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(5000);
		return connection;
	}


	/**For use with functionality that requires additional parameters be added to an HTTP operation.
	 * @param parameters a string that has been created using the makeParameters function
	 * @param url a URL object
	 * @return a new HttpsURLConnection with common settings */
	private static HttpsURLConnection setupHTTP( String parameters, URL url, String newPassword ) throws IOException {
		HttpsURLConnection connection = minimalHTTP(url);

		DataOutputStream request = new DataOutputStream( connection.getOutputStream() );
		request.write( securityParameters(newPassword).getBytes() );
		request.write( parameters.getBytes() );
		request.flush();
		request.close();

		return connection;
	}

	/**Reads in the response data from an HttpsURLConnection, returns it as a String.
	 * @param connection an HttpsURLConnection
	 * @return a String containing return data
	 * @throws IOException */
	private static String readResponse(HttpsURLConnection connection) throws IOException {
		Integer responseCode = connection.getResponseCode();
		if (responseCode == 200) {
			BufferedReader reader = new BufferedReader(new InputStreamReader( new DataInputStream( connection.getInputStream() ) ) );
			String line;
			StringBuilder response = new StringBuilder();
			while ( (line = reader.readLine() ) != null) { response.append(line); }
			return response.toString();
		}
		return responseCode.toString();
	}


	/*##################################################################################
	 ####################### Actual Post Request Functions #############################
	 #################################################################################*/
	
	private static String doPostRequestGetResponseString(String parameters, String urlString) throws IOException {
		HttpsURLConnection connection = setupHTTP( parameters, new URL( urlString ), null );
		connection.connect();
		String data = readResponse(connection);
		connection.disconnect();
		return data;
	}


	private static int doPostRequestGetResponseCode(String parameters, URL url, String newPassword) throws IOException {
		HttpsURLConnection connection = setupHTTP(parameters, url, newPassword);
		int response = connection.getResponseCode();
		connection.disconnect();
		return response;
	}


	private static int doRegisterRequest(String parameters, URL url) throws IOException {
		HttpsURLConnection connection = setupHTTP(parameters, url, null);
		int response = connection.getResponseCode();
		if ( response == 200 ) {
			String responseBody = readResponse(connection);
			try {
				JSONObject responseJSON = new JSONObject(responseBody);
				String key = responseJSON.getString("client_public_key");
				writeKey(key, response);
				JSONObject deviceSettings = responseJSON.getJSONObject("device_settings");
				SetDeviceSettings.writeDeviceSettings(deviceSettings);
			} catch (JSONException e) { CrashHandler.writeCrashlog(e, appContext); }
		}
		connection.disconnect();
		return response;
	}

	
	private static int writeKey(String key, int httpResponse) {
		if ( !key.startsWith("MIIBI") ) {
			Log.e("PostRequest - register", " Received an invalid encryption key from server: " + key );
			return 2; }
		// Log.d( "PostRequest", "Received a key: " + key );
		TextFileManager.getKeyFile().deleteSafely();
		TextFileManager.getKeyFile().writePlaintext( key );
		return httpResponse;
	}
	

	/** Constructs and sends a multipart HTTP POST request with a file attached.
	 * This function uses minimalHTTP() directly because it needs to add a header (?) to the HttpsURLConnection object before it writes a file to it.
	 * Based on http://stackoverflow.com/a/11826317
	 * @param file the File to be uploaded
	 * @param uploadUrl the destination URL that receives the upload
	 * @return HTTP Response code as int
	 * @throws IOException */
	private static int doFileUpload(File file, URL uploadUrl) throws IOException {
		HttpsURLConnection connection = minimalHTTP( uploadUrl );
		DataOutputStream request = new DataOutputStream( connection.getOutputStream() );
		//insert the multipart parameter here.
		DataInputStream inputStream = new DataInputStream( new FileInputStream(file) );

		request.writeBytes( securityParameters(null) );
		request.writeBytes( makeParameter("file_name", file.getName() ) );
		request.writeBytes( "file=" );

		// Read in data from the file, and pour it into the POST request stream
		int data;
		while( ( data = inputStream.read() ) != -1 ) request.write( (char) data );
		inputStream.close();
		
		request.writeBytes("");
		request.flush();
		request.close();

		// Get HTTP Response
		// Log.d("PostRequest.java", "File: " + file.getName() + " Response: " + connection.getResponseMessage());
		int response = connection.getResponseCode();
		connection.disconnect();
		return response;
	}


	//#######################################################################################
	//################################## File Upload ########################################
	//#######################################################################################


	/** Uploads all available files on a separate thread. */
	public static void uploadAllFiles() {
		Log.i("DOING UPLOAD STUFF", "DOING UPLOAD STUFF");
		// determine if you are allowed to upload over WiFi or cellular data, return if not.
		if ( !NetworkUtility.canUpload(appContext) ) { return; }
		
		// Run the HTTP POST on a separate thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable <HttpsURLConnection> thread = new Callable<HttpsURLConnection>() {
			@Override
			public synchronized HttpsURLConnection call() {
				// Log.i("PostRequest.java", "Uploading files");
				doTryUploadDelete();
				Log.i("DOING UPLOAD STUFF", "DONE DOING UPLOAD STUFF");
				return null; //(indentation was stupid, made a function.)
			}
		};
		executor.submit(thread);
	}


	/** For each file name given, tries to upload that file.  If successful it then deletes the file.*/
	private static synchronized void doTryUploadDelete() {
		String[] files = TextFileManager.getAllUploadableFiles();
		for (String fileName : files) {
			try {
				if ( tryToUploadFile(fileName) ) { TextFileManager.delete(fileName); } }
			catch (IOException e) {
				Log.w( "PostRequest.java", "Failed to upload file " + fileName + ". Raised exception: " + e.getCause() );
			}
		}
		// Log.i("PostRequest.java", "Finished upload loop.");
	}


	/**Try to upload a file to the server
	 * @param filename the short name (not the full path) of the file to upload
	 * @return TRUE if the server reported "200 OK"; FALSE otherwise */
	private static Boolean tryToUploadFile(String filename) throws IOException {
		URL uploadUrl = new URL( appContext.getResources().getString(R.string.data_upload_url) );
		File file = new File( appContext.getFilesDir() + "/" + filename );
		
		if ( PostRequest.doFileUpload( file, uploadUrl ) == 200 ) { return true; }
		return false;
	}


	//#######################################################################################
	//############################### UTILITY FUNCTIONS #####################################
	//#######################################################################################

	public static String makeParameter(String key, String value) { return key + "=" + value + "&"; }

	/** Create the 3 standard security parameters for POST request authentication.
	 *  @param newPassword If this is a Forgot Password request, pass in a newPassword string from
	 *  a text input field instead of from the device storage.
	 *  @return a String of the securityParameters to append to the POST request */
	public static String securityParameters(String newPassword) {
		String patientId = PersistentData.getPatientID();
		String deviceId = DeviceInfo.getAndroidID();
		String password = PersistentData.getPassword();
		if (newPassword != null) password = newPassword;

		return makeParameter("patient_id", patientId) +
				makeParameter("password", password) +
				makeParameter("device_id", deviceId);
	}
}