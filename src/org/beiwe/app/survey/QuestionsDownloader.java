package org.beiwe.app.survey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;

import org.beiwe.app.R;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

public class QuestionsDownloader {

	private Context appContext;
	
	public QuestionsDownloader(Context applicationContext) {
		this.appContext = applicationContext; 
	}
	
	
	public String getJsonSurveyString() {
		
		Log.i("QuestionsDownloader", "Called getJsonSurveyString()");
		
		try {
			return getSurveyQuestionsFromServer();
		}catch (Exception e) {
			e.printStackTrace();
			return getSurveyQuestionsFromAppResources();
		}
		
		// TODO: Try getting from the local filesystem
		
	}
	
	
	/**
	 * Returns as a String the JSON survey file that's hard-coded into res/raw/
	 * @return
	 */
	private String getSurveyQuestionsFromAppResources() {
		Log.i("QuestionsDownloader", "Called getSurveyQuestionsFromAppResources()");
		
		InputStream inputStream = 
				appContext.getResources().openRawResource(R.raw.sample_survey);
		return fileToString(inputStream);
	}
	
	
	/**
	 * Read a file from the server, and return the file as a String 
	 * @throws NotFoundException 
	 * @throws IOException 
	 */
	private String getSurveyQuestionsFromServer() throws NotFoundException, IOException {
		Log.i("QuestionsDownloader", "Called getSurveyQuestionsFromServer()");
		
		URL locationsFileURL;
		locationsFileURL = new URL(appContext.getResources().getString(R.string.survey_questions_url));
		BufferedReader reader = new BufferedReader(new InputStreamReader(locationsFileURL.openStream()));
		
		// Based on code from http://stackoverflow.com/a/4666766
		StringBuilder builder = new StringBuilder();
		String aux = "";
		
		while ((aux = reader.readLine()) != null) {
			builder.append(aux);
		}
		
		return builder.toString();
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
	/*private void updateSurveyQuestions() throws Exception {
	    // Run the HTTP GET on a separate, non-blocking thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<HttpGet> thread = new Callable<HttpGet>() {
			@Override
			public HttpGet call() throws Exception {
				// Copy the survey questions from the server
				getSurveyQuestionsFromServer();
				return null;
			}
		};
		executor.submit(thread);
	}*/
	
}
