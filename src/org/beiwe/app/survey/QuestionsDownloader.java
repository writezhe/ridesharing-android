package org.beiwe.app.survey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.storage.TextFileManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class QuestionsDownloader {

	private Context appContext;
	
	public QuestionsDownloader(Context applicationContext) {
		this.appContext = applicationContext;
	}
	
	
	public void downloadJsonQuestions() {
		new GetUpToDateSurveys().execute();
	}


	public String getJsonSurveyString(SurveyType.Type type) {
		try {
			// Try loading the questions.json file from the local filesystem
			return getSurveyQuestionsFromFilesystem(type);
		}
		catch (Exception e1) {
			/* If the app hasn't downloaded questions.json and saved it to
			 * the filesystem, return an empty String, which will break the
			 * JSON parser and display an error message instead of the survey */
			return "";
		}
	}
	
	
	/**
	 * Read a file from the server, and return the file as a String 
	 * @throws NotFoundException 
	 * @throws IOException 
	 * @throws JSONException 
	 */
	private String getSurveyQuestionsFromServer(String urlString) throws NotFoundException, JSONException {
		String parameters = "";
		String surveyQuestions = PostRequest.httpRequestString( parameters, urlString );
		if (isValidJson(surveyQuestions)) {
			return surveyQuestions;
		}
		else {
			throw new JSONException("Invalid JSON");
		}
	}
	
	
	/**
	 * Read a file from the local Android filesystem, and return it as a String
	 * @throws JSONException 
	 */
	private String getSurveyQuestionsFromFilesystem(SurveyType.Type type) throws NullPointerException, JSONException {
		Log.i("QuestionsDownloader", "Called getSurveyQuestionsFromFilesystem()");
		
		String surveyQuestions = getQuestionsFile(type).read();

		if (isValidJson(surveyQuestions)) {
			return surveyQuestions;
		}
		else {
			throw new JSONException("Invalid JSON");
		}
	}
	
	
	/**
	 * Tells you whether a String is valid JSON
	 * Based on: http://stackoverflow.com/a/10174938
	 * @param
	 * @return true if valid JSON; false otherwise
	 */
	private boolean isValidJson(String input) {
		try {
			new JSONObject(input);
		}
		catch (JSONException e) {
			try {
				new JSONArray(input);
			}
			catch (JSONException e2) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Gets the most up-to-date versions of the surveys, writes them to files,
	 * and schedules repeating notifications for them. Does it on a separate,
	 * non-blocking thread, because it's a slow network request
	 */
	class GetUpToDateSurveys extends AsyncTask<String, Integer, Map<String, String>> {

		@Override
		protected Map<String, String> doInBackground(String... params) {
			try {
				Map<String, String> surveysDict = new HashMap<String, String>();
				for (SurveyType.Type type : SurveyType.Type.values()) {
					String urlString = appContext.getResources().getString(type.urlResource);
					surveysDict.put(type.dictKey, getSurveyQuestionsFromServer(urlString));
				}
				return surveysDict;
			}
			catch (Exception e) {
				Log.i("QuestionsDownloader", "getSurveyQuestionsFromServer() failed with exception " + e);
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Map<String, String> surveysDict) {
			if (surveysDict != null && !surveysDict.isEmpty()) {
				for (SurveyType.Type type : SurveyType.Type.values()) {
					writeSurveyToFile(surveysDict.get(type.dictKey), getQuestionsFile(type));
				}
			}
		}
	}
	
	
	private void writeSurveyToFile(String survey, TextFileManager file) {
		Log.d("QuestionsDownloader.java", "writeSurveyToFile() called on " + file.name);
		if (survey != null) {
			file.deleteSafely();
			file.write_plaintext(survey);
			SurveyScheduler.scheduleSurvey(survey);
		}		
	}
	
	
	/** Return the correct TextFileManager.surveyQuestionsFile JSON file from
	 * the filesystem.
	 * This was a part of the SurveyType enum, but that caused TextFileManager
	 * to try creating files before it had been started, which caused an NPE
	 * crash. 
	 * @param type the SurveyType (DAILY or WEEKLY)
	 * @return the correct (daily or weekly) surveyQuestionsFile */
	private TextFileManager getQuestionsFile(SurveyType.Type type) {
		switch (type) {
			case DAILY:
				return TextFileManager.getCurrentDailyQuestionsFile();				
			case WEEKLY:
				return TextFileManager.getCurrentWeeklyQuestionsFile();
			default:
				Log.e("QuestionsDownloader.java", "getQuestionsFile() found no matches");
				return null;
		}
	}

}
