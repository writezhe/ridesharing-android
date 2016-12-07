package org.beiwe.app.survey;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

import java.util.List;

public class SurveyAnswersRecorder {
	public static String header = "question id,question type,question text,question answer options,answer";
	private static String noAnswer = "NO_ANSWER_SELECTED";
	private static String errorCode = "ERROR_QUESTION_NOT_RECORDED";
	

	public static String getAnswerString(View questionLayout, QuestionType.Type questionType) {
		if (questionType == QuestionType.Type.SLIDER) {
			return SurveyAnswersRecorder.getAnswerFromSliderQuestion(questionLayout);
		} else if (questionType == QuestionType.Type.RADIO_BUTTON) {
			return SurveyAnswersRecorder.getAnswerFromRadioButtonQuestion(questionLayout);
		} else if (questionType == QuestionType.Type.CHECKBOX) {
			return SurveyAnswersRecorder.getAnswerFromCheckboxQuestion(questionLayout);
		} else if (questionType == QuestionType.Type.FREE_RESPONSE) {
			return SurveyAnswersRecorder.getAnswerFromOpenResponseQuestion(questionLayout);
		} else {
			return null;
		}
	}


	/**Get the answer from a Slider Question
	 * @return the answer as a String */
	public static String getAnswerFromSliderQuestion(View questionLayout) {
		SeekBarEditableThumb slider = (SeekBarEditableThumb) questionLayout.findViewById(R.id.slider);
		if (slider.getHasBeenTouched()) {
			int answer = slider.getProgress() + slider.getMin();
			return "" + answer;
		}
		return null;
	}


	/**Get the answer from a Radio Button Question
	 * @return the answer as a String */
	public static String getAnswerFromRadioButtonQuestion(View questionLayout) {
		RadioGroup radioGroup = (RadioGroup) questionLayout.findViewById(R.id.radioGroup);
		int selectedId = radioGroup.getCheckedRadioButtonId();
		RadioButton selectedButton = (RadioButton) radioGroup.findViewById(selectedId);
		if (selectedButton != null) {
			int answerInt = selectedId;  //TODO: does this give the answer ordinal, or some random ID?
			String answerString = (String) selectedButton.getText();
			return answerString;
		}
		return null;
	}


	/**Get the answer from a Checkbox Question
	 * @return the answer as a String */
	public static String getAnswerFromCheckboxQuestion(View questionLayout) {
		LinearLayout checkboxesList = (LinearLayout) questionLayout.findViewById(R.id.checkboxesList);
		String selectedAnswers = getSelectedCheckboxes(checkboxesList);
		if (selectedAnswers.equals("[]")) {
			return noAnswer;
		} else {
			return selectedAnswers;
		}
	}


	/**Get the answer from an Open Response question
	 * @return the answer as a String */
	public static String getAnswerFromOpenResponseQuestion(View questionLayout) {
		LinearLayout textFieldContainer = (LinearLayout) questionLayout.findViewById(R.id.textFieldContainer);
		EditText textField = (EditText) textFieldContainer.getChildAt(0);
		String answer = textField.getText().toString();
		if (answer == null || answer.equals("")) {
			return null;
		}
		return answer;
	}
	
	
	/**Create a line (that will get written to a CSV file) that includes
	 * question metadata and the user's answer
	 * @param questionDescription metadata on the question
	 * @return a String that can be written as a line to a file */
	private String answerFileLine(QuestionData questionDescription) {
		String line = "";
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getId());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getType());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getText());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getOptions());
		line += TextFileManager.DELIMITER;
		if (questionDescription.getAnswerString() == null) {
			line += noAnswer;
		} else {
			line += SurveyTimingsRecorder.sanitizeString(questionDescription.getAnswerString());
		}
		return line;
	}

	
	/** Create a new SurveyAnswers file, and write all of the answers to it
	 * @return TRUE if wrote successfully; FALSE if caught an exception */
	public Boolean writeLinesToFile(String surveyId, List<QuestionData> answers) {
		try {
			TextFileManager.getSurveyAnswersFile().newFile(surveyId);
			for (int i = 0; i < answers.size(); i++) {
				String line = answerFileLine(answers.get(i));
				Log.i("SurveyAnswersRecorder", line);
				TextFileManager.getSurveyAnswersFile().writeEncrypted(line);
			}
			TextFileManager.getSurveyAnswersFile().closeFile();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}


	/**
	 * Return a list of the selected checkboxes in a list of checkboxes
	 * @param checkboxesList a LinearLayout, presumably containing only checkboxes
	 * @return a String formatted like a String[] printed to a single String
	 */
	public static String getSelectedCheckboxes(LinearLayout checkboxesList) {

		// Make a list of the checked answers that reads like a printed array of strings
		String answersList = "[";

		// Iterate over the whole list of CheckBoxes in this LinearLayout
		for (int i = 0; i < checkboxesList.getChildCount(); i++) {

			View childView = checkboxesList.getChildAt(i);
			if (childView instanceof CheckBox) {
				CheckBox checkBox = (CheckBox) childView;

				// If this CheckBox is selected, add it to the list of selected answers
				if (checkBox.isChecked()) {
					answersList += checkBox.getText() + ", ";
				}
			}
		}

		// Trim the last comma off the list so that it's formatted like a String[] printed to a String
		if (answersList.length() > 3) {
			answersList = answersList.substring(0, answersList.length() - 2);
		}
		answersList += "]";

		return answersList;
	}
}
