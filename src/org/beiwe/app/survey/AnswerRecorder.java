package org.beiwe.app.survey;

import android.util.Log;

public class AnswerRecorder {
	
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
