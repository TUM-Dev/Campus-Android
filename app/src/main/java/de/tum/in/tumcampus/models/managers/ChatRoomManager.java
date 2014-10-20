package de.tum.in.tumcampus.models.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.LecturesSearchRow;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatRoomManager {

    public static final int COL_GROUP_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_SEMESTER = 2;
    public static final int COL_SEMESTER_ID = 3;
    public static final int COL_STATUS = 4;
    public static final int COL_LV_NR = 5;
    public static final int COL_CONTRIBUTOR = 6;

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatRoomManager(Context context) {
        db = DatabaseManager.getDb(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_room (group_id INTEGER, name VARCHAR, " +
                "semester VARCHAR, semester_id VARCHAR, status INTEGER, _id INTEGER, contributor VARCHAR, PRIMARY KEY(_id, name, semester_id))");
    }

    /**
     * Gets all chat rooms that you have joined(1)/not joined(0) for the specified room
     *
     * @param status 1=joined, 0=not joined
     * @return List of chat messages
     */
    public Cursor getAllByStatus(int status) {
        return db.rawQuery("SELECT * FROM chat_room WHERE status=? ORDER BY semester_id DESC, name", new String[]{"" + status});
    }

    /**
     * Saves the given message into database
     */
    public void replaceInto(LecturesSearchRow lecture) {
        Utils.logv("replace " + lecture.getTitel());
        db.execSQL("REPLACE INTO chat_room (group_id,name,semester_id,semester,status,_id, contributor) " +
                        "VALUES (-1,?,?,?,0,?,?)",
                new String[]{lecture.getTitel(), lecture.getSemester_id(),
                        lecture.getSemester_name(), lecture.getStp_lv_nr(), lecture.getVortragende_mitwirkende()});
    }

    /**
     * Saves the given lectures into database
     */
    public void replaceInto(List<LecturesSearchRow> lectures) {
        db.beginTransaction();
        Cursor cur = db.rawQuery("SELECT _id FROM chat_room", null);
        HashSet<String> set = new HashSet<String>();
        if (cur.moveToFirst()) {
            do {
                set.add(cur.getString(0));
            } while (cur.moveToNext());
        }
        cur.close();

        for (LecturesSearchRow lecture : lectures) {
            if (!set.contains(lecture.getStp_lv_nr())) {
                replaceInto(lecture);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Saves the given chat rooms into database
     */
    public void replaceIntoRooms(List<ChatRoom> rooms) {
        for (ChatRoom room : rooms) {
            String roomName=room.getName();
            String semester="";
            if(roomName.contains(":")){
                semester=roomName.substring(0,3);
                roomName=roomName.substring(4);
            }

            Cursor cur = db.rawQuery("SELECT _id FROM chat_room WHERE name=?", new String[] {room.getName()});
            if(cur.getCount()>=1) {
                db.execSQL("UPDATE chat_room SET group_id=?, status=1 WHERE name=? AND semester_id=?",
                        new String[]{room.getId(), roomName, semester});
            } else {
                db.execSQL("REPLACE INTO chat_room (group_id,name,semester_id,semester,status,_id, contributor) " +
                                "VALUES (-1,?,'ZZZ','',1,0,'')", new String[]{roomName});
            }
        }
    }

    public void join(ChatRoom currentChatRoom) {
        db.execSQL("UPDATE chat_room SET group_id=?, status=1 WHERE name=? AND semester_id=?",
                new String[]{currentChatRoom.getId(), currentChatRoom.getName().substring(4), currentChatRoom.getName().substring(0, 3)});
    }

    public void leave(ChatRoom currentChatRoom) {
        db.execSQL("UPDATE chat_room SET group_id=?, status=0 WHERE name=? AND semester_id=?",
                new String[]{currentChatRoom.getId(), currentChatRoom.getName().substring(4), currentChatRoom.getName().substring(0, 3)});
    }
}