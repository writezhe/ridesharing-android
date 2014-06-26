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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zagaran.scrubs.R;
import com.zagaran.scrubs.survey.SurveyTextFieldType.Type;

public class JsonParser {

	private Context appContext;
	private SurveyQuestionRenderer renderer;
	private View errorWidget;
	
	
	// Constructor exists to set up class variables
	public JsonParser(Context applicationContext) {
		appContext = applicationContext;
		renderer = new SurveyQuestionRenderer(applicationContext);
		
		// Create an error message widget that displays by default
		LayoutInflater inflater = 
				(LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		errorWidget = inflater.inflate(R.layout.survey_info_textbox, null);
	}
	
	
	/**
	 * Add all survey questions to the provided surveyLayout View object
	 * @param surveyLayout
	 */
	public void renderSurveyFromJSON(LinearLayout surveyLayout) {
		try {
			// Get the JSON array of questions
			InputStream inputStream = 
					appContext.getResources().openRawResource(R.raw.sample_survey);
			JSONObject wholeSurveyObject;
			wholeSurveyObject = new JSONObject(fileToString(inputStream));
			JSONArray jsonQuestions = wholeSurveyObject.getJSONArray("questions");
			
			// Iterate over the array, and add each question to the survey View
			for (int i = 0; i < jsonQuestions.length(); i++) {
				View question = renderQuestionFromJSON(jsonQuestions.getJSONObject(i));
				surveyLayout.addView(question);
			}
		}
		catch (JSONException e) {
			// If rendering or parsing failed, display the error widget instead
			Log.i("JsonParser", "Failed to parse JSON properly");
			e.printStackTrace();
			surveyLayout.addView(errorWidget);
		}
	}
	
	
	/**
	 * Create/render a single survey question
	 * @param jsonQuestion JSON representation of a question
	 * @return a survey question that's a View object
	 */
	private View renderQuestionFromJSON(JSONObject jsonQuestion) {
		
		String questionType = getStringFromJSONObject(jsonQuestion, "question_type");
		
		if (questionType.equals("info_text_box")) {
			return renderInfoTextBox(jsonQuestion);
		}
		else if (questionType.equals("slider")) {
			return renderSliderQuestion(jsonQuestion);
		}
		else if (questionType.equals("radio_button")) {
			return renderRadioButtonQuestion(jsonQuestion);
		}
		else if (questionType.equals("checkbox")) {
			return renderCheckboxQuestion(jsonQuestion);
		}
		else if (questionType.equals("free_response")) {
			return renderFreeResponseQuestion(jsonQuestion);
		}
		else {
			return errorWidget;
		}
	}
	
	
	// Gets and cleans the parameters necessary to create an Info Text Box
	private View renderInfoTextBox(JSONObject jsonQuestion) {
		String infoText = getStringFromJSONObject(jsonQuestion, "question_text");
		return renderer.createInfoTextbox(infoText);
	}
	
	
	// Gets and cleans the parameters necessary to create a Slider Question
	private View renderSliderQuestion(JSONObject jsonQuestion) {
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		int numberOfValues = getIntFromJSONObject(jsonQuestion, "number_of_values");
		int defaultValue = getIntFromJSONObject(jsonQuestion, "default_value");
		return renderer.createSliderQuestion(questionText, numberOfValues, defaultValue);
	}
	
	
	// Gets and cleans the parameters necessary to create a Radio Button Question
	private View renderRadioButtonQuestion(JSONObject jsonQuestion) {
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		String[] answers = getStringArrayFromJSONObject(jsonQuestion, "answers");
		return renderer.createRadioButtonQuestion(questionText, answers);
	}
	
	
	// Gets and cleans the parameters necessary to create a Checkbox Question
	private View renderCheckboxQuestion(JSONObject jsonQuestion) {
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		String[] options = getStringArrayFromJSONObject(jsonQuestion, "answers");
		return renderer.createCheckboxQuestion(questionText, options);
	}
	
	
	// Gets and cleans the parameters necessary to create a Free-Response Question
	private View renderFreeResponseQuestion(JSONObject jsonQuestion) {
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		SurveyTextFieldType.Type textFieldType = 
				getTextFieldTypeFromJSONObject(jsonQuestion, "text_field_type");
		return renderer.createFreeResponseQuestion(questionText, textFieldType);
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
	 * Get a String from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return an empty String instead of throwing a JSONException
	 */
	private String getStringFromJSONObject(JSONObject obj, String key) {
		try {
			return obj.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}
	
	
	/**
	 * Get an int from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return -1 instead of throwing a JSONException
	 */
	private int getIntFromJSONObject(JSONObject obj, String key) {
		try {
			return obj.getInt(key);
		} catch (JSONException e) {
			return -1;
		}
	}
	
	
	/**
	 * Get an array of Strings from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return a one-String array instead of throwing a JSONException
	 */
	private String[] getStringArrayFromJSONObject(JSONObject obj, String key) {
		JSONArray jsonArray;
		try {
			jsonArray = obj.getJSONArray(key);
			String[] strings = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					strings[i] = jsonArray.getJSONObject(i).getString("text");
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
	
	
	/**
	 * Get an Enum TextFieldType from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return SINGLE_LINE_TEXT as the default instead of throwing a JSONException
	 */
	private SurveyTextFieldType.Type getTextFieldTypeFromJSONObject(JSONObject obj, String key) {
		try {
			return Type.valueOf(obj.getString(key));
		} catch (JSONException e) {
			return Type.SINGLE_LINE_TEXT;
		}
	}

}
