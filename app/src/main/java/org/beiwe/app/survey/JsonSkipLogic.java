package org.beiwe.app.survey;

import android.annotation.SuppressLint;
import android.util.Log;

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

public class JsonSkipLogic {
	//Comparator sets
	private static Set<String> ALL_COMPARATORS = new HashSet<String>(6);
	private static Set<String> NUMERIC_COMPARATORS = new HashSet<String>(4);
	private static Set<String> EXACTNESS_COMPARATORS = new HashSet<String>(6);
	private static Set<String> BOOLEAN_OPERATORS = new HashSet<String>(2);
	static {
		NUMERIC_COMPARATORS.add("<"); //Setup numerics
		NUMERIC_COMPARATORS.add(">");
		NUMERIC_COMPARATORS.add("<=");
		NUMERIC_COMPARATORS.add(">=");
		EXACTNESS_COMPARATORS.add("=="); //Setup == / !=
		EXACTNESS_COMPARATORS.add("!=");
		BOOLEAN_OPERATORS.add("and"); //Setup boolean operators
		BOOLEAN_OPERATORS.add("or");
		ALL_COMPARATORS.addAll(NUMERIC_COMPARATORS); //stick all of them into the everything collection
		ALL_COMPARATORS.addAll(EXACTNESS_COMPARATORS);
//		ALL_COMPARATORS.addAll(BOOLEAN_OPERATORS);
		ALL_COMPARATORS.add("not"); //and add "not", as it is its own thing and doesn't have a collection.
		BOOLEAN_OPERATORS = Collections.unmodifiableSet(BOOLEAN_OPERATORS); //Block modification of all the sets.
		NUMERIC_COMPARATORS = Collections.unmodifiableSet(NUMERIC_COMPARATORS);
		EXACTNESS_COMPARATORS = Collections.unmodifiableSet(EXACTNESS_COMPARATORS);
		ALL_COMPARATORS = Collections.unmodifiableSet(ALL_COMPARATORS);
	}

	//is the string formatter
	private static final String NUMERICAL_STRING_REPRESENTATION = "%.1f";

	private HashMap<String, String> QuestionAnswer;
	private HashMap<String, JSONObject> QuestionSkipLogic;
	private ArrayList<String> QuestionOrder;
	private Integer currentQuestion;

	public JsonSkipLogic(JSONArray jsonQuestions) throws JSONException {
		int max_size = jsonQuestions.length();

		//Locals
		String questionId;
		JSONObject question;

		//construct the various question id collections
		QuestionAnswer = new HashMap<String, String> (max_size);
		QuestionSkipLogic = new HashMap<String, JSONObject> (max_size);
		QuestionOrder = new ArrayList<String> (max_size);

		for (int i = 0; i < max_size; i++) {
			question = jsonQuestions.optJSONObject(i); //line can throw JSONException
			questionId = question.getString("question_id");

			QuestionOrder.add(questionId); //get question order

			//TODO: if a question has no skip logic it is always displayed
			//TODO: also handle empty skip logic object case.

			if ( question.has("display_if") ) {
				Log.i("json display_if", ": " + question.getString("display_if"));
				QuestionSkipLogic.put(questionId, question.getJSONObject("display_if"));
			}
		}

		currentQuestion = -1; //set the current question to -1, makes getNextQuestionID less annoying.
	}

	public String getNextQuestionID() {
		currentQuestion++;
		String questionId;
		//if cannot access, return null, we are done with survey.
		try{ questionId = QuestionOrder.get(currentQuestion);
		} catch (IndexOutOfBoundsException e) { return null; }

		// if questionId does not have skip logic we display it.
		if ( !QuestionSkipLogic.containsKey(questionId) ) {
			return questionId;
		}

		Boolean display;
		display = shouldQuestionDisplay(questionId);

		if ( !display ) {
			return getNextQuestionID();
		}
		else { return questionId; }
	}

