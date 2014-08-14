package org.beiwe.app.survey;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SurveyAnswersRecorder {

	public static String header = "question id,question type,question text,question answer options,answer";


	public static void gatherAllAnswers(LinearLayout surveyLayout) {
		TextFileManager.getSurveyAnswersFile().newFile();
		LinearLayout questionsLayout = (LinearLayout) surveyLayout.findViewById(R.id.surveyQuestionsLayout);

		for (int i = 0; i < questionsLayout.getChildCount(); i++) {
			View childView = questionsLayout.getChildAt(i);
			String questionType = childView.getTag().toString();
			
			if (questionType.equals("infoTextbox")) {
				// Do nothing
			}			
			else if (questionType.equals("sliderQuestion")) {
				getAnswerFromSliderQuestion(childView);
			}
			else if (questionType.equals("radioButtonQuestion")) {
				getAnswerFromRadioButtonQuestion(childView);
			}
			else if (questionType.equals("checkboxQuestion")) {
				getAnswerFromCheckboxQuestion(childView);
			}
			else if (questionType.equals("openResponseQuestion")) {
				getAnswerFromOpenResponseQuestion(childView);
			}
		}
	}
	
	private static void getAnswerFromSliderQuestion(View childView) {
		QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
		SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.getChildAt(2);
		// TODO: figure out why getChildAt() works but findViewById() doesn't. It's weird, because findViewById() works for some IDs!
		//SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.findViewById(R.id.theSlider);
		if (slider != null) {
			//Log.i("SurveyAnswersRecorder.java", "slider != null");
			if (slider.getHasBeenTouched()) {
				int answer = slider.getProgress();
				recordAnswer(wholeQuestion.getQuestionDescription(), "" + answer);
			}
			else {
				recordAnswer(wholeQuestion.getQuestionDescription(), "NO ANSWER SELECTED");
			}
		}
		else {
			//Log.i("SurveyAnswersRecorder.java", "slider == null");
		}		
	}
	
	private static void getAnswerFromRadioButtonQuestion(View childView) {
		QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
		RadioGroup radioGroup = (RadioGroup) wholeQuestion.findViewById(R.id.radioGroup);
		if (radioGroup != null) {
			int selectedId = radioGroup.getCheckedRadioButtonId();
			RadioButton selectedButton = (RadioButton) radioGroup.findViewById(selectedId);
			if (selectedButton != null) {
				String selectedAnswer = (String) selectedButton.getText();
				recordAnswer(wholeQuestion.getQuestionDescription(), selectedAnswer);						
			}
			else {
				recordAnswer(wholeQuestion.getQuestionDescription(), "NO ANSWER SELECTED");						
			}
		}		
	}
	
	private static void getAnswerFromCheckboxQuestion(View childView) {
		QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
		LinearLayout checkboxesList = (LinearLayout) wholeQuestion.findViewById(R.id.checkboxesList);
		String selectedAnswers = InputListener.getSelectedCheckboxes(checkboxesList);
		recordAnswer(wholeQuestion.getQuestionDescription(), selectedAnswers);
	}
	
	private static void getAnswerFromOpenResponseQuestion(View childView) {
		try {
			QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
			LinearLayout textFieldContainer = (LinearLayout) wholeQuestion.findViewById(R.id.textFieldContainer);
			EditText textField = (EditText) textFieldContainer.getChildAt(0);
			recordAnswer(wholeQuestion.getQuestionDescription(), textField.getText().toString());
		} catch (Exception e) {
			// TODO: figure out how to log errors
		}
	}
	
	private static void recordAnswer(QuestionDescription questionDescription, String answer) {
		
		String line = "";
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getId());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getType());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getText());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(questionDescription.getOptions());
		line += TextFileManager.DELIMITER;
		line += SurveyTimingsRecorder.sanitizeString(answer);

		TextFileManager.getSurveyAnswersFile().write(line);
	}

}
