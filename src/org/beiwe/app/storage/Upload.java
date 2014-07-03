package org.beiwe.app.storage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.HttpGet;
import org.beiwe.app.R;

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
				String[] files = CSVFileManager.getAllFilesSafely();
				
				//for (String fileName : files) {
					try {
						tryToUploadFile("");
					}
					catch (Exception e) {
						//Log.w("Upload", "File " + fileName + " didn't exist");
						e.printStackTrace();
					}
				//}
				
				return null;
			}
		};
		executor.submit(thread);
	}
	
	
	private void tryToUploadFile(String filename) {
		try {
			// TODO: get Eli to provide a function that grabs a File, given the file name
			// This is uploading the dummy audio recording file, just for testing
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp");
			//File file = new File(appContext.getFilesDir() + fileName);
			//File file = new File(fileName);

			uploadFile(file, getUploadUrl(filename));
		} catch (IOException e) {
			Log.w("Upload", "Failed to upload file. Raised exception " + e.getCause());
			e.printStackTrace();
		}
	}
	
	
	private String getUploadUrl(String filename) {
		String url = appContext.getResources().getString(R.string.data_upload_base_url);
		return url + "upload_gps/";
		/* Upload URLs:
		 * http://54.204.178.17/upload_gps/
		 * http://54.204.178.17/upload_accel/
		 * http://54.204.178.17/upload_powerstate/
		 * http://54.204.178.17/upload_calls/
		 * http://54.204.178.17/upload_texts/
		 * http://54.204.178.17/upload_surveyresposne/
		 * http://54.204.178.17/upload_audio/
		 */		
	}
	
	
	private void uploadFile(File file, String uploadUrl) throws IOException {
		// Based on: http://www.codejava.net/java-ee/servlet/upload-file-to-servlet-without-using-html-form
		
		URL url = new URL(uploadUrl);
		//URL url = new URL("http://joshzagorsky.com/foobar");
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true);
		httpConn.setRequestMethod("POST");
		httpConn.setRequestProperty("fileName", file.getName());
		
		OutputStream outputStream = httpConn.getOutputStream();
		FileInputStream inputStream = new FileInputStream(file);
		
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		
		outputStream.close();
		inputStream.close();
		
		// Process HTTP Response
		int responseCode = httpConn.getResponseCode();
		Log.i("Upload", "HTTP response code = " + responseCode);
		//if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String response = reader.readLine();
			Log.i("Upload", "HTTP Response = " + response);
		//}
	}
	
	
	/**reads in the entire file, returns a string.
	 * @param fileName a file name
	 * @return contents of file 
	 * @throws IOException */
	private String readTextFile(String fileName) throws IOException{
		
		BufferedInputStream bufferedInputStream;// BufferedInputStream( new FileInputStream(null));
		StringBuffer inputStringBuffer = new StringBuffer();
		int data;
		try {
			bufferedInputStream = new BufferedInputStream( appContext.openFileInput(fileName) );

			try{ while( (data = bufferedInputStream.read()) != -1)
				inputStringBuffer.append((char)data); }
			catch (IOException e) {
				Log.i("Upload", "read error in " + fileName);
				e.printStackTrace(); }
			bufferedInputStream.close();
		}
		catch (FileNotFoundException e) {
			Log.i("Upload", "file " + fileName + " does not exist");
			e.printStackTrace(); }
		return inputStringBuffer.toString();
	}

	
private String readDataFile(String fileName) throws IOException{
		
		DataInputStream dataInputStream;// BufferedInputStream( new FileInputStream(null));
		StringBuffer inputStringBuffer = new StringBuffer();
		
		try {			
			String filePath = appContext.getFilesDir() + "/" + fileName;
			File file = new File(filePath);
			dataInputStream = new DataInputStream( new FileInputStream(file) );	
			//we need a byte array of exactly the correct length
			byte[] fileData = new byte[(int) file.length()];

			try{ dataInputStream.readFully(fileData); }
			catch (IOException e) {
				Log.i("Upload", "read error in " + fileName);
				e.printStackTrace(); }
			
			dataInputStream.close();
		}
		catch (FileNotFoundException e) {
			Log.i("Upload", "file " + fileName + " does not exist");
			e.printStackTrace(); }
		return inputStringBuffer.toString();
		
	}

	
}
