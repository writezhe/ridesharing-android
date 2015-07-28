package org.beiwe.app.survey;

import java.util.List;

import org.beiwe.app.JSONUtils;
import org.beiwe.app.networking.HTTPAsync;
import org.beiwe.app.networking.PostRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class QuestionsDownloader {
	
	public void downloadJsonQuestions() {
		Log.d("QuestionsDownloader", "downloadJSONQuestions() called");
		doDownload("INSERT URL HERE");
	}

	private void doDownload(final String url) { new HTTPAsync(url) {
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
		
		JSONObject surveyJSONObject;
		String surveyId;
		JSONArray jsonQuestions;
		
		for (String survey : surveys){
			//it is possible that the survey is a JSONArray, in which case this needs to be rewritten.
			try { surveyJSONObject = new JSONObject(survey); }
			catch (JSONException e) { throw new NullPointerException("JSON fail 1"); }
			try { surveyId = surveyJSONObject.getString("survey_id"); }
			catch (JSONException e) { throw new NullPointerException("JSON fail 2"); }
			try { jsonQuestions = surveyJSONObject.getJSONArray("questions"); }
			catch (JSONException e) { throw new NullPointerException("JSON fail 3"); }
		}
		//TODO: Eli. finish implementing json parsing...
	}
}
