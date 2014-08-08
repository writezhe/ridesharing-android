package org.beiwe.app.survey;

import org.beiwe.app.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SurveyActivity extends Activity {
	
	private LinearLayout surveyLayout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		
		// Show that the survey is "Loading..."
		showSurveyLoadingSpinner();
		
		// Now try to download the most recent version of the survey
		new GetUpToDateSurvey().execute(" ");
	}
	
	
	/**
	 * Display, in the survey's spot in the Activity/page, a spinning hourglass
	 * equivalent, and a "loading/downloading" explanation message
	 */
	private void showSurveyLoadingSpinner() {
		View loadingSpinner = getLayoutInflater().inflate(R.layout.survey_loading_spinner, null);
		replaceSurveyPageContents(loadingSpinner);
	}
	
	
	/**
	 * Display, in the survey's spot in the Activity/page, the survey itself
	 * @param jsonSurveyString the JSON file, as a string, used to render the survey questions
	 */
	private void renderSurvey(String jsonSurveyString) {
		// Get the survey layout objects that we'll add questions to
		surveyLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.survey_layout, null);
		
		// Parse the JSON list of questions and render them as Views
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		jsonParser.renderSurveyFromJSON(surveyLayout, jsonSurveyString);

		// Display the survey instead of the "Loading..." wheel
		replaceSurveyPageContents(surveyLayout);
		
		// Record the time that the survey was first visible to the user
		AnswerRecorder.recordSurveyFirstDisplayed();
	}
	
	
	/**
	 * Delete/empty the contents of the Survey Activity/page, and replace
	 * them with the View provided here. This is called because adding multiple
	 * children to a ScrollView crashes the app; also, we want to show only the
	 * loading wheel, or the survey, but not both at once.
	 * @param newContents the View to display in the survey Activity/page
	 */
	private synchronized void replaceSurveyPageContents(View newContents) {
		ViewGroup page = (ViewGroup) findViewById(R.id.scrollViewMain);
		page.removeAllViews();
		page.addView(newContents);
	}
	
	
	/**
	 * Called when the user presses "Submit" at the bottom of the survey,
	 * saves the answers, and takes the user back to the main page.
	 * @param v
	 */
	public void submitButtonPressed(View v) {
		AnswerRecorder.recordSubmit(getApplicationContext());
		
		AnswerGatherer.gatherAllAnswers(surveyLayout);
		
		finish();
	}
	
	
	/**
	 * Gets the most up-to-date version of the survey; does it on a separate,
	 * non-blocking thread, because it's a slow network request
	 */
	class GetUpToDateSurvey extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			QuestionsDownloader downloader = new QuestionsDownloader(getApplicationContext());
			return downloader.getJsonSurveyString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			renderSurvey(result);
		}
	}

}
