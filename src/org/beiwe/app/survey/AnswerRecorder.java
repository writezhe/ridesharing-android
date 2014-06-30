package org.beiwe.app.survey;

import android.util.Log;

public class AnswerRecorder {
	
	public static void recordAnswer(String answer, QuestionDescription questionDescription) {
		Log.i("AnswerRecorder", "Question ID: " + questionDescription.getId());
		Log.i("AnswerRecorder", "Question Type: " + questionDescription.getType());
		Log.i("AnswerRecorder", "Question Text: " + questionDescription.getText());
		Log.i("AnswerRecorder", "Question Answer Options: " + questionDescription.getOptions());
		Log.i("AnswerRecorder", "Answer: " + answer);
		Log.i("AnswerRecorder", "__________________");
	}
	
}
