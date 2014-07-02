package org.beiwe.app.survey;

import org.beiwe.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
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
		JsonParser jsonParser = new JsonParser(getApplicationContext());

		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyQuestionsLayout);
		String jsonSurveyString = downloader.getJsonSurveyString();
		
		jsonParser.renderSurveyFromJSON(surveyLayout, jsonSurveyString);
		
		AnswerRecorder.recordSurveyFirstDisplayed();
	}
	
	
	public void submitButtonPressed(View v) {
		AnswerRecorder.recordSubmit(getApplicationContext());
		finish();
	}

}
