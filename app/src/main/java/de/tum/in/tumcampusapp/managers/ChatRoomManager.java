package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.ChatMessagesCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRoom;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;

/**
 * TUMOnline cache manager, allows caching of TUMOnline requests
 */
public class ChatRoomManager extends AbstractManager implements Card.ProvidesCard {

    public static final int COL_ROOM = 0;
    public static final int COL_NAME = 1;
    public static final int COL_SEMESTER = 2;
    public static final int COL_SEMESTER_ID = 3;
    public static final int COL_JOINED = 4;
    public static final int COL_LV_NR = 5;
    public static final int COL_CONTRIBUTOR = 6;
    public static final int COL_MEMBERS = 7;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatRoomManager(Context context) {
        super(context);

        // create table if needed
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_room (room INTEGER, name VARCHAR, " +
                   "semester VARCHAR, semester_id VARCHAR, joined INTEGER, _id INTEGER, contributor VARCHAR, members INTEGER, PRIMARY KEY(name, semester_id))");
    }

    /**
     * Gets all chat rooms that you have joined(1)/not joined(0) for the specified room
     *
     * @param joined chat room 1=joined, 0=not joined/left chat room, -1=not joined
     * @return List of chat messages
     */
    public Cursor getAllByStatus(int joined) {
        String condition = "joined=1";
        if (joined == 0) {
            condition = "joined=0 OR joined=-1";
        }

        return db.rawQuery("SELECT r.*, m.ts, m.text " +
                           "FROM chat_room r " +
                           "LEFT JOIN (SELECT MAX(timestamp) ts, text, room FROM chat_message GROUP BY room) m ON (m.room=r.room) " +
                           "WHERE " + condition + " " +
                           " " +
                           "ORDER BY r.semester!='', r.semester_id DESC, datetime(m.ts) DESC, r.name", null);
    }

    /**
     * Saves the given lecture into database
     */
    public void replaceInto(LecturesSearchRow lecture) {
        try (Cursor cur = db.rawQuery("SELECT _id FROM chat_room WHERE name=? AND semester_id=?",
                                      new String[]{lecture.getTitel(), lecture.getSemester_id()})) {
            cur.moveToFirst();
            if (cur.getCount() >= 1) {
                db.execSQL("UPDATE chat_room SET semester=?, _id=?, contributor=? WHERE name=? AND semester_id=?",
                           new String[]{lecture.getSemester_name(), lecture.getStp_lv_nr(),
                                        lecture.getVortragende_mitwirkende(), lecture.getTitel(), lecture.getSemester_id()});
            } else {
                db.execSQL("REPLACE INTO chat_room (room,name,semester_id,semester,joined,_id,contributor,members) " +
                           "VALUES (-1,?,?,?,-1,?,?,0)",
                           new String[]{lecture.getTitel(), lecture.getSemester_id(),
                                        lecture.getSemester_name(), lecture.getStp_lv_nr(), lecture.getVortragende_mitwirkende()});
            }
        }
    }

