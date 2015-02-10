package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatMessage;
import de.tum.in.tumcampus.models.ChatVerification;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatMessageManager {


    public static final int COL_ID = 0;
    public static final int COL_PREVIOUS = 1;
    public static final int COL_ROOM = 2;
    public static final int COL_TEXT = 3;
    public static final int COL_TIMESTAMP = 4;
    public static final int COL_SIGNATURE = 5;
    public static final int COL_MEMBER = 6;
    public static final int COL_READ = 7;
    public static final int COL_SENDING = 8;

    /**
     * Database connection
     */
    private final SQLiteDatabase db;
    private final int mChatRoom;
    private final Context mContext;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatMessageManager(Context context, int room) {
        db = DatabaseManager.getDb(context);
        mContext = context;
        mChatRoom = room;
        init(db);
    }

    private static void init(SQLiteDatabase db) {
        // create tables if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_message (_id INTEGER PRIMARY KEY, previous INTEGER, room INTEGER, " +
                "text TEXT, timestamp VARCHAR, signature TEXT, member BLOB, read INTEGER, sending INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS unsent_chat_message (_id INTEGER PRIMARY KEY AUTOINCREMENT, room INTEGER, text TEXT, member BLOB, msg_id INTEGER)");

        // Delete all entries that are too old
        db.rawQuery("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')", null);
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
                "ORDER BY c._id", new String[]{""+mChatRoom, ""+mChatRoom});
    }

    public void markAsRead() {
        db.execSQL("UPDATE chat_message SET read=1 WHERE read=0 AND room=?", new String[]{"" + mChatRoom});
    }

    /**
     * Gets all unsent chat messages
     */
    public static ArrayList<ChatMessage> getAllUnsentUpdated(Context context) {
        SQLiteDatabase db = DatabaseManager.getDb(context);
        init(db);
        Cursor cur = db.rawQuery("SELECT member, text, room, msg_id, _id FROM unsent_chat_message ORDER BY _id", null);
        ArrayList<ChatMessage> list = new ArrayList<>(cur.getCount());
        if(cur.moveToFirst()) {
            do {
                ChatMember member = new Gson().fromJson(cur.getString(0), ChatMember.class);
                ChatMessage msg = new ChatMessage(cur.getString(1), member);
                msg.setRoom(cur.getInt(2));
                msg.setId(cur.getInt(3));
                msg.internalID = cur.getInt(4);
                list.add(msg);
            } while(cur.moveToNext());
        }
        cur.close();
        return list;
    }

    /**
     * Gets all unsent chat messages from the current room
     */
    public ArrayList<ChatMessage> getAllUnsent() {
        Cursor cur = db.rawQuery("SELECT member, text, room, _id FROM unsent_chat_message WHERE msg_id=0 ORDER BY _id", null);
        ArrayList<ChatMessage> list = new ArrayList<>(cur.getCount());
        if(cur.moveToFirst()) {
            do {
                ChatMember member = new Gson().fromJson(cur.getString(0), ChatMember.class);
                ChatMessage msg = new ChatMessage(cur.getString(1), member);
                msg.setRoom(cur.getInt(2));
                msg.internalID = cur.getInt(3);
                list.add(msg);
            } while(cur.moveToNext());
        }
        cur.close();
        return list;
    }

    /**
     * Saves the given message into database
     */
    public void addToUnsent(ChatMessage m) {
        //TODO handle message with already set id
        Log.e("TCA Chat", "replace into unsent " + m.getText() + " " + m.getId() + " " + m.getPrevious() + " " + m.getStatus());
        db.execSQL("REPLACE INTO unsent_chat_message (text,room,member,msg_id) VALUES (?,?,?, ?)",
                new String[]{"" + m.getText(), "" + mChatRoom, new Gson().toJson(m.getMember()), ""+m.getId()});
    }

    /**
     * Removes the message from unsent database
     * */
    public void removeFromUnsent(ChatMessage message) {
        db.execSQL("DELETE FROM unsent_chat_message WHERE _id=?", new String[]{"" + message.internalID});
    }

    /**
     * Gets all messages marked as unread
     */
    public Cursor getUnread() {
        return db.rawQuery("SELECT c.* FROM chat_message c, (SELECT c1._id " +
                "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=? " +
                "ORDER BY c1._id DESC " +
                "LIMIT 1) AS until " +
                "WHERE c._id>until._id AND c.room=? " +
                "ORDER BY c._id", new String[]{""+mChatRoom, ""+mChatRoom});
    }

    /**
     * Gets all unread chat messages
     */
    public ArrayList<ChatMessage> getLastUnread() {
        Cursor cur = db.rawQuery("SELECT c.member, c.text FROM chat_message c, (SELECT c1._id " +
                "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=? " +
                "ORDER BY c1._id DESC " +
                "LIMIT 1) AS until " +
                "WHERE c._id>until._id AND c.room=? " +
                "ORDER BY c._id DESC " +
                "LIMIT 5", new String[]{"" + mChatRoom, "" + mChatRoom});
        ArrayList<ChatMessage> list = new ArrayList<>(cur.getCount());
        if(cur.moveToFirst()) {
            do {
                ChatMember member = new Gson().fromJson(cur.getString(0), ChatMember.class);
                ChatMessage msg = new ChatMessage(cur.getString(1), member);
                list.add(msg);
            } while(cur.moveToNext());
        }
        cur.close();
        return list;
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(ChatMessage m, int memberId) {
        if (m == null || m.getText() == null) {
            Log.e("TCA Chat", "Message empty");
            return;
        }

        Log.e("TCA Chat", "replace " + m.getText() + " " + m.getId() + " "+ m.getPrevious()+ " "+ m.getStatus());

        db.beginTransaction();
        // Query read status from the previous message and use this read status as well if it is "0"
        boolean read = memberId==m.getMember().getId();
        Cursor cur = db.rawQuery("SELECT read FROM chat_message WHERE _id=?", new String[] {""+m.getId()});
        if(cur.moveToFirst()) {
            if(cur.getInt(0)==1)
                read = true;
        }
        cur.close();
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
                new String[]{"" + m.getId(), "" + m.getPrevious(), "" + mChatRoom, m.getText(), Utils.getDateTimeString(date),
                        m.getSignature(), (new Gson().toJson(m.getMember())), m.getRead() ? "1" : "0", ""+m.getStatus()});
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(List<ChatMessage> m) {
        ChatMember member = Utils.getSetting(mContext, Const.CHAT_MEMBER, ChatMember.class);
        db.beginTransaction();
        for (ChatMessage msg : m) {
            replaceInto(msg, member.getId());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
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

    public Cursor getNewMessages(PrivateKey pk, ChatMember member, int messageId) {
        ArrayList<ChatMessage> messages;
        if(messageId==-1)
            messages = ChatClient.getInstance(mContext).getNewMessages(mChatRoom, new ChatVerification(pk, member));
        else
            messages = ChatClient.getInstance(mContext).getMessages(mChatRoom, messageId, new ChatVerification(pk, member));
        replaceInto(messages);
        return getUnread();
    }
}