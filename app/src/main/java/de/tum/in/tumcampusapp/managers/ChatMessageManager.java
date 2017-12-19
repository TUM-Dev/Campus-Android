package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dataAccessObjects.ChatMessageDao;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatMessageManager  {

    public static final int COL_ID = 0;
    public static final int COL_PREVIOUS = 1;
    public static final int COL_ROOM = 2;
    public static final int COL_TEXT = 3;
    public static final int COL_TIMESTAMP = 4;
    public static final int COL_SIGNATURE = 5;
    public static final int COL_MEMBER = 6;
    public static final int COL_READ = 7;
    public static final int COL_SENDING = 8;
    public static final int COL_MSGID = 9;

    private final int mChatRoom;
    private final Context mContext;
    private final ChatMessageDao chatMessageDao;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatMessageManager(Context context, int room) {
        mContext = context;
        mChatRoom = room;
        TcaDb tcaDb = TcaDb.getInstance(context);
        chatMessageDao = tcaDb.chatMessageDao();
        chatMessageDao.deleteOldEntries();
    }

    public static ChatMessage toObject(Cursor cursor) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        int id = cursor.getInt(COL_ID);
        String text = cursor.getString(COL_TEXT);
        String time = formatter.format(Utils.getISODateTime(cursor.getString(COL_TIMESTAMP)));
        int previous = cursor.getInt(COL_PREVIOUS);
        ChatMember member = new Gson().fromJson(cursor.getString(COL_MEMBER), ChatMember.class);
        ChatMessage msg = new ChatMessage(id, text, member, time, previous);
        msg.setSignature(cursor.getString(COL_SIGNATURE));
        msg.setRoom(cursor.getInt(COL_ROOM));
        msg.setRead(cursor.getInt(COL_READ) == 1);
        msg.setSendingStatus(cursor.getInt(COL_SENDING));
        msg.internalID = cursor.getInt(COL_MSGID);
        return msg;
    }

    /**
     * Gets all messages for the room
     *
     * @return List of chat messages
     */
    public Cursor getAll() {
        markAsRead();
        return chatMessageDao.getAll(mChatRoom);
    }

    public void markAsRead()    {
        chatMessageDao.markAsRead(mChatRoom);
    }

    /**
     * Gets all unsent chat messages from the current room
     */
    public List<ChatMessage> getAllUnsent() {
        return chatMessageDao.getAllUnsentFromCurrentRoom();
    }

    /**
     * Saves the given message into database
     */
    public void addToUnsent(ChatMessage m) {
        //TODO handle message with already set id
        Utils.logv("replace into unsent " + m.getText() + " " + m.getId() + " " + m.getPrevious() + " " + m.getSendingStatus());
        m.setRoom(mChatRoom);
        chatMessageDao.addToUnsent(m);
    }

    /**
     * Removes the message from unsent database
     */
    public void removeUnsentMessage(ChatMessage message) {
        chatMessageDao.removeUnsentMessage(message.internalID);
    }


    /**
     * Gets all unread chat messages
     */
    public List<ChatMessage> getLastUnread() {
        return chatMessageDao.getLastUnread(mChatRoom);
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(ChatMessage m, int memberId) {
        if (m == null || m.getText() == null) {
            Utils.log("Message empty");
            return;
        }

        Utils.logv("replace " + m.getText() + " " + m.getId() + " " + m.getPrevious() + " " + m.getSendingStatus());

        // Query read status from the previous message and use this read status as well if it is "0"
        boolean read = memberId == m.getMember()
                                    .getId();
        int status = chatMessageDao.getRead(m.getId());
        if (status == 1) {
            read = true;
        }
        m.setSendingStatus(ChatMessage.STATUS_SENT);
        m.setRead(read);
        replaceMessage(m);
    }

    public void replaceMessage(ChatMessage m) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date date;
        try {
            date = formatter.parse(m.getTimestamp());
        } catch (ParseException e) {
            date = new Date();
        }
        m.setTimestamp(Utils.getDateTimeString(date));
        chatMessageDao.replaceMessage(m);
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(List<ChatMessage> m) {
        ChatMember member = Utils.getSetting(mContext, Const.CHAT_MEMBER, ChatMember.class);

        if (member == null) {
            return;
        }

        for (ChatMessage msg : m) {
            replaceInto(msg, member.getId());
        }
    }

    public Cursor getNewMessages(ChatMember member, int messageId) throws NoPrivateKey, IOException {
        List<ChatMessage> messages;
        if (messageId == -1) {
            messages = TUMCabeClient.getInstance(mContext)
                                    .getNewMessages(mChatRoom, ChatVerification.Companion.getChatVerification(mContext, member));
        } else {
            messages = TUMCabeClient.getInstance(mContext)
                                    .getMessages(mChatRoom, messageId, ChatVerification.Companion.getChatVerification(mContext, member));
        }
        replaceInto(messages);
        return chatMessageDao.getUnread(mChatRoom);
    }
}