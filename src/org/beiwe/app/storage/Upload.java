package org.beiwe.app.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Upload {
	
	private Context appContext;
	
	public Upload(Context applicationContext) {
		this.appContext = applicationContext;
	}
	
	
	public void uploadAllFiles() {
	    // Run the HTTP GET on a separate, non-blocking thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<HttpGet> thread = new Callable<HttpGet>() {
			@Override
			public HttpGet call() {
				String[] files = CSVFileManager.getAllFiles();
				
				for (String fileName : files) {
					try {
						tryToUploadFile(fileName);
					}
					catch (Exception e) {
						Log.w("Upload", "File " + fileName + " didn't exist");
					}
				}
				
				return null;
			}
		};
		executor.submit(thread);
	}
	
	
	private void tryToUploadFile(String filename) {
		try {
			uploadFile(filename);
		} catch (IOException e) {
			Log.w("Upload", "Failed to upload file. Raised exception " + e.getCause());
			e.printStackTrace();
		}
	}
	
	
	private void uploadFile(String fileName) throws IOException {
		// Based on: http://www.codejava.net/java-ee/servlet/upload-file-to-servlet-without-using-html-form
		
		// TODO: get Eli to provide a function that grabs a File, given the file name
		// This is uploading the dummy audio recording file, just for testing
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp");
		//File file = new File(appContext.getFilesDir() + fileName);
		//File file = new File(fileName);
		Log.i("Upload", "Attempting to upload file " + fileName);
		
		URL url = new URL("http://beiwe.org/upload_gps/");
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true);
		httpConn.setRequestMethod("POST");
		httpConn.setRequestProperty("fileName", file.getName());
		
		OutputStream outputStream = httpConn.getOutputStream();
		FileInputStream inputStream = new FileInputStream(file);
		
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		
		Log.i("Upload", "Start writing data");
		
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		
		Log.i("Upload", "Finished writing data");
		outputStream.close();
		inputStream.close();
		
		int responseCode = httpConn.getResponseCode();
		Log.i("Upload", "HTTP response code = " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String response = reader.readLine();
			Log.i("Upload", "HTTP Response = " + response);
		}
	}

	/* Upload URLS:
	 * http://54.204.178.17/upload_gps/
	 * http://54.204.178.17/upload_accel/
	 * http://54.204.178.17/upload_powerstate/
	 * http://54.204.178.17/upload_calls/
	 * http://54.204.178.17/upload_texts/
	 * http://54.204.178.17/upload_surveyresposne/
	 * http://54.204.178.17/upload_audio/
	 */
}
