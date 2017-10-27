package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatMessageManager extends AbstractManager {

    public static final int COL_ID = 0;
    public static final int COL_PREVIOUS = 1;
    public static final int COL_ROOM = 2;
    public static final int COL_TEXT = 3;
    public static final int COL_TIMESTAMP = 4;
    public static final int COL_SIGNATURE = 5;
    public static final int COL_MEMBER = 6;
    public static final int COL_READ = 7;
    public static final int COL_SENDING = 8;

    private final int mChatRoom;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatMessageManager(Context context, int room) {
        super(context);
        mChatRoom = room;
        init(db);
    }

    private static void init(SQLiteDatabase db) {
        // create tables if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_message (_id INTEGER PRIMARY KEY, previous INTEGER, room INTEGER, " +
                   "text TEXT, timestamp VARCHAR, signature TEXT, member BLOB, read INTEGER, sending INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS unsent_chat_message (_id INTEGER PRIMARY KEY AUTOINCREMENT, room INTEGER, text TEXT, member BLOB, msg_id INTEGER)");

        // Delete all entries that are too old
        db.rawQuery("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')", null)
          .close();
    }

    /**
     * Gets all unsent chat messages
     */
    public static List<ChatMessage> getAllUnsentUpdated(Context context) {
        SQLiteDatabase db = AbstractManager.getDb(context);
        init(db);
        List<ChatMessage> list;
        try (Cursor cur = db.rawQuery("SELECT member, text, room, msg_id, _id FROM unsent_chat_message ORDER BY _id", null)) {
            list = new ArrayList<>(cur.getCount());
            if (cur.moveToFirst()) {
                do {
                    ChatMember member = new Gson().fromJson(cur.getString(0), ChatMember.class);
                    ChatMessage msg = new ChatMessage(cur.getString(1), member);
                    msg.setRoom(cur.getInt(2));
                    msg.setId(cur.getInt(3));
                    msg.internalID = cur.getInt(4);
                    list.add(msg);
                } while (cur.moveToNext());
            }
        }
        return list;
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
        msg.setStatus(cursor.getInt(COL_SENDING));
        return msg;
    }

    /**
     * Gets all messages for the room
     *
     * @return List of chat messages
     */
    public Cursor getAll() {
        markAsRead();
        return db.rawQuery("SELECT c.* FROM chat_message c, (SELECT c1._id " +
                           "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                           "WHERE c2._id IS NULL AND c1.room=? " +
                           "ORDER BY c1._id DESC " +
                           "LIMIT 1) AS until " +
                           "WHERE c._id>=until._id AND c.room=? " +
                           "ORDER BY c._id", new String[]{String.valueOf(mChatRoom), String.valueOf(mChatRoom)});
    }

    public void markAsRead() {
        db.execSQL("UPDATE chat_message SET read=1 WHERE read=0 AND room=?", new String[]{String.valueOf(mChatRoom)});
    }

    /**
     * Gets all unsent chat messages from the current room
     */
    public List<ChatMessage> getAllUnsent() {
        List<ChatMessage> list;
        try (Cursor cur = db.rawQuery("SELECT member, text, room, _id FROM unsent_chat_message WHERE msg_id=0 ORDER BY _id", null)) {
            list = new ArrayList<>(cur.getCount());
            if (cur.moveToFirst()) {
                do {
                    ChatMember member = new Gson().fromJson(cur.getString(0), ChatMember.class);
                    ChatMessage msg = new ChatMessage(cur.getString(1), member);
                    msg.setRoom(cur.getInt(2));
                    msg.internalID = cur.getInt(3);
                    list.add(msg);
                } while (cur.moveToNext());
            }
        }
        return list;
    }

    /**
     * Saves the given message into database
     */
    public void addToUnsent(ChatMessage m) {
        //TODO handle message with already set id
        Utils.logv("replace into unsent " + m.getText() + " " + m.getId() + " " + m.getPrevious() + " " + m.getStatus());
        db.execSQL("REPLACE INTO unsent_chat_message (text,room,member,msg_id) VALUES (?,?,?, ?)",
                   new String[]{m.getText(), String.valueOf(mChatRoom), new Gson().toJson(m.getMember()), String.valueOf(m.getId())});
    }

    /**
     * Removes the message from unsent database
     */
    public void removeFromUnsent(ChatMessage message) {
        db.execSQL("DELETE FROM unsent_chat_message WHERE _id=?", new String[]{String.valueOf(message.internalID)});
    }

    /**
     * Gets all messages marked as unread
     */
    private Cursor getUnread() {
        return db.rawQuery("SELECT c.* FROM chat_message c, (SELECT c1._id " +
                           "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                           "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=? " +
                           "ORDER BY c1._id DESC " +
                           "LIMIT 1) AS until " +
                           "WHERE c._id>until._id AND c.room=? " +
                           "ORDER BY c._id", new String[]{String.valueOf(mChatRoom), String.valueOf(mChatRoom)});
    }

    /**
     * Gets all unread chat messages
     */
    public List<ChatMessage> getLastUnread() {
        List<ChatMessage> list;
        try (Cursor cur = db.rawQuery("SELECT c.member, c.text FROM chat_message c, (SELECT c1._id " +
                                      "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                                      "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=? " +
                                      "ORDER BY c1._id DESC " +
                                      "LIMIT 1) AS until " +
                                      "WHERE c._id>until._id AND c.room=? " +
                                      "ORDER BY c._id DESC " +
                                      "LIMIT 5", new String[]{String.valueOf(mChatRoom), String.valueOf(mChatRoom)})) {
            list = new ArrayList<>(cur.getCount());
            if (cur.moveToFirst()) {
                do {
                    ChatMember member = new Gson().fromJson(cur.getString(0), ChatMember.class);
                    ChatMessage msg = new ChatMessage(cur.getString(1), member);
                    list.add(msg);
                } while (cur.moveToNext());
            }
        }
        return list;
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(ChatMessage m, int memberId) {
        if (m == null || m.getText() == null) {
            Utils.log("Message empty");
            return;
        }

        Utils.logv("replace " + m.getText() + " " + m.getId() + " " + m.getPrevious() + " " + m.getStatus());

        db.beginTransaction();
        // Query read status from the previous message and use this read status as well if it is "0"
        boolean read = memberId == m.getMember()
                                    .getId();
        try (Cursor cur = db.rawQuery("SELECT read FROM chat_message WHERE _id=?", new String[]{String.valueOf(m.getId())})) {
            if (cur.moveToFirst() && cur.getInt(0) == 1) {
                read = true;
            }
        }
        m.setStatus(ChatMessage.STATUS_SENT);
        m.setRead(read);
        replaceMessage(m);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void replaceMessage(ChatMessage m) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date date;
        try {
            date = formatter.parse(m.getTimestamp());
        } catch (ParseException e) {
            date = new Date();
        }
        db.execSQL("REPLACE INTO chat_message (_id,previous,room,text,timestamp,signature,member,read,sending) VALUES (?,?,?,?,?,?,?,?,?)",
                   new String[]{String.valueOf(m.getId()), String.valueOf(m.getPrevious()), String.valueOf(mChatRoom), m.getText(), Utils.getDateTimeString(date),
                                m.getSignature(), new Gson().toJson(m.getMember()), m.getRead() ? "1" : "0", String.valueOf(m.getStatus())});
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(List<ChatMessage> m) {
        ChatMember member = Utils.getSetting(mContext, Const.CHAT_MEMBER, ChatMember.class);

        if (member == null) {
            return;
        }

        db.beginTransaction();
        for (ChatMessage msg : m) {
            replaceInto(msg, member.getId());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public Cursor getNewMessages(ChatMember member, int messageId) throws NoPrivateKey, IOException {
        List<ChatMessage> messages;
        if (messageId == -1) {
            messages = TUMCabeClient.getInstance(mContext)
                                    .getNewMessages(mChatRoom, new ChatVerification(mContext, member));
        } else {
            messages = TUMCabeClient.getInstance(mContext)
                                    .getMessages(mChatRoom, messageId, new ChatVerification(mContext, member));
        }
        replaceInto(messages);
        return getUnread();
    }
}