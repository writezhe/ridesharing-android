package org.beiwe.app.survey;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class TextFieldKeyboard {
	
	private Context appContext;

	public TextFieldKeyboard(Context applicationContext) {
		this.appContext = applicationContext;
	}
	
	
	/**
	 * Set up the UI so that if the user taps outside the current EditText, the
	 * software keyboard goes away
	 * @param editText the EditText the user is currently typing in
	 */
	public void makeKeyboardBehave(EditText editText) {
		View topParentView = getTopParentView(editText);
		setupUiToHideKeyboard(topParentView, editText);
	}
	

	// Gets the view's parent's parent's parent... and so on, until you find a parent-less view
	private View getTopParentView(View view) {
		ViewParent parent = view.getParent();
		if ((parent != null) && (parent instanceof View)) {
			return getTopParentView((View) parent);
		}
		else {
			return view;
		}
	}
	
	
    // If you tap anywhere but a text box (the Search box), hide the keyboard
    // Source: http://stackoverflow.com/a/11656129
    private void setupUiToHideKeyboard(View rootView, final EditText editText) {
        if(!(rootView instanceof EditText)) {
            rootView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(editText);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (rootView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
                View innerView = ((ViewGroup) rootView).getChildAt(i);
                setupUiToHideKeyboard(innerView, editText);
            }
        }
    }

	
	// Hide the keyboard and close the Search text box
    // Source: http://stackoverflow.com/a/11656129
    private void hideSoftKeyboard(EditText editText) {
    	InputMethodManager imm = (InputMethodManager) appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    	
    	editText.clearFocus();
    	View topParentView =getTopParentView(editText);
    	topParentView.requestFocus();
    }
    
}
