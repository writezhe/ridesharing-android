package org.beiwe.app.survey;

import java.util.ArrayList;
import java.util.List;

import org.beiwe.app.R;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SurveyAnswersRecorder {

	public static String header = "question id,question type,question text,question answer options,answer";
	private static String noAnswer = "NO_ANSWER_SELECTED";
	private static String errorCode = "ERROR_QUESTION_NOT_RECORDED";
	
	private ArrayList<String> fileLines;
	private List<Integer> unansweredQuestionNumbers;

	
	/**
	 * Get all the answers from the Survey Layout
	 * @param surveyLayout
	 * @param appContext
	 * @return a String that's a list of unanswered questions
	 */
	public String gatherAllAnswers(LinearLayout surveyLayout, Context appContext) {
		LinearLayout questionsLayout = (LinearLayout) surveyLayout.findViewById(R.id.surveyQuestionsLayout);
		
		fileLines = new ArrayList<String>();
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
		
		return unansweredQuestions;
	}
	
	
	/**
	 * Get the answer from a Slider Question
	 * @param childView
	 * @param questionNumber
	 * @return the answer as a String
	 */
	private String answerFromSliderQuestion(View childView, int questionNumber) {
		try {
			QuestionLinearLayout wholeQuestion = (QuestionLinearLayout) childView;
			SeekBarEditableThumb slider = (SeekBarEditableThumb) wholeQuestion.getChildAt(2);
			/* We can't use findViewById() to get the slider, because the slider's ID got reset so
			 * that every slider would have a unique ID. That's why it's called using getChildAt().
			 * The downside of this is that it makes the layout file brittle; if
			 * survey_slider_question.xml is changed and the slider is no longer the third element,
			 * then we need to change the index fed as the argument to getChildAt(). */
			
			if (slider.getHasBeenTouched()) {
				int answer = slider.getProgress() + slider.getMin();
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
	
	
	/**
	 * Get the answer from a Radio Button Question
	 * @param childView
	 * @param questionNumber
	 * @return the answer as a String
	 */
	private String answerFromRadioButtonQuestion(View childView, int questionNumber) {
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
	
	
	/**
	 * Get the answer from a Checkbox Question
	 * @param childView
	 * @param questionNumber
	 * @return the answer as a String
	 */
	private String answerFromCheckboxQuestion(View childView, int questionNumber) {
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
	
	
	/**
	 * Get the answer from an Open Response question
	 * @param childView
	 * @param questionNumber
	 * @return the answer as a String
	 */
	private String answerFromOpenResponseQuestion(View childView, int questionNumber) {
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
	
	
	/**
	 * Create a line (that will get written to a CSV file) that includes
	 * question metadata and the user's answer
	 * @param questionDescription metadata on the question
	 * @param answer the user's answer
	 * @return a String that can be written as a line to a file
	 */
	private String answerFileLine(QuestionDescription questionDescription, String answer) {
		
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

	
	/**
	 * Create a new SurveyAnswers file, and write all of the answers to it
	 * @return TRUE if wrote successfully; FALSE if caught an exception
	 */
	public Boolean writeLinesToFile(String surveyId) {
		try {
			TextFileManager.getSurveyAnswersFile().newFile(surveyId);
			for (String line : fileLines) {
				TextFileManager.getSurveyAnswersFile().writeEncrypted(line);
			}
			TextFileManager.getSurveyAnswersFile().closeFile();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
}
