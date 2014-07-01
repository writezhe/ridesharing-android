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
		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyQuestionsLayout);
		
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		jsonParser.renderSurveyFromJSON(surveyLayout);
		
		AnswerRecorder.recordSurveyFirstDisplayed();
	}
	
	
	public void submitButtonPressed(View v) {
		AnswerRecorder.recordSubmit(getApplicationContext());
		finish();
	}

}
