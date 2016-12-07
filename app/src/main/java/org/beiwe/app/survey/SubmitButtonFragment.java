package org.beiwe.app.survey;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.beiwe.app.R;

import java.util.ArrayList;

/**
 * Created by admin on 12/10/16.
 */

public class SubmitButtonFragment extends Fragment {
    OnSubmitButtonClickedListener submitButtonClickedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout surveySubmitLayout = (LinearLayout) inflater.inflate(R.layout.fragment_submit_button, null);

        // Set an onClickListener on the Submit Answers button
        Button submitButton = (Button) surveySubmitLayout.findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButtonClickedListener.submitButtonClicked();
            }
        });

        // If any questions weren't answered, display a message about them
        ArrayList<String> unansweredQuestions = getArguments().getStringArrayList("unansweredQuestions");
        hideOrDisplayUnansweredQuestionsView(unansweredQuestions, surveySubmitLayout, submitButton);

        return surveySubmitLayout;
    }


    // Interface for the "Next" button to signal the Activity
    public interface OnSubmitButtonClickedListener {
        public void submitButtonClicked();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        submitButtonClickedListener = (OnSubmitButtonClickedListener) context;
    }


    private void hideOrDisplayUnansweredQuestionsView(ArrayList<String> unansweredQuestions,
                                                      LinearLayout surveySubmitLayout,
                                                      Button submitButton) {
        // If there are any unanswered questions
        if (unansweredQuestions.size() > 0) {
            // Show a message about the number of unanswered questions
            TextView unansweredQuestionsMessage = (TextView) surveySubmitLayout.findViewById(R.id.unansweredQuestionsExplanation);
            if (unansweredQuestions.size() == 1) {
                unansweredQuestionsMessage.setText("You did not answer 1 question:");
            } else {
                unansweredQuestionsMessage.setText("You did not answer " + unansweredQuestions.size() + " questions:");
            }
            // Show a list of the unanswered questions
            ListView unansweredQuestionsListView = (ListView) surveySubmitLayout.findViewById(R.id.unansweredQuestionsList);
            ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, unansweredQuestions);
            unansweredQuestionsListView.setAdapter(adapter);
            // Change the text of the submit button
            submitButton.setText("Submit Answers Anyway");
        } else {  // If there aren't any unanswered questions
            // Hide the unanswered questions display
            LinearLayout unansweredQuestionsMessage = (LinearLayout) surveySubmitLayout.findViewById(R.id.unansweredQuestionsMessage);
            unansweredQuestionsMessage.setVisibility(View.GONE);
        }
    }
}