package org.beiwe.app.survey;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.beiwe.app.CrashHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Created by elijones on 12/1/16. */

//TODO: implement a serialization and/or iterable output blob.  need to see what survey anwsers actually look like to determine all the values it needs

public class JsonSkipLogic {
	//Comparator sets
	private static Set<String> NUMERIC_COMPARATORS = new HashSet<String>(4);
	private static Set<String> EQUALITY_COMPARATORS = new HashSet<String>(6);
	private static Set<String> BOOLEAN_OPERATORS = new HashSet<String>(2);
	static {
		NUMERIC_COMPARATORS.add("<"); //Setup numerics
		NUMERIC_COMPARATORS.add(">");
		NUMERIC_COMPARATORS.add("<=");
		NUMERIC_COMPARATORS.add(">=");
		EQUALITY_COMPARATORS.add("=="); //Setup == / !=
		EQUALITY_COMPARATORS.add("!=");
		BOOLEAN_OPERATORS.add("and"); //Setup boolean operators
		BOOLEAN_OPERATORS.add("or");
		BOOLEAN_OPERATORS = Collections.unmodifiableSet(BOOLEAN_OPERATORS); //Block modification of all the sets.
		NUMERIC_COMPARATORS = Collections.unmodifiableSet(NUMERIC_COMPARATORS);
		EQUALITY_COMPARATORS = Collections.unmodifiableSet(EQUALITY_COMPARATORS);
	}

	//is the string formatter
	private static final String NUMERICAL_STRING_REPRESENTATION = "%.1f";

	private HashMap<String, String> QuestionAnswer;
	private HashMap<String, JSONObject> QuestionSkipLogic;
	private ArrayList<String> QuestionOrder;
	private Integer currentQuestion;
	private Boolean runDisplayLogic;
	private Context appContext;

	public JsonSkipLogic(JSONArray jsonQuestions, Boolean runDisplayLogic, Context applicationContext) throws JSONException {
		appContext = applicationContext;
		int max_size = jsonQuestions.length();
		String questionId;
		JSONObject question;
		JSONObject display_logic;

		//construct the various question id collections
		QuestionAnswer = new HashMap<String, String> (max_size);
		QuestionSkipLogic = new HashMap<String, JSONObject> (max_size);
		QuestionOrder = new ArrayList<String> (max_size);

		for (int i = 0; i < max_size; i++) {
			question = jsonQuestions.optJSONObject(i); //line can throw JSONException
			questionId = question.getString("question_id");

			QuestionOrder.add(questionId); //get question order
			if ( question.has("display_if") ) {
				Log.v("debugging json content", " " + question.toString() );
				display_logic = question.optJSONObject("display_if");
				if (display_logic != null) {
					QuestionSkipLogic.put(questionId, display_logic);
				}
			}
		}
		this.runDisplayLogic = runDisplayLogic;
		currentQuestion = -1; //set the current question to -1, makes getNextQuestionID less annoying.
	}

	/** Determines question should be displayed next.
	 * @return a question id string, null if there is no next item. */
	@SuppressWarnings("TailRecursion")
	public String getNextQuestionID() {
		currentQuestion++;
		//if we would overflow the list (>= size) we are done, return null.
		if (currentQuestion >= QuestionOrder.size()) {
			Log.w("json logic", "overflowed...");
			return null; }
		//if display logic has been disabled we skip logic processing and return the next question
		if (!runDisplayLogic) {
			Log.d("json logic", "runDisplayLogic set to true! doing all questions!");
			return QuestionOrder.get(currentQuestion); }

		String questionId = QuestionOrder.get(currentQuestion);
		Log.v("json logic", "starting question " + QuestionOrder.indexOf(questionId) + " (" + questionId + "))");
		// if questionId does not have skip logic we display it.
		//TODO: handle skip logic empty as identical to not existing
		if ( !QuestionSkipLogic.containsKey(questionId) ) {
			Log.d("json logic", "Question " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") has no skip logic, done.");
			return questionId;
		}
		if ( shouldQuestionDisplay(questionId) ) {
			Log.d("json logic", "Question " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") evaluated as true, done.");
			return questionId;
		}
		else {
			Log.d("json logic", "Question " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") did not evaluate as true, proceeding to next question...");
			return getNextQuestionID();
		}
	}

