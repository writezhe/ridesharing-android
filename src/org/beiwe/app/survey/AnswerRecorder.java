package org.beiwe.app.survey;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AnswerRecorder {
	
	private static String delimiter = "" + '\t';
		
	
	/**
	 * Create a new Survey Response file, and record the timestamp of when 
	 * the survey first displayed to the user
	 */
	public static void recordSurveyFirstDisplayed() {
		// TODO: switch it to SurveyResponseFile()
		//TextFileManager.getSurveyResponseFile().newFile();
		
		String message = "Survey first rendered and displayed to user";
		appendLineToLogFile(message);
	}
	
	
	/**
	 * Record (in the Survey Response file) the answer to a single survey question
	 * @param answer the user's input answer
	 * @param questionDescription the question that was answered
	 */
	public static void recordAnswer(String answer, QuestionDescription questionDescription) {
		String message = "";
		message += sanitizeString(questionDescription.getId()) + delimiter;
		message += sanitizeString(questionDescription.getType()) + delimiter;
		message += sanitizeString(questionDescription.getText()) + delimiter;
		message += sanitizeString(questionDescription.getOptions()) + delimiter;
		message += sanitizeString(answer) + delimiter;
		Log.i("AnswerRecorder", message);
		
		appendLineToLogFile(message);
	}

	
	/**
	 * Record (in the Survey Response file) that the user pressed the "Submit" 
	 * button at the bottom of a survey
	 * @param appContext
	 */
	public static void recordSubmit(Context appContext) {
		String message = "User hit submit";
		appendLineToLogFile(message);
		
		// If successful, pop a Toast telling the user "thanks. success!"
		String msg = appContext.getResources().getString(R.string.survey_submit_success_message);
		Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show();
	}
	
	
	/**
	 * Write a line to the bottom of the Survey Response file
	 * @param message
	 */
	private static void appendLineToLogFile(String message) {
		/** Handles the logging, includes a new line for the CSV files.
		 * This code is otherwised reused everywhere.*/
		Long javaTimeCode = System.currentTimeMillis();
		String line = javaTimeCode.toString() + delimiter + message +"\n"; 

		Log.i("AnswerRecorder", line);
		TextFileManager.getDebugLogFile().write(line);
		//TextFileManager.getSurveyResponseFile().write(line);
	}

	
	/**
	 * Sanitize a string for use in a Tab-Separated Values file
	 * @param input string to be sanitized
	 * @return String with tabs and newlines removed
	 */
	private static String sanitizeString(String input) {
		// TODO: fix RegEx so it sanitizes '\t'
		input = input.replaceAll("[\t\n\r]", "  ");
		return input;
	}

}
