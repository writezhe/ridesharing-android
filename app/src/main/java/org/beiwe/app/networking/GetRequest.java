package org.beiwe.app.networking;

import android.content.Context;
import android.util.Log;

import org.beiwe.app.CrashHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/** PostRequest is our class for handling all HTTP operations we need; they are all in the form of HTTP post requests.
 * All HTTP connections are HTTPS, and automatically include a password and identifying information. 
 * @author Josh, Eli, Dor */

//TODO: Low priority. Eli. clean this up and update docs. It does not adequately state that it puts into any request automatic security parameters, and it is not obvious why some of the functions exist (minimal http thing)
public class GetRequest {
	private static Context appContext;

	/**Uploads must be initialized with an appContext before they can access the wifi state or upload a _file_. */
	private GetRequest(Context applicationContext ) { appContext = applicationContext; }

	/** Simply runs the constructor, using the applcationContext to grab variables.  Idempotent. */
	public static void initialize(Context applicationContext) { new GetRequest(applicationContext); }

	public static String makeParameter(String key, String value) { return key + "=" + value + "&"; }

	/**For use with Async tasks.
	 * This opens a connection with the server, sends the HTTP parameters, then receives a response code, and returns it.
	 * @return serverResponseCode */
	public static JSONObject httpGetResponse(String parameters, String url, List<String> headers) {
		try {
			return doRequest(new URL(url + parameters), headers); }
		catch (MalformedURLException e) {
			Log.e("PostRequestFileUpload", "malformed URL");
			e.printStackTrace();
			return null; }
		catch (Exception e) {
			e.printStackTrace();
			Log.e("PostRequest","Network error: " + e.getMessage());
			return null; }
	}

	private static JSONObject doRequest(URL url, List<String> header) throws IOException {
		HttpsURLConnection connection = setupHTTPGet(url, null);
		for (String s : header) {
			connection.setRequestProperty(s.split(",")[0], s.split(",")[1]);
		}
		JSONObject responseJSON = new JSONObject();
		int response = connection.getResponseCode();
		if ( response == 200 ) {
			String responseBody = readResponseGet(connection);
			try {
				responseJSON = new JSONObject(responseBody);
			} catch (JSONException e) { CrashHandler.writeCrashlog(e, appContext); }
		}
		connection.disconnect();
		return responseJSON;
	}

	private static HttpsURLConnection setupHTTPGet(URL url, String newPassword ) throws IOException {
		HttpsURLConnection connection = minimalHTTPGet(url);

		return connection;
	}

	private static String readResponseGet(HttpsURLConnection connection) throws IOException {
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

	private static HttpsURLConnection minimalHTTPGet(URL url) throws IOException {
		// Create a new HttpsURLConnection and set its parameters
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		//connection.setUseCaches(false);
		//connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		//connection.setRequestProperty("Connection", "Keep-Alive");
		//connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(5000);
		return connection;
	}

}