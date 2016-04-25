package org.beiwe.app.survey;

import org.beiwe.app.CrashHandler;
import org.beiwe.app.JSONUtils;
import org.beiwe.app.R;
import org.beiwe.app.ui.TextFieldType;
import org.beiwe.app.ui.TextFieldType.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

//most of the errors in here should be caught by a sufficiently vigilant researcher, so will not be crashhandled

public class JsonParser {

	private Context appContext;
	private QuestionRenderer renderer;
	private View errorWidget;


	// Constructor exists to set up class variables
	public JsonParser(Context applicationContext) {
		appContext = applicationContext;
		renderer = new QuestionRenderer(applicationContext);
		// Create an error message widget that displays by default
		LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		errorWidget = inflater.inflate(R.layout.survey_info_textbox, null);
	}
	
	
	/**Add all survey questions to the provided surveyLayout View object
	 * @param surveyLayout */
	public void renderSurveyFromJSON(LinearLayout surveyLayout, String jsonSurveyString, String surveyId, Boolean randomize, int numberQuestions, Boolean randomizeWithMemory) {
		LinearLayout questionsLayout = (LinearLayout) surveyLayout.findViewById(R.id.surveyQuestionsLayout);
		try { //Every single line here can throw a JSONException
			JSONArray jsonQuestions = new JSONArray(jsonSurveyString);
			// Iterate over the array, and add each question to the survey View
			if (randomize && !randomizeWithMemory) { jsonQuestions = JSONUtils.shuffleJSONArray(jsonQuestions, numberQuestions); }
			if (randomize && randomizeWithMemory) { jsonQuestions = JSONUtils.shuffleJSONArrayWithMemory(jsonQuestions, numberQuestions, surveyId); }
			for (int i = 0; i < jsonQuestions.length(); i++) {
				View question = renderQuestionFromJSON(jsonQuestions.getJSONObject(i));
				questionsLayout.addView(question);
			}
		}
		catch (JSONException e) {
			// If rendering or parsing failed, display the error widget instead
			Log.i("JsonParser", "Failed to parse JSON survey properly");
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext);
			surveyLayout.removeAllViews();
			surveyLayout.addView(errorWidget);
		}
	}


	/**Create/render a single survey question
	 * @param jsonQuestion JSON representation of a question
	 * @return a survey question that's a View object */
	private View renderQuestionFromJSON(JSONObject jsonQuestion) {
		String questionType = getStringFromJSONObject(jsonQuestion, "question_type");
		if (questionType.equals("info_text_box")) { return renderInfoTextBox(jsonQuestion); }
		else if (questionType.equals("slider")) { return renderSliderQuestion(jsonQuestion); }
		else if (questionType.equals("radio_button")) { return renderRadioButtonQuestion(jsonQuestion); }
		else if (questionType.equals("checkbox")) { return renderCheckboxQuestion(jsonQuestion); }
		else if (questionType.equals("free_response")) { return renderFreeResponseQuestion(jsonQuestion); }
		return errorWidget;
	}


	// Gets and cleans the parameters necessary to create an Info Text Box
	private View renderInfoTextBox(JSONObject jsonQuestion) {
		String questionID = getStringFromJSONObject(jsonQuestion, "question_id");
		String infoText = getStringFromJSONObject(jsonQuestion, "question_text");
		return renderer.createInfoTextbox(questionID, infoText);
	}


	// Gets and cleans the parameters necessary to create a Slider Question
	private View renderSliderQuestion(JSONObject jsonQuestion) {
		String questionID = getStringFromJSONObject(jsonQuestion, "question_id");
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		int min = getIntFromJSONObject(jsonQuestion, "min");
		int max = getIntFromJSONObject(jsonQuestion, "max");
		return renderer.createSliderQuestion(questionID, questionText, min, max);
	}


	// Gets and cleans the parameters necessary to create a Radio Button Question
	private View renderRadioButtonQuestion(JSONObject jsonQuestion) {
		String questionID = getStringFromJSONObject(jsonQuestion, "question_id");
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		String[] answers = getStringArrayFromJSONObject(jsonQuestion, "answers");
		return renderer.createRadioButtonQuestion(questionID, questionText, answers);
	}


	// Gets and cleans the parameters necessary to create a Checkbox Question
	private View renderCheckboxQuestion(JSONObject jsonQuestion) {
		String questionID = getStringFromJSONObject(jsonQuestion, "question_id");
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		String[] options = getStringArrayFromJSONObject(jsonQuestion, "answers");
		return renderer.createCheckboxQuestion(questionID, questionText, options);
	}


	// Gets and cleans the parameters necessary to create a Free-Response Question
	private View renderFreeResponseQuestion(JSONObject jsonQuestion) {
		String questionID = getStringFromJSONObject(jsonQuestion, "question_id");
		String questionText = getStringFromJSONObject(jsonQuestion, "question_text");
		TextFieldType.Type textFieldType = getTextFieldTypeFromJSONObject(jsonQuestion, "text_field_type");
		return renderer.createFreeResponseQuestion(questionID, questionText, textFieldType);
	}


	/**Get a String from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return an empty String instead of throwing a JSONException */
	private String getStringFromJSONObject(JSONObject obj, String key) {
		try { return obj.getString(key); }
		catch (JSONException e) { return ""; }
	}


	/**Get an int from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return -1 instead of throwing a JSONException */
	private int getIntFromJSONObject(JSONObject obj, String key) {
		try { return obj.getInt(key); }
		catch (JSONException e) { return -1; }
	}


	/**Get an array of Strings from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return a one-String array instead of throwing a JSONException */
	private String[] getStringArrayFromJSONObject(JSONObject obj, String key) {
		JSONArray jsonArray;
		try { jsonArray = obj.getJSONArray(key); }
		catch (JSONException e1) {
			String[] errorArray = {""};
			return errorArray;
		}
		String[] strings = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			try { strings[i] = jsonArray.getJSONObject(i).getString("text"); }
			catch (JSONException e) { strings[i] = ""; }
		}
		return strings;
	}


	/**Get an Enum TextFieldType from a JSONObject key
	 * @param obj a generic JSONObject
	 * @param key the JSON key
	 * @return return SINGLE_LINE_TEXT as the default instead of throwing a JSONException */
	private TextFieldType.Type getTextFieldTypeFromJSONObject(JSONObject obj, String key) {
		try { return Type.valueOf(obj.getString(key)); }
		catch (JSONException e) { return Type.SINGLE_LINE_TEXT; }
	}
}
