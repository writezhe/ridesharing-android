package org.beiwe.app.survey;

/**
 * This class makes it easier to pass around the description of a question so
 * that it can be recorded in Survey Answers and Survey Timings files.
 * That way, instead of just recording "user answered '7' to question #5", the
 * Survey Answers and Survey Timings files record something along the lines of
 * "user answered '7' to question #5, which asked 'how many hours of sleep did
 * you get last night' and had a numeric input field, with options..."
 */

public class QuestionData {
	private String id = null;
	private QuestionType.Type type = null;
	private String text = null;
	private String options = null;
	private String answerString = null;
	private Integer answerIntegerValue = null;
	private Double answerDoubleValue = null;

	public QuestionData(String id, QuestionType.Type type, String text, String options) {
		this.setId(id);
		this.setType(type);
		this.setText(text);
		this.setOptions(options); //this is formatting for the csv
	}
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public QuestionType.Type getType() { return type; }
	public void setType(QuestionType.Type type) { this.type = type; }
	
	public String getText() { return text; }
	public void setText(String text) { this.text = text; }
	
	public String getOptions() { return options; }
	public void setOptions(String options) { this.options = options; }

	public String getAnswerString() { return answerString; }
	public void setAnswerString(String answerString) { this.answerString = answerString; }

	public Integer getAnswerIntegerValue() { return answerIntegerValue; }
	public void setAnswerIntegerValue(Integer answerIntegerValue) { this.answerIntegerValue = answerIntegerValue; }

	public Double getAnswerDoubleValue() { return answerDoubleValue ; }
	public void setAnswerDoubleValue(Double answerDoubleValue) { this.answerDoubleValue  = answerDoubleValue; }
}
