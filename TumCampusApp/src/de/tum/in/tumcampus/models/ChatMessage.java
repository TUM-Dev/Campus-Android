package de.tum.in.tumcampus.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ChatMessage {

	private String url;
	private String text;
	private String member;
	private Date timestamp;
	private String signature;
	private boolean valid;
	
	public ChatMessage(String text, String member/*, Date timestamp*/) {
		super();
		this.text = text;
		this.member = member;
		/*this.timestamp = timestamp;*/
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getMember() {
		return member;
	}
	public void setMember(String member) {
		this.member = member;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getTimestampString() {
		return new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(timestamp);
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
