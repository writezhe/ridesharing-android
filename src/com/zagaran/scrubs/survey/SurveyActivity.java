package com.zagaran.scrubs.survey;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.zagaran.scrubs.R;

public class SurveyActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyLayout);
		
		/*SurveyQuestionRenderer surveyQ = new SurveyQuestionRenderer(getApplicationContext());
		
		surveyLayout.addView(surveyQ.createInfoTextbox("Welcome to the survey! Please answer these questions as creatively as possible.  It helps us debug!"));
		
		surveyLayout.addView(surveyQ.createFreeResponseQuestion("How many eggs did you eat this morning?", SurveyTextFieldType.Type.NUMERIC));

		surveyLayout.addView(surveyQ.createFreeResponseQuestion("What is your nickname, moniker, or nom de guerre?", SurveyTextFieldType.Type.SINGLE_LINE_TEXT));
		
		surveyLayout.addView(surveyQ.createFreeResponseQuestion("Please enter any suggestions you have about this whole process.", SurveyTextFieldType.Type.MULTI_LINE_TEXT));
		
		surveyLayout.addView(surveyQ.createSliderQuestion("How are you feeling today, Dave??", 5, 2));
		
		surveyLayout.addView(surveyQ.createSliderQuestion("How annoyed are you at this survey?", 5, 0));
		
		String[] answerOptions1 = {"Your birthday", "Your un-birthday"};
		surveyLayout.addView(surveyQ.createRadioButtonQuestion("What day is today?", answerOptions1));

		String[] answerOptions2 = {"Android smartphone", 
				"Android tablet", "Android phablet", "Google glass", "Rotary-dial phone", "Bananaphone"};
		surveyLayout.addView(surveyQ.createRadioButtonQuestion("What device are you using to take this survey?", answerOptions2));

		String[] answerOptions3 = {"Android smartphone", null, "blergh"};
		surveyLayout.addView(surveyQ.createRadioButtonQuestion(null, answerOptions3));

		surveyLayout.addView(surveyQ.createSliderQuestion("Eli, how far are you through your current audio book?", 100, 32));
		
		String[] checkboxes1 = {"Tattered baseball cap", "Cowboy boots with spurs", "Lucky talisman necklace", "Tie-dyed spandex"};
		surveyLayout.addView(surveyQ.createCheckboxQuestion("Which of the following are you wearing?", checkboxes1)); */
		
		
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		jsonParser.renderSurveyFromJSON(surveyLayout);
	}

}
