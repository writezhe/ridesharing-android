package com.zagaran.scrubs.survey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.zagaran.scrubs.R;

public class JsonParser {

	private Context appContext;
	private SurveyQuestionRenderer renderer;
	
	public JsonParser(Context applicationContext) {
		appContext = applicationContext;
		renderer = new SurveyQuestionRenderer(applicationContext);
	}
	
	
	public void tryToRenderSurveyFromJSON() {
		try {
			renderSurveyFromJSON();
		} catch (Exception e) {
			Log.i("JsonParser", "Failed to parse JSON properly");
			e.printStackTrace();
		}
	}
	
	
	private void renderSurveyFromJSON() throws Exception {
		InputStream inputStream = appContext.getResources().openRawResource(R.raw.sample_survey);
		JSONObject wholeSurveyObject = new JSONObject(fileToString(inputStream));
		
		JSONArray jsonQuestions = wholeSurveyObject.getJSONArray("questions");
		for (int i = 0; i < jsonQuestions.length(); i++) {
			renderQuestionFromJSON(jsonQuestions.getJSONObject(i));
		}		
	}
	
	
	private void renderQuestionFromJSON(JSONObject jsonQuestion) {
		String questionType = getStringFromJSONObject(jsonQuestion, "question_type");

		if (questionType.equals("info_text_box")) {
			renderInfoTextBox(jsonQuestion);
		}
		else if (questionType.equals("slider")) {
			renderSliderQuestion(jsonQuestion);
		}
		else if (questionType.equals("radio_button")) {
			renderRadioButtonQuestion(jsonQuestion);
		}
		else if (questionType.equals("checkbox")) {
			renderCheckboxQuestion(jsonQuestion);
		}
		else if (questionType.equals("free_response")) {
			renderFreeResponseQuestion(jsonQuestion);
		}
	}
	
	
	private void renderInfoTextBox(JSONObject jsonQuestion) {
		String infoText = getStringFromJSONObject(jsonQuestion, "question_text");
		renderer.createInfoTextbox(infoText);
	}
	
	
	private void renderSliderQuestion(JSONObject jsonQuestion) {
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		int numberOfValues = getIntFromJSONObject(jsonQuestion, "number_of_values");
		int defaultValue = getIntFromJSONObject(jsonQuestion, "default_value");
		renderer.createSliderQuestion(questionText, numberOfValues, defaultValue);
	}
	
	
	private void renderRadioButtonQuestion(JSONObject jsonQuestion) {
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		String[] answers = getStringArrayFromJSONObject(jsonQuestion, "answers");
		renderer.createRadioButtonQuestion(questionText, answers);
	}
	
	
	private void renderCheckboxQuestion(JSONObject jsonQuestion) {
		
	}
	
	
	private void renderFreeResponseQuestion(JSONObject jsonQuestion) {
		
	}

	
	// Get a JSONArray of survey questions from a file in the local filesystem or, if there is 
	// no such file in the filesystem, from a file in the res/ directory that's part of the app download
	/*private JSONArray getJSONArray() throws Exception {
		try {
			// Try loading the locations list from a file in the local system
			String filePath = context.getFilesDir() + 
					context.getResources().getString(R.string.locations_file_local_address);
			FileInputStream inputStream = new FileInputStream(new File(filePath));
			return new JSONArray(fileToString(inputStream));
		} catch (Exception e) { 
			// If the local file isn't there or can't be parsed, use the JSON file in res/
			InputStream inputStream = appContext.getResources().openRawResource(R.raw.sample_survey);
			return new JSONArray(fileToString(inputStream));
		}
	}*/

	
	// Read a file (really an InputStream) and return a string
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

	
	// Get a String from a JSONObject key, and return an empty String instead of throwing a JSONException
	private String getStringFromJSONObject(JSONObject obj, String key) {
		try {
			return obj.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}
	
	
	// Get an int from a JSONObject key, and return -1 instead of throwing a JSONException
	private int getIntFromJSONObject(JSONObject obj, String key) {
		try {
			return obj.getInt(key);
		} catch (JSONException e) {
			return -1;
		}
	}
	
	
	// Get an String array from a JSONObject key, and return an empty array instead of throwing a JSONException
	private String[] getStringArrayFromJSONObject(JSONObject obj, String key) {
		JSONArray jsonArray;
		try {
			jsonArray = obj.getJSONArray(key);
			String[] strings = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					strings[i] = jsonArray.getString(i);
				} catch (JSONException e) {
					strings[i] = "";
				}
			}
			return strings;
		}
		catch (JSONException e1) {
			String[] errorArray = {""};
			return errorArray;
		}		
	}

}
