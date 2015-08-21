package org.beiwe.app.survey;

import org.beiwe.app.JSONUtils;
import org.beiwe.app.R;
import org.beiwe.app.session.SessionActivity;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.ui.user.MainMenuActivity;
import org.beiwe.app.ui.utils.SurveyNotifications;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/**The SurveyActivity displays to the user the survey that has been pushed to the device.
 * Layout in this activity is rendered, not static.
 * @author Josh Zagorsky, Eli Jones
 */

public class SurveyActivity extends SessionActivity {
	
	private SurveyAnswersRecorder answersRecorder;
	private String surveyId;
	private JSONObject surveySettings;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);
		Intent triggerIntent = getIntent();
		surveyId = triggerIntent.getStringExtra("surveyId");
		
		try { surveySettings = new JSONObject( PersistentData.getSurveySettings(surveyId) ); }
		catch (JSONException e) {
			Log.e("Survey Activity", "There was an error parsing survey settings");
			e.printStackTrace();
			surveySettings = new JSONObject();
		}
		
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				Log.d("Survey renderer", "surveyId: " + surveyId);
				renderSurvey( PersistentData.getSurveyContent(surveyId) );
			}
		}
	}
	
	
	/**Display, in the survey's spot in the Activity/page, the survey itself
	 * @param jsonSurveyString the JSON file, as a string, used to render the survey questions */
	private void renderSurvey(String jsonSurveyString) {
		// Get the survey layout objects that we'll add questions to
		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyLayout);
		// Parse the JSON list of questions and render them as Views
		JsonParser jsonParser = new JsonParser(getApplicationContext());
		
		Boolean randomizeWithMemory = surveySettings.optBoolean(getString(R.string.randomizeWithMemory), false);
		Boolean randomize = surveySettings.optBoolean(getString(R.string.randomize), false);
		
		int numberQuestions = surveySettings.optInt(getString(R.string.numberQuestions), 0);
		jsonParser.renderSurveyFromJSON(surveyLayout, jsonSurveyString, surveyId, randomize, numberQuestions, randomizeWithMemory );
		
		// Record the time that the survey was first visible to the user
		SurveyTimingsRecorder.recordSurveyFirstDisplayed(surveyId);
	}
	
	
	/**Called when the user presses "Submit" at the bottom of the survey,
	 * saves the answers, and takes the user back to the main page.
	 * @param v */
	public void submitButtonPressed(View v) {
		SurveyTimingsRecorder.recordSubmit(getApplicationContext());
		answersRecorder = new SurveyAnswersRecorder();
		LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.surveyLayout);
		String unansweredQuestions = answersRecorder.gatherAllAnswers(surveyLayout, getApplicationContext());
		if (unansweredQuestions.length() > 0) {  // If there are unanswered questions, show a warning
			showUnansweredQuestionsWarning(unansweredQuestions); }
		else { // If there are no unanswered questions, record the answers and close
			recordAnswersAndClose(); }
	}
	
	
	/**Show a warning pop-up saying "some questions aren't answered; do you 
	 * want to go back, or submit anyway?"
	 * @param unansweredQuestions a String of unanswered questions */
	private void showUnansweredQuestionsWarning(String unansweredQuestions) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SurveyActivity.this);
		alertBuilder.setTitle("Unanswered Questions");
		alertBuilder.setMessage("You did not answer the following questions: " + unansweredQuestions + ". Do you want to submit the survey anyway?");
		alertBuilder.setPositiveButton("Submit anyway", new DialogInterface.OnClickListener() {
			@Override // If the user clicks "Submit anyway", record the answers and close
			public void onClick(DialogInterface dialog, int which) { recordAnswersAndClose(); }
		});
		alertBuilder.setNegativeButton("Go back to survey", new DialogInterface.OnClickListener() {
			@Override // If the user clicked "Go back to survey", then close the pop-up and do nothing.
			public void onClick(DialogInterface dialog, int which) {}
		});
		alertBuilder.create().show();
	}
	
	
	/**Write the Survey answers to a new SurveyAnswers.csv file, and show a Toast reporting either success or failure*/
	private void recordAnswersAndClose() {
		// Write the data to a SurveyAnswers file
		// Show a Toast telling the user either "Thanks, success!" or "Oops, there was an error"
		String toastMsg = null;
		if (answersRecorder.writeLinesToFile(surveyId)) {
			toastMsg = PersistentData.getSurveySubmitSuccessToastText();
		} else {
			toastMsg = getApplicationContext().getResources().getString(R.string.survey_submit_error_message);
		}
		Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();

		// Close the Activity
		startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
		PersistentData.setSurveyNotificationState(surveyId, false);		
		SurveyNotifications.dismissNotification(getApplicationContext(), surveyId);
		finish();
	}
}
