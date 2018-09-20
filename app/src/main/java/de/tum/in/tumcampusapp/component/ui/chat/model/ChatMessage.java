package de.tum.in.tumcampusapp.component.ui.chat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;

@Entity(tableName = "chat_message")
public class ChatMessage implements Parcelable {

    public static final int STATUS_SENT = 0;
    public static final int STATUS_SENDING = 1;
    public static final int STATUS_ERROR = 2;

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int id;

    private int previous;
    private int room;
    private String text;
    private String timestamp;

    private String signature;
    private ChatMember member;

    @ColumnInfo(name = "sending")
    private int sendingStatus;

    /**
     * Default constructor: called by gson when parsing an element
     */
    @Ignore
    public ChatMessage() {
        this.sendingStatus = STATUS_SENDING;
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
        this.timestamp = DateTimeUtils.INSTANCE.getDateTimeString(DateTime.now());
    }

    @Ignore
    public ChatMessage(int id, String text, ChatMember member, DateTime timestamp, int previous) {
        super();
        this.id = id;
        this.text = text;
        this.member = member;
        this.timestamp = DateTimeUtils.INSTANCE.getDateTimeString(timestamp);
        this.sendingStatus = STATUS_SENDING;
        this.previous = previous;
    }

    protected ChatMessage(Parcel in) {
        id = in.readInt();
        previous = in.readInt();
        room = in.readInt();
        text = in.readString();
        timestamp = in.readString();
        signature = in.readString();
        member = in.readParcelable(ChatMember.class.getClassLoader());
        sendingStatus = in.readInt();
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

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

    public String getTimestamp() {
        return timestamp;
    }

    public DateTime getDateTime() {
        return DateTimeUtils.INSTANCE.getDate(timestamp);
    }

    public String getFormattedTimestamp(Context context) {
        return DateTimeUtils.INSTANCE.formatTimeOrDay(getDateTime(), context);
    }

    public void setTimestamp(String timestamp) {
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
    public boolean isNewMessage() {
        return id == 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeInt(previous);
        parcel.writeString(text);
        parcel.writeString(timestamp);
        parcel.writeString(signature);
        parcel.writeParcelable(member, flags);
        parcel.writeInt(sendingStatus);
    }

}
