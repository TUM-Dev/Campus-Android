package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.List;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ListChatMessage;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatMessageManager {

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatMessageManager(Context context) {
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_message (id INTEGER PRIMARY KEY, room INTEGER, url VARCHAR, " +
                "text TEXT, timestamp VARCHAR, signature TEXT, member BLOB, valid INTEGER)");

        // Delete all entries that are too old
        db.rawQuery("DELETE FROM chat_message WHERE timestamp<UNIX_TIMESTAMP(datetime('now','-1 month'))", null);
    }

    /**
     * Gets all messages for the specified room
     *
     * @param room room id
     * @return List of chat messages
     */
    public Cursor getAll(String room) {
        return db.rawQuery("SELECT * FROM chat_message WHERE room=? ORDER BY id", new String[]{room});
    }

    /**
     * Saves the given message into database
     * */
    public void replaceInto(ListChatMessage m, String room) {
        Utils.logv("replace " + m.getText());
        db.execSQL("REPLACE INTO chat_message (id,room,url,text,timestamp,signature,member,valid) VALUES (?,?,?,?,?,?,?,?)",
                new String[]{""+m.getId(), room, m.getUrl(), m.getText(), m.getTimestamp(), m.getSignature(),
                        (new Gson().toJson(m.getMember())), m.isValid()?"1":"0"});
    }

    /**
     * Saves the given message into database
     * */
    public void replaceInto(List<ListChatMessage> m, String room) {
        for(ListChatMessage msg : m) {
            replaceInto(msg, room);
        }
    }

    public static ListChatMessage toObject(Cursor cursor) {
        String url = cursor.getString(2);
        String text = cursor.getString(3);
        String time = cursor.getString(4);
        ChatMember member = new Gson().fromJson(cursor.getString(6), ChatMember.class);
        ListChatMessage msg = new ListChatMessage(url, text, member, time);
        msg.setId(cursor.getInt(0));
        msg.setSignature(cursor.getString(5));
        msg.setValid(cursor.getInt(7) == 1);
        return msg;
    }
}