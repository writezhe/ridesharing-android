package org.beiwe.app.survey;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class InputListener {
	
	private Context appContext;
	
	public InputListener(Context applicationContext) {
		this.appContext = applicationContext;
	}
	
	
	/** Listens for a touch/answer to a Slider Question, and records the answer */
	public class SliderListener implements OnSeekBarChangeListener {

		QuestionDescription questionDescription;
		
		public SliderListener(QuestionDescription questionDescription) {
			this.questionDescription = questionDescription;
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO: Start tracking touch if JP wants
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			String answer = "" + seekBar.getProgress();
			SurveyTimingsRecorder.recordAnswer(answer, questionDescription);
		}
		
	}
	
	
	/** Listens for a touch/answer to a Radio Button Question, and records the answer */
	public class RadioButtonListener implements OnCheckedChangeListener {

		QuestionDescription questionDescription;
		
		public RadioButtonListener(QuestionDescription questionDescription) {
			this.questionDescription = questionDescription;
		}
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			RadioButton selectedButton = (RadioButton) group.findViewById(checkedId);
			if (selectedButton.isChecked()) {
				SurveyTimingsRecorder.recordAnswer(selectedButton.getText().toString(), questionDescription);
			}
			else {
				/* It should not be possible to un-check a radio button, but if
				 * that happens, record the answer as an empty string */
				SurveyTimingsRecorder.recordAnswer("", questionDescription);
			}
		}
	}
	
	
	/** Listens for a touch/answer to a Checkbox Question, and records the answer */
	public class CheckboxListener implements OnClickListener {

		QuestionDescription questionDescription;
		
		public CheckboxListener(QuestionDescription questionDescription) {
			this.questionDescription = questionDescription;
		}
		
		@Override
		public void onClick(View view) {
			// If it's a CheckBox and its parent is a LinearLayout
			if ((view instanceof CheckBox) && (view.getParent() instanceof LinearLayout)) {
				LinearLayout checkboxesList = (LinearLayout) view.getParent();
				String answersList = getSelectedCheckboxes(checkboxesList);
				SurveyTimingsRecorder.recordAnswer(answersList, questionDescription);
			}				
		}		
	}
	
	
	/** Listens for an input/answer to an Open/Free Response Question, and records the answer */
	public class OpenResponseListener implements OnFocusChangeListener {

		QuestionDescription questionDescription;
		
		public OpenResponseListener(QuestionDescription questionDescription) {
			this.questionDescription = questionDescription;
		}
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				// The user just selected the input box
				// TODO: figure out if JP wants us to record this event

				// Set the EditText so that if the user taps outside, the keyboard disappears
				if (v instanceof EditText) {
					TextFieldKeyboard keyboard = new TextFieldKeyboard(appContext);
					keyboard.makeKeyboardBehave((EditText) v);
				}
			}
			else {
				// The user just selected away from the input box
				if (v instanceof EditText) {
					EditText textField = (EditText) v;
					String answer = textField.getText().toString();
					SurveyTimingsRecorder.recordAnswer(answer, questionDescription);
				}
			}			
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
