package de.tum.in.tumcampus.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {

	private String user;
	private String message;
	private Date timestamp;
	
	public ChatMessage(String user, String message, Date timestamp) {
		super();
		this.user = user;
		this.message = message;
		this.timestamp = timestamp;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
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
}
