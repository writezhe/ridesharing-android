package org.beiwe.app.survey;

/**
 * This class makes it easier to pass around the description of a question so
 * that it can be recorded in Survey Answers and Survey Timings files.
 * That way, instead of just recording "user answered '7' to question #5", the
 * Survey Answers and Survey Timings files record something along the lines of
 * "user answered '7' to question #5, which asked 'how many hours of sleep did
 * you get last night' and had a numeric input field, with options..."
 */

public class QuestionDescription {
	private String id = null;
	private String type = null;
	private String text = null;
	private String options = null;

	public QuestionDescription(String id, String type, String text, String options) {
		this.setId(id);
		this.setType(type);
		this.setText(text);
		this.setOptions(options);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String getOptions() {
		return options;
	}
	public void setOptions(String options) {
		this.options = options;
	}

}
