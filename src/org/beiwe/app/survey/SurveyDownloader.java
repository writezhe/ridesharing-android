package org.beiwe.app.survey;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.JSONUtils;
import org.beiwe.app.R;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.networking.PostRequest;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.PersistentData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class SurveyDownloader {
	
	public static void downloadSurveys( Context context ) {
		Log.d("QuestionsDownloader", "downloadJSONQuestions() called");
		doDownload( context.getResources().getString(R.string.download_surveys_url) );		
	}

	private static void doDownload(final String url) { new HTTPAsync(url) {
		@Override
		protected Void doInBackground(Void... arg0) {
			//FIXME: FIND THE CORRECT DOWNLOAD URL AND RETRIEVE IT FROM STRINGS.XML
			String parameters = "";
			responseString = PostRequest.httpRequestString( parameters, url);
			return null; //hate
		}
		@Override
		protected void onPostExecute(Void arg) {
			super.onPostExecute(arg);
			updateSurveys( responseString );
		} }.execute();
	}

	private static void updateSurveys(String jsonString){
		List<String> surveys;
		try { surveys = JSONUtils.jsonArrayToStringList( new JSONArray(jsonString) );}
		catch (JSONException e) { throw new NullPointerException("JSON PARSING FAIL FAIL FAIL"); }
		
		JSONObject surveyJSON;
		List<String> oldSurveyIds = PersistentData.getSurveyIds();
		ArrayList<String> newSurveyIds = new ArrayList<String>();
		String surveyId;
		String surveyType;
		String jsonQuestionsString;
		String jsonTimingsString;
		
		for (String surveyString : surveys){
			try { surveyJSON = new JSONObject(surveyString); }
			catch (JSONException e) { throw new NullPointerException("JSON fail 1"); }
//			Log.d("debugging survey update", "whole thing: " + surveyJSON.toString());
			
			try { surveyId = surveyJSON.getString("_id"); }
			catch (JSONException e) { throw new NullPointerException("JSON fail 2"); }
//			Log.d("debugging survey update", "id: " + surveyId.toString());
			
			try { surveyType = surveyJSON.getString("survey_type"); }
			catch (JSONException e) { throw new NullPointerException("JSON fail 2.5"); }
//			Log.d("debugging survey update", "type: " + surveyType.toString());
			
			try { jsonQuestionsString = surveyJSON.getString("content"); }
			catch (JSONException e) {throw new NullPointerException("JSON fail 3"); }
//			Log.d("debugging survey update", "questions: " + jsonQuestionsString);
			
			try { jsonTimingsString = surveyJSON.getString("timings"); }
			catch (JSONException e) {throw new NullPointerException("JSON fail 4"); }
//			Log.d("debugging survey update", "timings: " + jsonTimingsString);
			
			if ( oldSurveyIds.contains(surveyId) ) { //if surveyId already exists, check for changes, add to list of new survey ids.
				Log.e("debugging survey update", "checking for changes");
				Log.w("debugging survey update", "setting content to: " + jsonQuestionsString);
				PersistentData.setSurveyContent(surveyId, jsonQuestionsString);
				PersistentData.setSurveyType(surveyId, surveyType);				
				if (PersistentData.getSurveyTimes(surveyId) != jsonTimingsString) {
					SurveyScheduler.scheduleSurvey(surveyId);
				}
				newSurveyIds.add(surveyId);
			}
			else { //if survey is new, create new survey entry.
				Log.e("debugging survey update", "CREATE A SURVEY");
				PersistentData.addSurveyId(surveyId);
				PersistentData.createSurveyData(surveyId, jsonQuestionsString, jsonTimingsString, surveyType);
			}
		}
		
		for (String oldSurveyId : oldSurveyIds){ //for each old survey id
			if ( !newSurveyIds.contains( oldSurveyId ) ) { //check if it is still a valid survey (it the list of new survey ids.)
				//	TODO: Eli. unschedule old survey alarm, delete
				throw new NullPointerException("surveys currently scheduled need to be unscheduled");
			}
		}
	}
}
