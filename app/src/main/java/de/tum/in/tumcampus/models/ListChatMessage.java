package de.tum.in.tumcampus.models;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampus.auxiliary.Utils;


@SuppressWarnings("UnusedDeclaration")
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
	
	public ListChatMessage(String url, String text, ChatMember member, String timestamp) {
		super();
		this.url = url;
		this.text = text;
		this.member = member;
		this.timestamp = timestamp;
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
	public String getTimestampString(Context context) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH); // 2014-06-30T16:31:57.878Z
			Date date = formatter.parse(timestamp);
            return DateUtils.getRelativeDateTimeString(context, date.getTime(),
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS*2, 0).toString();
		} catch (Exception e) {
			Utils.log(e);
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

        return today.get(Calendar.YEAR) == passedDate.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == passedDate.get(Calendar.DAY_OF_YEAR);
    }
	
	private boolean isYesterday(Date date) {
		Calendar passedDate = Calendar.getInstance();
		passedDate.setTime(date);
		
		Calendar yesterday = Calendar.getInstance(); // today
		yesterday.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        return yesterday.get(Calendar.YEAR) == passedDate.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == passedDate.get(Calendar.DAY_OF_YEAR);
    }
}
