package com.zagaran.scrubs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
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
			text = getResources().getString(R.string.default_question_text);
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
	
}
