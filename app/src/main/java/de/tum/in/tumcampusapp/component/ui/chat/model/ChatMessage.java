package de.tum.in.tumcampusapp.component.ui.chat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.R;

@Entity(tableName = "chat_message")
public class ChatMessage {
    public static final int STATUS_SENDING = 1;
    public static final int STATUS_SENT = 0;
    //public static final int STATUS_SENDING_FAILED = -1;

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int id;
    private int previous;
    private int room;
    private String text;
    private DateTime timestamp;
    private String signature;
    private ChatMember member;
    @ColumnInfo(name = "sending")
    private int sendingStatus;

    /**
     * Default constructor: called by gson when parsing an element
     */
    @Ignore
    public ChatMessage() {
        this.sendingStatus = STATUS_SENT;
    }

    @Ignore
    public ChatMessage(String text) {
        super();
        this.text = text;
    }

    /**
     * Called when creating a new chat message
     *
     * @param text   ChatNotification message text
     * @param member Member who sent the message
     */
    public ChatMessage(String text, ChatMember member) {
        super();
        this.text = text;
        this.member = member;
        this.sendingStatus = STATUS_SENDING;
        this.previous = 0;
        this.timestamp = DateTime.now();
    }

    @Ignore
    public ChatMessage(int id, String text, ChatMember member, DateTime timestamp, int previous) {
        super();
        this.id = id;
        this.text = text;
        this.member = member;
        this.timestamp = timestamp;
        this.sendingStatus = STATUS_SENT;
        this.previous = previous;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public int getSendingStatus() {
        return sendingStatus;
    }

    public void setSendingStatus(int status) {
        sendingStatus = status;
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

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getStatusStringRes() {
        if (sendingStatus == STATUS_SENT) {
            return R.string.status_sent;
        } else if (sendingStatus == STATUS_SENDING) {
            return R.string.status_sending;
        } else {
            return R.string.status_sending_failed;
        }
    }
}
