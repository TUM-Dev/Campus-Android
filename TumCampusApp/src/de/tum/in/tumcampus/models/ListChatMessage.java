package de.tum.in.tumcampus.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ListChatMessage {

	private String url;
	private String text;
	private ChatMember member;
	private String timestamp;
	private String signature;
	private boolean valid;
	
	public ListChatMessage(String text) {
		super();
		this.text = text;
	}
	
	public ListChatMessage(CreateChatMessage newlyCreatedMessage, ChatMember currentChatMember) {
		this.text = newlyCreatedMessage.getText();
		this.timestamp = newlyCreatedMessage.getTimestamp();
		this.member = currentChatMember;
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
	public ChatMember getMember() {
		return member;
	}
	public void setMember(ChatMember member) {
		this.member = member;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getTimestampString() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH); // 2014-06-30T16:31:57.878Z
			Date date = formatter.parse(timestamp);
			if (isToday(date)) {
				return new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(date);
			} else if (isYesterday(date)) {
				return "Yesterday " + new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(date);
			}
			return new SimpleDateFormat("dd-mm-yyyy HH:mm", Locale.ENGLISH).format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
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
	
	private boolean isToday(Date date) {
		Calendar passedDate = Calendar.getInstance();
		passedDate.setTime(date); // your date
		
		Calendar today = Calendar.getInstance(); // today

		if (today.get(Calendar.YEAR) == passedDate.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == passedDate.get(Calendar.DAY_OF_YEAR)) {
			return true;
		}
		return false;
	}
	
	private boolean isYesterday(Date date) {
		Calendar passedDate = Calendar.getInstance();
		passedDate.setTime(date);
		
		Calendar yesterday = Calendar.getInstance(); // today
		yesterday.add(Calendar.DAY_OF_YEAR, -1); // yesterday

		if (yesterday.get(Calendar.YEAR) == passedDate.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == passedDate.get(Calendar.DAY_OF_YEAR)) {
			return true;
		}
		return false;
	}
}
