package org.beiwe.app.survey;

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
