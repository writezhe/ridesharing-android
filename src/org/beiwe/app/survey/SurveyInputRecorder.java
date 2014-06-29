package org.beiwe.app.survey;

import android.util.Log;
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

public class SurveyInputRecorder {

	// TODO: create a function in this class that escapes commas, carriage returns/newlines, quotes, apostrophes, etc. from text strings
	
	
	/** Listens for a touch/answer to a Slider Question, and records the answer */
	public class SliderListener implements OnSeekBarChangeListener {

		String questionID;
		String questionText;
		
		public SliderListener(String questionID, String questionText) {
			this.questionID = questionID;
			this.questionText = questionText;
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.i("Slider Recorder", "Start tracking SeekBar on Question #" + questionID + " with text: " + questionText);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			Log.i("Slider Recorder", "User selected " + progress + " on Question #" + questionID + " with text: " + questionText);
		}
		
	}
	
	
	/** Listens for a touch/answer to a Radio Button Question, and records the answer */
	public class RadioButtonListener implements OnCheckedChangeListener {

		String questionID;
		String questionText;
		
		public RadioButtonListener(String questionID, String questionText) {
			this.questionID = questionID;
			this.questionText = questionText;
		}
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			RadioButton selectedButton = (RadioButton) group.findViewById(checkedId);
			if (selectedButton.isChecked()) {
				Log.i("RadioButton Recorder", "Selected " + selectedButton.getText() + " for Question #" + questionID + " with text: " + questionText);
			}
			else {
				Log.i("RadioButton Recorder", "Dunno why, but " + selectedButton.getText() + "is apparently no longer checked");					
			}			
		}		
	}
	
	
	/** Listens for a touch/answer to a Checkbox Question, and records the answer */
	public class CheckboxListener implements OnClickListener {

		String questionID;
		String questionText;
		
		public CheckboxListener(String questionID, String questionText) {
			this.questionID = questionID;
			this.questionText = questionText;
		}
		
		@Override
		public void onClick(View view) {

			// If it's a CheckBox and its parent is a LinearLayout
			if ((view instanceof CheckBox) && (view.getParent() instanceof LinearLayout)) {
				
				String checkedOptionsList = "";
				
				// Iterate over the whole list of CheckBoxes in this LinearLayout
				LinearLayout checkboxesList = (LinearLayout) view.getParent();
				for (int i = 0; i < checkboxesList.getChildCount(); i++) {

					View childView = checkboxesList.getChildAt(i);
					if (childView instanceof CheckBox) {
						CheckBox checkBox = (CheckBox) childView;
						
						// If this CheckBox is selected, add it to the list of selected answers
						if (checkBox.isChecked()) {
							checkedOptionsList += "'" + checkBox.getText() + "' & ";
						}
					}
				}
				
				Log.i("Checkbox Recorder", "Selected: " + checkedOptionsList + " on question #" + questionID + " with text: " + questionText);
			}
		}		
	}
	
	
	/** Listens for an input/answer to an Open/Free Response Question, and records the answer */
	public class OpenResponseListener implements OnFocusChangeListener {

		String questionID;
		String questionText;
		
		public OpenResponseListener(String questionID, String questionText) {
			this.questionID = questionID;
			this.questionText = questionText;
		}
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				// The user just selected the input box
				Log.i("FreeResponse Recorder", "just gained focus");
			}
			else {
				// The user just selected away from the input box
				if (v instanceof EditText) {
					EditText textField = (EditText) v;
					String answer = textField.getText().toString();
					Log.i("FreeResponse Recorder", "USER ENTERED ANSWER = " + answer + " for question #" + questionID + " with text: " + questionText);
				}
			}			
		}
	}

}
