package org.beiwe.app.survey;

import org.beiwe.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SurveyActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
		renderSurvey();		
	}
	
	
	private void renderSurvey() {
		QuestionsDownloader downloader = new QuestionsDownloader(getApplicationContext());
		String jsonSurveyString = downloader.getJsonSurveyString();

		LinearLayout surveyLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.survey_layout, null);
		LinearLayout surveyQuestionsLayout = (LinearLayout) surveyLayout.findViewById(R.id.surveyQuestionsLayout);
		
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		jsonParser.renderSurveyFromJSON(surveyQuestionsLayout, jsonSurveyString);

		ViewGroup page = (ViewGroup) findViewById(R.id.scrollViewMain);
		page.addView(surveyLayout);
		
		AnswerRecorder.recordSurveyFirstDisplayed();
	}
	
	
	public void submitButtonPressed(View v) {
		AnswerRecorder.recordSubmit(getApplicationContext());
		finish();
	}

}
