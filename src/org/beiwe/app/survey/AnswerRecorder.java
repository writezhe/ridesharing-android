package org.beiwe.app.survey;

import org.beiwe.app.R;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AnswerRecorder {
	
	
	public static void recordSurveyFirstDisplayed() {
		Log.i("AnswerRecorder", "Survey first rendered and displayed to user");

		// TODO: print this event to a TSV file
	}
	
	
	public static void recordAnswer(String answer, QuestionDescription questionDescription) {
		String line = "";
		String delimiter = "" + '\t';
		line += sanitizeString(questionDescription.getId()) + delimiter;
		line += sanitizeString(questionDescription.getType()) + delimiter;
		line += sanitizeString(questionDescription.getText()) + delimiter;
		line += sanitizeString(questionDescription.getOptions()) + delimiter;
		line += sanitizeString(answer) + delimiter;
		Log.i("AnswerRecorder", line);
		
		// TODO: print this to a TSV file
	}

	
	public static void recordSubmit(Context appContext) {
		Log.i("AnswerRecorder", "User hit submit");

		// TODO: print this event to a TSV file
		
		// If successful, pop a Toast telling the user "thanks. success!"
		String msg = appContext.getResources().getString(R.string.survey_submit_success_message);
		Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show();
	}
	
	
	/**
	 * Sanitize a string for use in a Tab-Separated Values file
	 * @param input string to be sanitized
	 * @return String with tabs and newlines removed
	 */
	private static String sanitizeString(String input) {
		input = input.replaceAll("[\t\n\r]", "  ");
		return input;
	}

}
