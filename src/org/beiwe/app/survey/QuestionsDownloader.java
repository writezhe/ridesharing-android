package org.beiwe.app.survey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.HttpGet;
import org.beiwe.app.R;

import android.content.Context;

public class QuestionsDownloader {

	private Context appContext;
	
	public QuestionsDownloader(Context applicationContext) {
		this.appContext = applicationContext; 
	}
	
	
	public String getJsonSurveyString() {
		// Get the JSON array of questions
		InputStream inputStream = 
				appContext.getResources().openRawResource(R.raw.sample_survey);
		return fileToString(inputStream);
	}
	

	/**
	 * Read a file (really an InputStream) and return a string
	 * @param inputStream the file you want to read
	 * @return String that contains the contents of the file
	 */
	private String fileToString(InputStream inputStream) {
		try {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
		    Reader reader;
			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		    int n;
		    while ((n = reader.read(buffer)) != -1) {
		        writer.write(buffer, 0, n);
		    }
			inputStream.close();
			return writer.toString();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
		catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	
	/**
	 * Get the current survey questions from the server as a JSON file
	 * @throws Exception
	 */
	private void updateLocations() throws Exception {
	    // Run the HTTP GET on a separate, non-blocking thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<HttpGet> thread = new Callable<HttpGet>() {
			@Override
			public HttpGet call() throws Exception {
				// Copy the survey questions from the server
				copyFileFromServerToLocal();
				return null;
			}
		};
		executor.submit(thread);
	}

	
	/**
	 * Read a JSON file from the server, and copy it to a file in the local Android filesystem
	 * @throws Exception
	 */
	private void copyFileFromServerToLocal() throws Exception {
		URL locationsFileURL;
		locationsFileURL = new URL(appContext.getResources().getString(R.string.survey_questions_url));
		BufferedReader reader = new BufferedReader(new InputStreamReader(locationsFileURL.openStream()));
		
		// TODO: reconcile this with Eli's filesystem manager
		String filePath = appContext.getFilesDir() + "/survey1.json";
		File writeFile = new File(filePath);
 		BufferedWriter writer = new BufferedWriter(new FileWriter(writeFile));
		
		String inputLine;
		while ((inputLine = reader.readLine()) != null) {
			writer.write(inputLine);
		}
		
		reader.close();
		writer.close();		
	}

}
