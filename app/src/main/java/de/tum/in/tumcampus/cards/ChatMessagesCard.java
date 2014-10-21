package de.tum.in.tumcampus.cards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.tum.in.tumcampus.activities.ChatRoomsActivity;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.managers.ChatMessageManager;

import static de.tum.in.tumcampus.models.managers.CardManager.CARD_CHAT;

/**
 * Card that shows the cafeteria menu
 */
public class ChatMessagesCard extends Card {
    private ArrayList<ChatMessage> mUnread;
    private ChatMessageManager manager;
    private String mRoomName;

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
    public void setChatRoom(String roomName, int roomId) {
        mRoomName = roomName;
        manager = new ChatMessageManager(mContext, roomId);
        mUnread = manager.getLastUnread();
    }

    @Override
    public Intent getIntent() {
        return new Intent(mContext, ChatRoomsActivity.class);
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