	/** This function wraps the logic processing code.  If the logic processing encounters an error
	 * due to json parsing the behavior is to invariably return true.
	 * @param questionId
	 * @return Boolean result of the logic */
	private Boolean shouldQuestionDisplay(String questionId){
		try {
			return parseLogicTree(questionId, QuestionSkipLogic.get(questionId));
		} catch (JSONException e) {
			Log.w("json exception while doing a logic parse", "=============================================================================================================================================");
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext);
		}
		return true;
	}

	private Boolean parseLogicTree(String questionId, JSONObject logic ) throws JSONException {
		// extract the comparator, force it as lower case.
		String comparator = logic.keys().next().toLowerCase();

		// logic.getXXX(comparator_as_key) is the only way to grab the value due to strong typing.
		// This object has many uses, so the following logic has been written for explicit clarity,
		// rather than optimized code length or performance.

		//We'll get the NOT out of the way first.
		//todo: test not, it may not be in the reference.
		if ( comparator.equals("not") ) {
			//we need to pass in the Json _Object_ of the next layer in
			Log.d("json logic", "evaluating as not (invert)");
			JSONObject innerObject = logic.getJSONObject(comparator);
			return !parseLogicTree(questionId, innerObject);
		}

		if ( NUMERIC_COMPARATORS.contains(comparator) ) {
			// in this case logic.getString(comparator) contains a json list/array with the first
			// element being the referencing question ID, and the second being a value to compare to.
			Log.d("json logic", "evaluating as numeric");
			JSONArray parameters = logic.getJSONArray(comparator);
			return runNumericLogic(comparator, parameters);
		}

		if ( EQUALITY_COMPARATORS.contains(comparator) ) {
			//identical to numeric comparators but we need to call the runEqualityLogic in order
			// to avoid floating point direct equality checks.
			Log.d("json logic", "evaluating as equality");
			JSONArray parameters = logic.getJSONArray(comparator);
			return runEqualityLogic(comparator, parameters);
		}

		if ( BOOLEAN_OPERATORS.contains(comparator) ) {
			//get array, iterate over array, get the booleans into a list
			JSONArray manyLogics = logic.getJSONArray(comparator);
			Log.v("json logic", "evaluating as boolean, " + manyLogics.length() + " things to process...");
			List<Boolean> results = new ArrayList<Boolean>(manyLogics.length());
			for (int i = 0; i < manyLogics.length(); i++) { //jsonArrays are not iterable...
				results.add( parseLogicTree(questionId, manyLogics.getJSONObject(i) ) );
			} //results now contains the boolean evaluation of all nested logics.
			Log.v("json logic", "returning inside of " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") after processing logic for boolean.");
			//And. if anything is false, return false. If those all pass, return true.
			if ( comparator.equals("and") ) {
				for (Boolean bool : results) { if ( !bool ) { return false; } }
				return true;
			}
			//Or. if anything is true, return true. If those all pass, return false.
			if ( comparator.equals("or") ) {
				for (Boolean bool : results) { if ( bool ) { return true; } }
				return false;
			}
		}
		throw new NullPointerException("received invalid comparator: " + comparator);
	}

	/** Processes the logical operation implemented of a comparator, only for use with equality tests.
	 * If there has been no answer for the question a logic operation references this function returns false.
	 * @param comparator a string that is in the EQUALITY_COMPARATORS constant.
	 * @param parameters json array 2 elements in length.  The first element is a target question ID to pull an answer from, the second is the survey's value to compare to.
	 * @return Boolean result of the operation, or false if the referenced question has no answer.
	 * @throws JSONException */
	@SuppressLint("DefaultLocale")
	private Boolean runEqualityLogic(String comparator, JSONArray parameters ) throws JSONException {
		Log.d("json logic", "inside equality logic: " + comparator + ", " + parameters.toString());
		String targetQuestionId = parameters.getString(0);
		if ( !QuestionAnswer.containsKey(targetQuestionId) ) { return false; } //False if DNE
		String userAnswer = QuestionAnswer.get(targetQuestionId); //TODO: insert here no answer logic
		String surveyValue = String.format(NUMERICAL_STRING_REPRESENTATION, parameters.getDouble(1) );
		//we do equality comparison as a string comparison in order to avoid Double equality evaluation
		boolean ret = surveyValue.equals(userAnswer);
		if ( comparator.equals("==")) { return ret; }
		if ( comparator.equals("!=")) { return !ret; }
		throw new NullPointerException("non numeric logic fail");
	}

	/** Processes the logical operation implemented of a comparator, only for use with numerical
	 * comparisons (greater than, less than, plus the = varieties).
	 * If there has been no answer for the question a logic operation references this function returns false.
	 * @param comparator a string that is in the NUMERIC_COMPARATORS constant.
	 * @param parameters json array 2 elements in length.  The first element is a target question ID to pull an answer from, the second is the survey's value to compare to.
	 * @return Boolean result of the operation, or false if the referenced question has no answer.
	 * @throws JSONException */
	private Boolean runNumericLogic(String comparator, JSONArray parameters) throws JSONException {
		Log.d("json logic", "inside numeric logic: " + comparator + ", " + parameters.toString());
		String targetQuestionId = parameters.getString(0);
		if ( !QuestionAnswer.containsKey(targetQuestionId) ) { return false; } // false if DNE
		Double userAnswer = Double.valueOf( QuestionAnswer.get(targetQuestionId) ); //TODO: insert here no answer logic
		Double surveyValue = parameters.getDouble(1);
		//Fixme: this logic here could be backwards, need to think this through and document it.
		if ( comparator.equals("<") ) { return userAnswer < surveyValue; }
		if ( comparator.equals(">") ) { return userAnswer > surveyValue; }
  		if ( comparator.equals("<=") ) { return userAnswer <= surveyValue; }
		if ( comparator.equals(">=") ) { return userAnswer >= surveyValue; }
		throw new NullPointerException("numeric logic fail");
	}

	public void setQuestionAnswer(String questionId, String answer){ QuestionAnswer.put(questionId, answer); }

	//TODO: These need testing...
	@SuppressLint("DefaultLocale")
	public void setQuestionAnswer(String questionId, Integer answer){ setQuestionAnswer(questionId, String.format(NUMERICAL_STRING_REPRESENTATION, Double.valueOf(answer))); }
	@SuppressLint("DefaultLocale")
	public void setQuestionAnswer(String questionId, Double answer){ setQuestionAnswer(questionId, String.format(NUMERICAL_STRING_REPRESENTATION, answer)); }

	public void setAnswer(String questionId, QuestionData questiondata) {
		//TODO: implement
	}
}