    /**
     * Saves the given lectures into database
     */
    public void replaceInto(List<LecturesSearchRow> lectures) {
        db.beginTransaction();
        Collection<String> set;
        try (Cursor cur = db.rawQuery("SELECT _id FROM chat_room", null)) {
            set = new HashSet<>();
            if (cur.moveToFirst()) {
                do {
                    set.add(cur.getString(COL_ROOM));
                } while (cur.moveToNext());
            }
        }

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
        if (rooms == null) {
            Utils.log("No rooms passed, can't insert anything.");
            return;
        }

        db.beginTransaction();
        db.execSQL("UPDATE chat_room SET joined=0 WHERE joined=1");
        Utils.log("reset join status of all rooms");
        for (ChatRoom room : rooms) {
            Utils.log("Member of " + room.toString());
            String roomName = room.getName();
            String semester = "ZZZ";
            if (roomName.contains(":")) {
                semester = roomName.substring(0, 3);
                roomName = roomName.substring(4);
            }
            Utils.logv("members2 " + room.getMembers());
            try (Cursor cur = db.rawQuery("SELECT _id FROM chat_room WHERE name=? AND semester_id=?", new String[]{roomName, semester})) {
                cur.moveToFirst();
                if (cur.getCount() >= 1) {
                    db.execSQL("UPDATE chat_room SET room=?, joined=1, members=? WHERE name=? AND semester_id=?",
                               new String[]{String.valueOf(room.getId()), String.valueOf(room.getMembers()), roomName, semester});
                } else {
                    db.execSQL("REPLACE INTO chat_room (room,name,semester_id,semester,joined,_id,contributor,members) " +
                               "VALUES (?,?,?,'',1,0,'',?)", new String[]{String.valueOf(room.getId()), roomName, semester, String.valueOf(room.getMembers())});
                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void join(ChatRoom currentChatRoom) {
        db.execSQL("UPDATE chat_room SET room=?, joined=1 WHERE name=? AND semester_id=?",
                   new String[]{String.valueOf(currentChatRoom.getId()), currentChatRoom.getName().substring(4), currentChatRoom.getName().substring(0, 3)});
    }

    public void leave(ChatRoom currentChatRoom) {
        db.execSQL("UPDATE chat_room SET room=?, joined=0 WHERE name=? AND semester_id=?",
                   new String[]{String.valueOf(currentChatRoom.getId()), currentChatRoom.getName().substring(4), currentChatRoom.getName().substring(0, 3)});
    }

    @Override
    public void onRequestCard(Context context) {
        ChatRoomManager manager = new ChatRoomManager(context);

        // Use this to make sure chat_message table exists
        new ChatMessageManager(context, 0);

        // Get all of the users lectures and save them as possible chat rooms
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getLECTURES_PERSONAL(), context, true);
        Optional<LecturesSearchRowSet> lecturesList = requestHandler.fetch();
        if (lecturesList.isPresent() && lecturesList.get()
                                                    .getLehrveranstaltungen() != null) {
            List<LecturesSearchRow> lectures = lecturesList.get()
                                                           .getLehrveranstaltungen();
            manager.replaceInto(lectures);
        }

        // Join all new chat rooms
        if (Utils.getSettingBool(context, Const.AUTO_JOIN_NEW_ROOMS, false)) {
            List<String> newRooms = manager.getNewUnjoined();
            ChatMember currentChatMember = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
            for (String roomId : newRooms) {
                // Join chat room
                try {
                    ChatRoom currentChatRoom = new ChatRoom(roomId);
                    currentChatRoom = TUMCabeClient.getInstance(context)
                                                   .createRoom(currentChatRoom, ChatVerification.Companion.getChatVerification(context, currentChatMember));
                    if (currentChatRoom != null) {
                        manager.join(currentChatRoom);
                    }
                } catch (IOException e) {
                    Utils.log(e, " - error occured while creating the room!");
                } catch (NoPrivateKey noPrivateKey) {
                    return;
                }
            }
        }

        // Get all rooms that have unread messages
        try (Cursor cur = manager.getUnreadRooms()) {
            if (cur.moveToFirst()) {
                do {
                    ChatMessagesCard card = new ChatMessagesCard(context);
                    card.setChatRoom(cur.getString(0), cur.getInt(1), cur.getString(2) + ':' + cur.getString(0));
                    card.apply();
                } while (cur.moveToNext());
            }
        }
    }

    private List<String> getNewUnjoined() {
        List<String> list;
        try (Cursor cursor = db.rawQuery("SELECT r.semester_id, r.name " +
                                         "FROM chat_room r, (SELECT semester_id FROM chat_room " +
                                         "WHERE (NOT semester_id IS NULL) AND semester_id!='' AND semester!='' " +
                                         "ORDER BY semester_id DESC LIMIT 1) AS new " +
                                         "WHERE r.semester_id=new.semester_id AND r.joined=-1", null)) {
            list = new ArrayList<>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(0) + ':' + cursor.getString(1));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    private Cursor getUnreadRooms() {
        return db.rawQuery("SELECT r.name,r.room,r.semester_id " +
                           "FROM chat_room r, (SELECT room FROM chat_message " +
                           "WHERE read=0 GROUP BY room) AS c " +
                           "WHERE r.room=c.room " +
                           "ORDER BY r.semester_id DESC, r.name", null);
    }
}