package org.beiwe.app.survey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.storage.PersistentData;
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
	
	/** @param applicationContext We need a Context in order to run certain logic in this class. */
	public QuestionsDownloader(Context applicationContext) { this.appContext = applicationContext; }
	
	
	public void downloadJsonQuestions() {
		Log.d("QuestionsDownloader", "downloadJSONQuestions() called");
		new GetUpToDateSurveys().execute();
	}


	//TODO: The following is deprecated, replace with PersistentData.getSurveyContent(surveyId);
//	public String getJsonSurveyString(String surveyId) {
//		try {
//			// Try loading the questions.json file from the local filesystem
//			String content = PersistentData.getSurveyContent(surveyId);
//			return getSurveyQuestionsFromFilesystem(type);
//		}
//		//This should probably catch a JSONException, but the auto try-catch also inserts
//		// a catch NullPointerException, which is strange, so we have left it like it is.
//		catch (Exception e1) {
//			/* If the app hasn't downloaded questions.json and saved it to
//			 * the filesystem, return an empty String, which will break the
//			 * JSON parser and display an error message instead of the survey */
//			return "";
//		}
//	}
	
	
	/**Read a file from the server, and return the file as a String 
	 * @throws NotFoundException 
	 * @throws IOException 
	 * @throws JSONException */
	private String getSurveyQuestionsFromServer(String urlString) throws NotFoundException, JSONException {
		String parameters = "";
		String surveyQuestions = PostRequest.httpRequestString( parameters, urlString );
		if (isValidSurveyJson(surveyQuestions)) { return surveyQuestions; }
		else { throw new JSONException("Invalid JSON"); }
	}
	
	//TODO: Eli. Purge.
//	/**Read a file from the local Android filesystem, and return it as a String
//	 * @throws JSONException */
//	private String getSurveyQuestionsFromFilesystem(SurveyType.Type type) throws NullPointerException, JSONException {
//		Log.i("QuestionsDownloader", "Called getSurveyQuestionsFromFilesystem()");
//		
//		String surveyQuestions = getQuestionsFile(type).read();
//
//		if (isValidSurveyJson(surveyQuestions)) { return surveyQuestions; }
//		else { throw new JSONException("Invalid JSON"); }
//	}
	
	
	/**Tells you whether a String is valid JSON
	 * Based on: http://stackoverflow.com/a/10174938
	 * @param
	 * @return true if valid JSON; false otherwise */
	private boolean isValidSurveyJson(String input) {
		try {
			JSONObject wholeSurveyObject = new JSONObject(input);
			String surveyId = wholeSurveyObject.getString("survey_id");
			JSONArray jsonQuestions = wholeSurveyObject.getJSONArray("questions");
			if ((surveyId != null) && (jsonQuestions != null)) {
				return true;
			}
		}
		catch (JSONException e) {
			try { new JSONArray(input); }
			catch (JSONException e2) { return false; }
		}
		return false;
	}
	
	//TODO: Eli. Implement downloading things...  :D
	/**Gets the most up-to-date versions of the surveys, writes them to files,
	 * and schedules repeating notifications for them. Does it on a separate,
	 * non-blocking thread, because it's a slow network request */
	//todo: eli. reimplement.
	class GetUpToDateSurveys extends AsyncTask<String, Integer, Map<String, String>> {

		@Override
		//TODO: Eli.  can probably make this use the Void class, don't really need to return anything.
		protected Map<String, String> doInBackground(String... params) {
			Map<String, String> surveysDict = new HashMap<String, String>();
//			for (SurveyType.Type type : SurveyType.Type.values()) {
//				try {
//					String urlString = appContext.getResources().getString(type.urlResource);
//					surveysDict.put(type.dictKey, getSurveyQuestionsFromServer(urlString));
//				}
//				catch (Exception e) {
//					Log.d("QuestionsDownloader", "getSurveyQuestionsFromServer() failed with exception " + e);
//				}
//			}
			return surveysDict;
		}
		
		@Override
		protected void onPostExecute(Map<String, String> surveysDict) {
			//TODO: Eli. Insert here the creation in persistentdata for new surveys, as well as checks on whether a new survey has come in and handling all of those cases. (yay!)
//			if (surveysDict != null && !surveysDict.isEmpty()) {
//				for (SurveyType.Type type : SurveyType.Type.values()) {
//					String survey = surveysDict.get(type.dictKey);
//					if (survey != null) { writeSurveyToFile(survey, getQuestionsFile(type)); }
//				}
//			}
		}
	}
//	
//	/** Writes a survey to a file and schedules the survey to run.
//	 * @param survey survey data.
//	 * @param file the TextFileManager instance to write data to. */
//	private void writeSurveyToFile(String survey, TextFileManager file) {
//		Log.d("QuestionsDownloader.java", "writeSurveyToFile() called on " + file.name);
//		if (survey != null) {
//			file.deleteSafely();
//			file.writePlaintext(survey);
//			SurveyScheduler.scheduleSurvey(survey);
//		}		
//	}
	
	//TODO: Eli. Purge
//	/** Return the correct TextFileManager.surveyQuestionsFile JSON file from
//	 * the filesystem.
//	 * This was a part of the SurveyType enum, but that caused TextFileManager
//	 * to try creating files before it had been started, which caused an NPE
//	 * crash. 
//	 * @param type the SurveyType (DAILY or WEEKLY)
//	 * @return the correct (daily or weekly) surveyQuestionsFile */
//	private TextFileManager getQuestionsFile(SurveyType.Type type) {
//		switch (type) {
//			case DAILY:
//				return TextFileManager.getCurrentDailyQuestionsFile();				
//			case WEEKLY:
//				return TextFileManager.getCurrentWeeklyQuestionsFile();
//			default:
//				Log.e("QuestionsDownloader.java", "getQuestionsFile() found no matches");
//				return null;
//		}
//	}
}
