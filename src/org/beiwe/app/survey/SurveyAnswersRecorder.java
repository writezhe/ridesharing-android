package org.beiwe.app.survey;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SurveyAnswersRecorder {

	//public static String header = "timestamp,question id,question type,question text,question answer options,answer";
	public static String header = "question type,answer";


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
		LinearLayout wholeQuestion = (LinearLayout) childView;
		SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.getChildAt(2);
		// TODO: figure out why getChildAt() works but findViewById() doesn't. It's weird, because findViewById() works for some IDs!
		//SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.findViewById(R.id.theSlider);
		if (slider != null) {
			//Log.i("SurveyAnswersRecorder.java", "slider != null");
			if (slider.getHasBeenTouched()) {
				int answer = slider.getProgress();
				recordAnswer("Slider Question", "" + answer);
			}
			else {
				recordAnswer("Slider Question", "NO ANSWER SELECTED");
			}
		}
		else {
			//Log.i("SurveyAnswersRecorder.java", "slider == null");
		}		
	}
	
	private static void getAnswerFromRadioButtonQuestion(View childView) {
		LinearLayout wholeQuestion = (LinearLayout) childView;
		RadioGroup radioGroup = (RadioGroup) wholeQuestion.findViewById(R.id.radioGroup);
		if (radioGroup != null) {
			int selectedId = radioGroup.getCheckedRadioButtonId();
			RadioButton selectedButton = (RadioButton) radioGroup.findViewById(selectedId);
			if (selectedButton != null) {
				String selectedAnswer = (String) selectedButton.getText();
				recordAnswer("Radio Button Question", selectedAnswer);						
			}
			else {
				recordAnswer("Radio Button Question", "NO ANSWER SELECTED");						
			}
		}		
	}
	
	private static void getAnswerFromCheckboxQuestion(View childView) {
		LinearLayout wholeQuestion = (LinearLayout) childView;
		LinearLayout checkboxesList = (LinearLayout) wholeQuestion.findViewById(R.id.checkboxesList);
		String selectedAnswers = InputListener.getSelectedCheckboxes(checkboxesList);
		recordAnswer("Checkbox Question", selectedAnswers);
	}
	
	private static void getAnswerFromOpenResponseQuestion(View childView) {
		try {
			LinearLayout wholeQuestion = (LinearLayout) childView;
			LinearLayout textFieldContainer = (LinearLayout) wholeQuestion.findViewById(R.id.textFieldContainer);
			EditText textField = (EditText) textFieldContainer.getChildAt(0);
			recordAnswer("Open Response Question", textField.getText().toString());
		} catch (Exception e) {
			recordAnswer("Open Response Question", "ERROR");
		}		
	}
	
	private static void recordAnswer(String questionType, String answer) {
		String sanitizedAnswer = questionType + TextFileManager.DELIMITER;
		sanitizedAnswer += SurveyTimingsRecorder.sanitizeString(answer);
		TextFileManager.getSurveyAnswersFile().write(sanitizedAnswer);
	}

}
