package com.zagaran.scrubs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SurveyActivity extends Activity {

	// TODO: when you rotate the screen, the EditTexts get wiped clear, and some of the sliders jump to 100%. Debug this.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyLayout);
		
		surveyLayout.addView(createFreeResponseQuestion("How many eggs did you eat this morning?", TextFieldType.NUMERIC));

		surveyLayout.addView(createFreeResponseQuestion("What is your nickname, moniker, or nom de guerre?", TextFieldType.SINGLE_LINE_TEXT));
		
		surveyLayout.addView(createFreeResponseQuestion("Please enter any suggestions you have about this whole process.", TextFieldType.MULTI_LINE_TEXT));
		
		surveyLayout.addView(createSliderQuestion("How are you feeling today, Dave??", 5, 2));
		
		surveyLayout.addView(createSliderQuestion("How annoyed are you at this survey?", 5, 0));
		
		String[] answerOptions1 = {"Your birthday", "Your un-birthday"};
		surveyLayout.addView(createRadioButtonQuestion("What day is today?", answerOptions1));

		String[] answerOptions2 = {"Android smartphone", 
				"Android tablet", "Android phablet", "Google glass", "Rotary-dial phone", "Bananaphone"};
		surveyLayout.addView(createRadioButtonQuestion("What device are you using to take this survey?", answerOptions2));

		String[] answerOptions3 = {"Android smartphone", null, "blergh"};
		surveyLayout.addView(createRadioButtonQuestion(null, answerOptions3));

		surveyLayout.addView(createSliderQuestion("Eli, how far are you through your current audio book?", 100, 32));
	}
	
	
	/**
	 * Creates the text of a question
	 * @param text The text of the question
	 * @return TextView (to be displayed as question text)
	 */
	private TextView createQuestionText(String text) {
		TextView question = (TextView) getLayoutInflater().inflate(R.layout.survey_question_text, null);
		
		// Clean inputs
		if (text == null) {
			text = getResources().getString(R.string.question_error_text);
		}
		
		// Set the question text
		question.setText(text);
		
		return question;
	}
	
	
	/**
	 * Creates a slider with a range of discrete values
	 * @param questionText The text of the question to be asked
	 * @param numberOfValues A range of "0-4" has 5 values
	 * @param defaultValue Starts at 0; can be as high as (numberOfValues - 1)
	 * @return LinearLayout A slider bar
	 */
	private LinearLayout createSliderQuestion(String questionText, int numberOfValues, int defaultValue) {
		LinearLayout question = (LinearLayout) getLayoutInflater().inflate(R.layout.survey_slider_question, null);
		SeekBar slider = (SeekBar) question.findViewById(R.id.slider);
		
		// Set the text of the question itself
		TextView questionTextView = (TextView) question.findViewById(R.id.questionText);
		if (questionText != null) {
			questionTextView.setText(questionText);
		}
		
		// Clean inputs/force them to be usable numbers
		if (numberOfValues < 2) { numberOfValues = 2; }
		if (numberOfValues > 100) {	numberOfValues = 100; }
		if (defaultValue < 0) {	defaultValue = 0; }
		if (defaultValue > numberOfValues - 1) { defaultValue = numberOfValues - 1; }
		
		// Set the slider's range and default/starting value
		slider.setMax(numberOfValues - 1);
		slider.setProgress(defaultValue);
		
		return question;
	}
	
	
	/**
	 * Creates a group of radio buttons
	 * @param questionText The text of the question
	 * @param answers An array of strings that are options matched with radio buttons
	 * @return RadioGroup A vertical set of radio buttons 
	 */
	private LinearLayout createRadioButtonQuestion(String questionText, String[] answers) {
		LinearLayout question = (LinearLayout) getLayoutInflater().inflate(R.layout.survey_radio_button_question, null);
		RadioGroup radioGroup = (RadioGroup) question.findViewById(R.id.radioGroup);
		
		// Set the text of the question itself
		TextView questionTextView = (TextView) question.findViewById(R.id.questionText);
		if (questionText != null) {
			questionTextView.setText(questionText);
		}
		
		// If the array of answers is null or too short, replace it with an error message
		if ((answers == null) || (answers.length < 2)) {
			String replacementAnswer = getResources().getString(R.string.question_error_text);
			String[] replacementAnswers = {replacementAnswer, replacementAnswer};
			answers = replacementAnswers;
		}
		
		// Loop through the answer strings, and make each one a radio button option
		for (int i = 0; i < answers.length; i++) {
			RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.survey_radio_button, null);
			if (answers[i] != null) {
				radioButton.setText(answers[i]);
			}
			radioGroup.addView(radioButton);
		}
		
		return question;
	}
	
	
	/**
	 * Creates a question with an open-response, text-input field
	 * @param questionText The text of the question
	 * @param inputTextType The type of answer (number, text, etc.)
	 * @return LinearLayout question and answer
	 */
	private LinearLayout createFreeResponseQuestion(String questionText, TextFieldType inputTextType) {
		LinearLayout question = (LinearLayout) getLayoutInflater().inflate(R.layout.survey_open_response_question, null);

		EditText editText = (EditText) getLayoutInflater().inflate(R.layout.survey_free_number_input, null);

		// Set the text of the question itself
		TextView questionTextView = (TextView) question.findViewById(R.id.questionText);
		if (questionText != null) {
			questionTextView.setText(questionText);
		}
		
		switch (inputTextType) {
		case NUMERIC:
			editText = (EditText) getLayoutInflater().inflate(R.layout.survey_free_number_input, null);
			break;
			
		case SINGLE_LINE_TEXT:
			editText = (EditText) getLayoutInflater().inflate(R.layout.survey_free_text_input, null);			
			break;
			
		case MULTI_LINE_TEXT:
			editText = (EditText) getLayoutInflater().inflate(R.layout.survey_multiline_text_input, null);			
			break;

		default:
			editText = (EditText) getLayoutInflater().inflate(R.layout.survey_free_text_input, null);			
			break;
		}
		
		question.addView(editText);
		
		// TODO: prevent the EditText from gaining focus- see here: http://stackoverflow.com/questions/1555109/stop-edittext-from-gaining-focus-at-activity-startup
		// TODO: prevent the EditText blue bar on the bottom from being the whole width of the screen
		
		return question;
	}
	
	public enum TextFieldType {
		NUMERIC,
		SINGLE_LINE_TEXT,
		MULTI_LINE_TEXT;
	}
	
}
