package de.tum.in.tumcampusapp.models.tumcabe;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "unsent_chat_message")
public class UnsentChatMessage {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int id;
    private String text;
    private ChatMember member;
    private int room;
    @ColumnInfo(name = "msg_id")
    public int internalID;


    /**
     * Called when creating a new chat message
     *
     * @param text   Chat message text
     * @param member Member who sent the message
     */
    public UnsentChatMessage(String text, ChatMember member, int room, int internalID) {
        super();
        this.text = text;
        this.member = member;
        this.room = room;
        this.internalID = internalID;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

}
