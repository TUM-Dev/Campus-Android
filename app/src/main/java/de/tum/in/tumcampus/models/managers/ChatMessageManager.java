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
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_message (_id INTEGER PRIMARY KEY, room INTEGER, url VARCHAR, " +
                "text TEXT, timestamp VARCHAR, signature TEXT, member BLOB, valid INTEGER, status INTEGER)");

        // Delete all entries that are too old
        db.rawQuery("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')", null);
    }

    /**
     * Gets all messages for the room
     *
     * @return List of chat messages
     */
    public Cursor getAll() {
        return db.rawQuery("SELECT * FROM chat_message WHERE room=? ORDER BY _id", new String[]{mChatRoom.getGroupId()});
    }

    /**
     * Gets all messages marked as unread
    * */
    private Cursor getUnread() {
        return db.rawQuery("SELECT * FROM chat_message WHERE room=? ORDER BY _id", new String[]{mChatRoom.getGroupId()});
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
        db.execSQL("REPLACE INTO chat_message (_id,room,url,text,timestamp,signature,member,valid,status) VALUES (?,?,?,?,?,?,?,?,0)",
                new String[]{"" + m.getId(), mChatRoom.getGroupId(), m.getUrl(), m.getText(), Utils.getDateTimeString(date),
                        m.getSignature(), (new Gson().toJson(m.getMember())), m.isValid() ? "1" : "0"});
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
        int id = cursor.getInt(0);
        String url = cursor.getString(2);
        String text = cursor.getString(3);
        String time = formatter.format(Utils.getISODateTime(cursor.getString(4)));
        ChatMember member = new Gson().fromJson(cursor.getString(6), ChatMember.class);
        ListChatMessage msg = new ListChatMessage(id, url, text, member, time);
        msg.setId(cursor.getInt(0));
        msg.setSignature(cursor.getString(5));
        msg.setValid(cursor.getInt(7) == 1);
        return msg;
    }

    public Cursor getNewMessages() {
        ArrayList<ListChatMessage> messages = ChatClient.getInstance(mContext).getMessages(mChatRoom.getGroupId(), 1);
        replaceInto(messages);
        return getUnread();
    }

    /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update results in UI
                if (chatHistoryAdapter == null) {
                    chatHistoryAdapter = new ChatHistoryAdapter(ChatActivity.this, chatManager.getAll(currentChatRoom.getGroupId()), currentChatMember);
                    lvMessageHistory.setAdapter(chatHistoryAdapter);
                } else if (!loadingMore) {
                    chatHistoryAdapter.changeCursor(chatManager.getAll(currentChatRoom.getGroupId()));
                    chatHistoryAdapter.notifyDataSetChanged();
                }
                if (loadingMore)
                    lvMessageHistory.removeHeaderView(bar);
            }
        });*/
}