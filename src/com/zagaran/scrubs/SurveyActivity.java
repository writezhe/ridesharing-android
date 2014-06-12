package com.zagaran.scrubs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SurveyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyLayout);
				
		surveyLayout.addView(createQuestionText("How are you feeling today, Dave??"));
		surveyLayout.addView(createSlider(5, 2));

		surveyLayout.addView(createQuestionText("How annoyed are you at this survey?"));
		surveyLayout.addView(createSlider(5, 0));
		
		surveyLayout.addView(createQuestionText("What day is today?"));
		String[] answerOptions1 = {"Your birthday", "Your un-birthday"};
		surveyLayout.addView(createRadioButtons(answerOptions1));

		surveyLayout.addView(createQuestionText("What device are you using to take this survey?"));
		String[] answerOptions2 = {"Android smartphone", 
				"Android tablet", "Android phablet", "Google glass", "Rotary-dial phone", "Bananaphone"};
		surveyLayout.addView(createRadioButtons(answerOptions2));

		surveyLayout.addView(createQuestionText(null));
		String[] answerOptions3 = {"Android smartphone", null, "blergh"};
		surveyLayout.addView(createRadioButtons(answerOptions3));

		surveyLayout.addView(createQuestionText("Eli, how far are you through your current audio book?"));
		surveyLayout.addView(createSlider(100, 32));
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
	 * @param numberOfValues A range of "0-4" has 5 values
	 * @param defaultValue Starts at 0; can be as high as (numberOfValues - 1)
	 * @return SeekBar A slider bar
	 */
	private SeekBar createSlider(int numberOfValues, int defaultValue) {
		SeekBar slider = (SeekBar) getLayoutInflater().inflate(R.layout.survey_slider, null);

		// Clean inputs/force them to be usable numbers
		if (numberOfValues < 2) { numberOfValues = 2; }
		if (numberOfValues > 100) {	numberOfValues = 100; }
		if (defaultValue < 0) {	defaultValue = 0; }
		if (defaultValue > numberOfValues - 1) { defaultValue = numberOfValues - 1; }
		
		// Set the slider's range and default/starting value
		slider.setMax(numberOfValues - 1);
		slider.setProgress(defaultValue);
		
		return slider;
	}
	
	
	/**
	 * Creates a group of radio buttons
	 * @param answers An array of strings that are options matched with radio buttons
	 * @return RadioGroup A vertical set of radio buttons 
	 */
	private RadioGroup createRadioButtons(String[] answers) {
		RadioGroup radioGroup = (RadioGroup) getLayoutInflater().inflate(R.layout.survey_radio_group, null);
		
		// TODO: clean inputs/add constraints, or decide it's unnecessary
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
		
		return radioGroup;
	}
	
}
