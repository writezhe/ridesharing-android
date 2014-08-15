package org.beiwe.app.survey;

import java.util.ArrayList;
import java.util.List;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SurveyAnswersRecorder {

	public static String header = "question id,question type,question text,question answer options,answer";
	private static String noAnswer = "NO_ANSWER_SELECTED";
	private static String errorCode = "ERROR_QUESTION_NOT_RECORDED";
	
	private static List<Integer> unansweredQuestionNumbers;


	public static void gatherAllAnswers(LinearLayout surveyLayout, Context appContext) {
		LinearLayout questionsLayout = (LinearLayout) surveyLayout.findViewById(R.id.surveyQuestionsLayout);
		
		ArrayList<String> fileLines = new ArrayList<String>();

		unansweredQuestionNumbers = new ArrayList<Integer>();
		int questionNumber = 1;

		for (int i = 0; i < questionsLayout.getChildCount(); i++) {
			View childView = questionsLayout.getChildAt(i);
			String questionType = childView.getTag().toString();
			
			if (questionType.equals("infoTextbox")) {
				// Do nothing
			}
			else if (questionType.equals("sliderQuestion")) {
				fileLines.add(answerFromSliderQuestion(childView, questionNumber++));
			}
			else if (questionType.equals("radioButtonQuestion")) {
				fileLines.add(answerFromRadioButtonQuestion(childView, questionNumber++));
			}
			else if (questionType.equals("checkboxQuestion")) {
				fileLines.add(answerFromCheckboxQuestion(childView, questionNumber++));
			}
			else if (questionType.equals("openResponseQuestion")) {
				fileLines.add(answerFromOpenResponseQuestion(childView, questionNumber++));
			}
		}
		
		String unansweredQuestions = unansweredQuestionNumbers.toString();
		unansweredQuestions = unansweredQuestions.replaceAll("\\[", "");
		unansweredQuestions = unansweredQuestions.replaceAll("\\]", "");
		Log.i("QUESTIONS", "UNANSWERED QUESTIONS = " + unansweredQuestions + " String length = " + unansweredQuestions.length());
		
		if (unansweredQuestions.length() > 0) {
			showUnansweredQuestionsWarning(appContext, unansweredQuestions);
		}
		
		writeLinesToFile(fileLines);
	}

	
	private static void showUnansweredQuestionsWarning(Context appContext, String unansweredQuestions) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(appContext);
		alertBuilder.setTitle("Unanswered Questions");
		alertBuilder.setMessage("You did not answer the following questions: " + unansweredQuestions + ". Do you want to submit the survey anyways?");
		alertBuilder.setPositiveButton("Submit anyways", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Deal with this; figure out how to finish() the activity and write the data to a file
			}
		});
		alertBuilder.setNegativeButton("Go back to survey", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Deal with this; figure out how to NOT finish() the activity
			}
		});
		alertBuilder.create().show();		
	}
	
	
	private static String answerFromSliderQuestion(View childView, int questionNumber) {
		try {
			QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
			SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.getChildAt(2);
			// TODO: figure out why getChildAt() works but findViewById() doesn't. It's weird, because findViewById() works for some IDs!
			//SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.findViewById(R.id.theSlider);
			if (slider.getHasBeenTouched()) {
				int answer = slider.getProgress();
				return answerFileLine(wholeQuestion.getQuestionDescription(), "" + answer);
			}
			else {
				unansweredQuestionNumbers.add(questionNumber);
				return answerFileLine(wholeQuestion.getQuestionDescription(), noAnswer);
			}
		} catch (Exception e) {
			return errorCode;
		}
	}
	
	private static String answerFromRadioButtonQuestion(View childView, int questionNumber) throws NullPointerException {
		try {
			QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
			RadioGroup radioGroup = (RadioGroup) wholeQuestion.findViewById(R.id.radioGroup);
			int selectedId = radioGroup.getCheckedRadioButtonId();
			RadioButton selectedButton = (RadioButton) radioGroup.findViewById(selectedId);
			if (selectedButton != null) {
				String selectedAnswer = (String) selectedButton.getText();
				return answerFileLine(wholeQuestion.getQuestionDescription(), selectedAnswer);						
			}
			else {
				unansweredQuestionNumbers.add(questionNumber);
				return answerFileLine(wholeQuestion.getQuestionDescription(), noAnswer);
			}
		} catch (Exception e) {
			return errorCode;
		}
	}
	
	private static String answerFromCheckboxQuestion(View childView, int questionNumber) throws NullPointerException {
		try {
			QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
			LinearLayout checkboxesList = (LinearLayout) wholeQuestion.findViewById(R.id.checkboxesList);
			String selectedAnswers = InputListener.getSelectedCheckboxes(checkboxesList);
			if (selectedAnswers.equals("[]")) {
				unansweredQuestionNumbers.add(questionNumber);
				selectedAnswers = noAnswer;
			}
			return answerFileLine(wholeQuestion.getQuestionDescription(), selectedAnswers);
		} catch (Exception e) {
			return errorCode;
		}
	}
	
	private static String answerFromOpenResponseQuestion(View childView, int questionNumber) throws NullPointerException {
		try {
			QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
			LinearLayout textFieldContainer = (LinearLayout) wholeQuestion.findViewById(R.id.textFieldContainer);
			EditText textField = (EditText) textFieldContainer.getChildAt(0);
			String answer = textField.getText().toString();
			if (answer == null || answer.equals("")) {
				unansweredQuestionNumbers.add(questionNumber);
				answer = noAnswer;
			}
			return answerFileLine(wholeQuestion.getQuestionDescription(), answer);
		} catch (Exception e) {
			return errorCode;
		}
	}
	
	private static String answerFileLine(QuestionDescription questionDescription, String answer) {
		
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

		return line;
	}

	private static void writeLinesToFile(ArrayList<String> fileLines) {
		TextFileManager.getSurveyAnswersFile().newFile();
		for (String line : fileLines) {
			TextFileManager.getSurveyAnswersFile().write(line);
		}
	}
	
}
