package de.tum.in.tumcampus.models;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;


@SuppressWarnings("UnusedDeclaration")
public class ChatMessage {

    public static final int STATUS_SENDING = 1;
    public static final int STATUS_SENT = 0;
    public static final int STATUS_SENDING_FAILED = -1;

    private int id;
	private String text;
	private ChatMember member;
	private String timestamp;
	private String signature;
    private int sendingStatus;
    private int previous;
	
	public ChatMessage(String text) {
		super();
		this.text = text;
	}

    /**
     * Called when creating a new chat message
     * @param text
     * @param member
     */
    public ChatMessage(String text, ChatMember member) {
        super();
        this.text = text;
        this.member = member;
        this.sendingStatus = STATUS_SENDING;
        this.previous = 0;
        this.setNow();
    }

    public int getStatus() {
        return sendingStatus;
    }

    public void setStatus(int status) {
        sendingStatus = status;
    }

	public ChatMessage(int id, String text, ChatMember member, String timestamp, int previous) {
		super();
        this.id = id;
		this.text = text;
		this.member = member;
		this.timestamp = timestamp;
        this.sendingStatus = STATUS_SENT;
        this.previous = previous;
	}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getPrevious() {
        return previous;
    }
    public void setPrevious(int previous) {
        this.previous = previous;
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
    public Date getTimestampDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date time;
        try {
            time = formatter.parse(this.getTimestamp());
        } catch (ParseException e) {
            Utils.log(e);
            time = new Date();
        }
        return time;
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

    public int getStatusStringRes() {
        if (sendingStatus==STATUS_SENT) {
            return R.string.status_sent;
        } else if (sendingStatus==STATUS_SENDING) {
            return R.string.status_sending;
        } else {
            return R.string.status_sending_failed;
        }
    }

    public void setNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH); // 2014-06-30T16:31:57.878Z
        timestamp = formatter.format(new Date());
    }
}