	public String getQuestionAnswer(String questionId){ return QuestionAnswer.get(questionId); }

	private Boolean shouldQuestionDisplay(String questionId){
		try {
			return parseLogicTree(questionId, QuestionSkipLogic.get(questionId));
		} catch (JSONException e) {
			Log.e("json exception while doing a logic parse", "blah");
			e.printStackTrace();
		}
		throw new NullPointerException("bad json parsing");
	}

	private Boolean parseLogicTree(String questionId, JSONObject logic ) throws JSONException {
		// extract the comparator, force it lower case.
		// note that logic.getXXX(comparator_as_key) is the only way to grab the value due to strong typing
		String comparator = logic.keys().next().toLowerCase();

		//We get not out of the way first as it does not have a dictionary check.
		if ( comparator.equals("not") ) {
			//we need to pass in the Json _Object_ of the next layer in
			return !parseLogicTree(questionId, logic.getJSONObject(comparator));
		}

		if ( NUMERIC_COMPARATORS.contains(comparator) ) {
			//For numeric operations we need to interpret the value as a number (a double)
			return doNumericLogic(comparator,
					Double.valueOf(logic.getString(comparator)), //compare value, as a double
					Double.valueOf( QuestionAnswer.get(questionId)) ); //user answer, as a double
		}

		if ( EXACTNESS_COMPARATORS.contains(comparator) ) {
			return doNonNumericLogic(comparator,
					logic.getString(comparator), //compare value, as string
					QuestionAnswer.get(questionId) ); //user answer, as string
		}

		if ( BOOLEAN_OPERATORS.contains(comparator) ) {
			//get array, iterate over array, get the booleans into a list
			JSONArray more_logic = logic.getJSONArray(comparator);
			List<Boolean> results = new ArrayList<Boolean>(more_logic.length());

			for (int i = 0; i < more_logic.length(); i++) { //jsonArrays are not iterable...
			    results.add( parseLogicTree(questionId, more_logic.getJSONObject(i) ) );
			}

			//And. if anything is false, return false. If those all pass, return true.
			if ( comparator.equals("and") ) {
				for (Boolean bool : results) { if ( !bool) { return false; } }
				return true;
			}

			//Or. if anything is true, return true. If those all pass, return false.
			if ( comparator.equals("or") ) {
				for (Boolean bool : results) { if ( bool) { return true; } }
				return false;
			}
		}

		Log.e("json logic parser", "received invalid comparator: " + comparator);
		throw new NullPointerException("received invalid comparator: " + comparator);
	}

	private Boolean doNonNumericLogic(String comparator, String value, String targetQuestionId){
		String userAnswer = QuestionAnswer.get(targetQuestionId);
		boolean ret = value.equals(userAnswer);
		if ( comparator.equals("==")) { return ret; }
		if ( comparator.equals("!=")) { return !ret; }
		throw new NullPointerException("non numeric logic fail");
	}

	private Boolean doNumericLogic(String comparator, Double compareValue, Double userAnswer){
		if ( comparator.equals("<") ) { return compareValue < userAnswer; }
		if ( comparator.equals(">") ) { return compareValue > userAnswer; }
  		if ( comparator.equals("<=") ) { return compareValue <= userAnswer; }
		if ( comparator.equals(">=") ) { return compareValue >= userAnswer; }
		throw new NullPointerException("numeric logic fail");
	}

	public void setQuestionAnswer(String questionId, String answer){ QuestionAnswer.put(questionId, answer); }

	//TODO: These need testing...
	@SuppressLint("DefaultLocale")
	public void setQuestionAnswer(String questionId, Integer answer){ setQuestionAnswer(questionId, String.format(NUMERICAL_STRING_REPRESENTATION, answer)); }
	@SuppressLint("DefaultLocale")
	public void setQuestionAnswer(String questionId, Double answer){ setQuestionAnswer(questionId, String.format(NUMERICAL_STRING_REPRESENTATION, answer)); }
}
