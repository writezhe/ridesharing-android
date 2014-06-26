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
		
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		jsonParser.renderSurveyFromJSON(surveyLayout);
	}

}
