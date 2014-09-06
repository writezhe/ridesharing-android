package org.beiwe.app.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class FileDownloader {

	public static void downloadFileToFile(String urlString, TextFileManager file) {
		try {
			writeStringToFile(downloadFileFromURL(urlString), file);
		} catch (IOException e) {
			Log.i("FileDownloader", "downloadFile or writeStringToFile failed with exception " + e);
		}
	}
	
	public static String downloadFileFromURL(String urlString) throws IOException {
		URL fileURL = new URL(urlString);
		
		// Set up an HTTP connection
		HttpURLConnection connection = (HttpURLConnection) (fileURL.openConnection());

		// Throw a TimeoutException after this many milliseconds if
		connection.setConnectTimeout(3000); // The server hasn't accepted the connection
		connection.setReadTimeout(5000); // This device hasn't received the response
		connection.connect();
		
		// Set up a BufferedReader from the HTTP connection
		InputStreamReader inputReader = new InputStreamReader(connection.getInputStream());
		BufferedReader reader = new BufferedReader(inputReader);
		
		// Set up a StringBuilder (uses less memory than repeatedly appending to a String)
		StringBuilder builder = new StringBuilder();
		String aux = "";
		
		// Read in the file from the BufferedReader, and append it to the StringBuilder
		while ((aux = reader.readLine()) != null) {
			builder.append(aux);
		}
		connection.disconnect();
		
		// Save the Survey Questions JSON file to the local filesystem
		return builder.toString();
	}
	
	
	/**
	 * Write a string to a file in the local Android Filesystem
	 */
	public static void writeStringToFile(String string, TextFileManager file) {
		file.deleteSafely();
		file.write(string);
	}
	
}
