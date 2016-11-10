package org.beiwe.app.survey;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * QuestionLinearLayout subclasses/extends LinearLayout; all it does is add one
 * custom attribute that stores data on the type of question and helps record
 * the question's text, ID number, etc. along with the user's answer.
 * @author Josh Zagorsky, August 2014
 */
public class QuestionLinearLayout extends LinearLayout {

	// Two custom attributes: questionDescription, and questionID
	private QuestionDescription questionDescription;
	
	
	// Standard constructors
	public QuestionLinearLayout(Context context) {
		super(context);
	}
	
	public QuestionLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public QuestionLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	// Getter and setter for questionDescription attribute 
	public void setQuestionDescription(QuestionDescription questionDescription) {
		this.questionDescription = questionDescription;  
	}
	
	public QuestionDescription getQuestionDescription() {
		return this.questionDescription;
	}
	
}
