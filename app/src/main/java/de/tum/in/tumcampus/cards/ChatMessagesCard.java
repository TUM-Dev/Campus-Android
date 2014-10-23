package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;

import de.tum.in.tumcampus.activities.ChatActivity;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CHAT;

/**
 * Card that shows the cafeteria menu
 */
public class ChatMessagesCard extends Card {
    private ArrayList<ChatMessage> mUnread;
    private ChatMessageManager manager;
    private String mRoomName;
    private int mRoomId;
    private String mRoomIdString;
    private ChatMember mMember;

    public ChatMessagesCard(Context context) {
        super(context, "card_chat");
    }

    @Override
    public int getTyp() {
        return CARD_CHAT;
    }

    @Override
    public String getTitle() {
        return mRoomName;
    }

    @Override
    public View getCardView(Context context, ViewGroup parent) {
        super.getCardView(context, parent);

        // Show cafeteria menu
        for(ChatMessage message : mUnread) {
            addTextView(message.getMember().getDisplayName()+": "+message.getText());
        }
        return mCard;
    }

    /**
     * Sets the information needed to build the card
     * @param roomName Name of the chat room
     * @param roomId Id of the chat room
     */
    public void setChatRoom(String roomName, int roomId, String roomIdString, ChatMember member) {
        mRoomName = roomName;
        mRoomName = mRoomName.replaceAll("[A-Z, 0-9(LV\\.Nr\\.)=]+$", "");
        mRoomName = mRoomName.replaceAll("\\([A-Z]+[0-9]+\\)", "");
        mRoomName = mRoomName.replaceAll("\\[[A-Z]+[0-9]+\\]", "");
        mRoomName = mRoomName.trim();
        manager = new ChatMessageManager(mContext, roomId);
        mUnread = manager.getLastUnread();
        mRoomIdString = roomIdString;
        mRoomId = roomId;
        mMember = member;
    }

    @Override
    public Intent getIntent() {
        Bundle bundle = new Bundle();
        final Intent intent = new Intent(mContext, ChatActivity.class);
        ChatRoom currentChatRoom = new ChatRoom(mRoomIdString);
        currentChatRoom.setId(mRoomId);
        intent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(currentChatRoom));
        intent.putExtra(Const.CURRENT_CHAT_MEMBER, new Gson().toJson(mMember));
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void discard(Editor editor) {
        manager.markAsRead();
    }

    @Override
    boolean shouldShowNotification(SharedPreferences prefs) {
        return false;
    }
}
