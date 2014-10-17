package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatClient;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.ListChatMessage;

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
    public static final int COL_STATUS = 7;

    /**
     * Database connection
     */
    private final SQLiteDatabase db;
    private final ChatRoom mChatRoom;
    private Context mContext;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatMessageManager(Context context, ChatRoom room) {
        db = DatabaseManager.getDb(context);
        mContext = context;
        mChatRoom = room;

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_message (_id INTEGER PRIMARY KEY, previous INTEGER, room INTEGER, " +
                "text TEXT, timestamp VARCHAR, signature TEXT, member BLOB, status INTEGER)");

        // Delete all entries that are too old
        db.rawQuery("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')", null);
    }

    /**
     * Gets all messages for the room
     *
     * @return List of chat messages
     */
    public Cursor getAll() {
        return db.rawQuery("SELECT c.* FROM chat_message c, (SELECT c1._id " +
                "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                "WHERE c2._id IS NULL AND c1.room=? " +
                "ORDER BY c1._id DESC " +
                "LIMIT 1) AS until " +
                "WHERE c._id>=until._id AND c.room=? " +
                "ORDER BY c._id", new String[]{mChatRoom.getId(),mChatRoom.getId()});
    }

    /**
     * Gets all messages marked as unread
    * */
    private Cursor getUnread() {
        return db.rawQuery("SELECT c.* FROM chat_message c, (SELECT c1._id " +
                "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
                "WHERE (c2._id IS NULL OR c1.status=1) AND c1.room=? " +
                "ORDER BY c1._id DESC " +
                "LIMIT 1) AS until " +
                "WHERE c._id>=until._id AND c.room=? " +
                "ORDER BY c._id", new String[]{mChatRoom.getId(),mChatRoom.getId()});
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(ListChatMessage m) {
       Log.e("TCA Chat", "replace " + m.getText() + " " + m.getId());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date date;
        try {
            date = formatter.parse(m.getTimestamp());
        } catch (ParseException e) {
            date = new Date();
        }
        db.execSQL("REPLACE INTO chat_message (_id,previous,room,text,timestamp,signature,member,status) VALUES (?,?,?,?,?,?,?,0)",
                new String[]{"" + m.getId(), ""+m.getPrevious(), mChatRoom.getId(), m.getText(), Utils.getDateTimeString(date),
                        m.getSignature(), (new Gson().toJson(m.getMember()))});
    }

    /**
     * Saves the given message into database
     */
    public boolean replaceInto(List<ListChatMessage> m) {
        db.beginTransaction();
        Cursor cur = db.rawQuery("SELECT _id FROM chat_message WHERE room=?", new String[]{mChatRoom.getGroupId()});
        HashSet<Integer> set = new HashSet<Integer>();
        int min = Integer.MAX_VALUE;
        if (cur.moveToFirst()) {
            do {
                final int val = cur.getInt(0);
                if (val < min)
                    min = val;
                set.add(val);
            } while (cur.moveToNext());
        }
        cur.close();
        int minNew = Integer.MAX_VALUE;
        boolean newMessages = false;
        for (ListChatMessage msg : m) {
            if (!set.contains(msg.getId())) {
                newMessages = true;
                replaceInto(msg);
            }
            if(msg.getId()<minNew)
                minNew = msg.getId();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        return newMessages || minNew>min;
    }

    public static ListChatMessage toObject(Cursor cursor) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        int id = cursor.getInt(COL_ID);
        String text = cursor.getString(COL_TEXT);
        String time = formatter.format(Utils.getISODateTime(cursor.getString(COL_TIMESTAMP)));
        int previous = cursor.getInt(COL_PREVIOUS);
        ChatMember member = new Gson().fromJson(cursor.getString(COL_MEMBER), ChatMember.class);
        ListChatMessage msg = new ListChatMessage(id, text, member, time, previous);
        msg.setSignature(cursor.getString(COL_SIGNATURE));
        return msg;
    }

    public Cursor getNewMessages() {
        ArrayList<ListChatMessage> messages = ChatClient.getInstance(mContext).getNewMessages(mChatRoom.getId());
        replaceInto(messages);
        return getUnread();
    }
}