package de.tum.in.tumcampus.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampus.auxiliary.Utils;

@SuppressWarnings("UnusedDeclaration")
public class CreateChatMessage {

    private int id;
	private String url;
	private String text;
	private String member;
	private String timestamp;
	private String signature;
	private boolean valid;
	
	public CreateChatMessage(String text, String member) {
		this.text = text;
		this.member = member;
	}

    public int getId() {
        return id;
    }
    public void setId(int i) {
        id = i;
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
