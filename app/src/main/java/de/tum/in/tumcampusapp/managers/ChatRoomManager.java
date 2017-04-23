package de.tum.in.tumcampusapp.managers;

import android.content.Context;
import android.database.Cursor;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.cards.ChatMessagesCard;
import de.tum.in.tumcampusapp.cards.generic.Card;
import de.tum.in.tumcampusapp.entities.ChatRoom;
import de.tum.in.tumcampusapp.entities.ChatRoom_;
import de.tum.in.tumcampusapp.entities.TcaBoxes;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRow;
import de.tum.in.tumcampusapp.models.tumo.LecturesSearchRowSet;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

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

    private Box<ChatRoom> roomBox;

    /**
     * Constructor, open/create database, create table if necessary
     *
     * @param context Context
     */
    public ChatRoomManager(Context context) {
        super(context);

        roomBox = TcaBoxes.getBoxStore().boxFor(ChatRoom.class);
    }

    /**
     * Gets all chat rooms that you have joined(1)/not joined(0) for the specified room
     *
     * @param joined chat room 1=joined, 0=not joined/left chat room, -1=not joined
     * @return List of chat messages
     */
    public List<ChatRoom> getAllByStatus(boolean joined) {
        QueryBuilder<ChatRoom> qb = roomBox.query();
        if (joined) {
            qb.equal(ChatRoom_.joined, 1);
        } else {
            qb.notEqual(ChatRoom_.joined, 1);
        }

        return qb.order(ChatRoom_.semesterId).order(ChatRoom_.name).build().find();
    }

    /**
     * Saves the given lecture into database
     */
    public void replaceInto(LecturesSearchRow lecture) {
        ChatRoom r = roomBox.query().equal(ChatRoom_.name, lecture.getTitel()).equal(ChatRoom_.semesterId, lecture.getSemester_id()).build().findFirst();
        if (r == null) {
            r = new ChatRoom();
            r.setRoom(-1);
            r.setName(lecture.getTitel());
            r.setSemesterId(lecture.getSemester_id());
            r.setSemester(lecture.getSemester_name());

        }

        r.setSemester(lecture.getSemester_name());
        r.set_id(Integer.parseInt(lecture.getStp_lv_nr()));
        r.setContributor(lecture.getVortragende_mitwirkende());

        roomBox.put(r);
    }

    /**
     * Saves the given lectures into database
     */
    public void replaceInto(List<LecturesSearchRow> lectures) {
        for (LecturesSearchRow lecture : lectures) {
            replaceInto(lecture);
        }
    }

    /**
     * Saves the given chat rooms into database
     */
    public void replaceIntoRooms(List<de.tum.in.tumcampusapp.models.tumcabe.ChatRoom> rooms) {
        if (rooms == null || rooms.size() == 0) {
            Utils.log("No rooms passed, can't insert anything.");
            return;
        }

        for (de.tum.in.tumcampusapp.models.tumcabe.ChatRoom room : rooms) {
            String[] nameSemester = splitName(room.getName());

            ChatRoom r = get(nameSemester[0], nameSemester[1]);
            if (r == null) {
                r.setName(nameSemester[1]);
                r.setSemesterId(nameSemester[0]);
            }
            r.setRoom(room.getId());
            r.setJoined(1);

            roomBox.put(r);
        }
    }

    private String[] splitName(String name) {
        String[] ret = new String[2];
        ret[0] = "ZZZ";
        ret[1] = name;
        if (name.contains(":")) {
            ret[0] = name.substring(0, 3);
            ret[1] = name.substring(4);
        }
        return ret;
    }

    private ChatRoom get(String semesterId, String name) {
        return roomBox.query().equal(ChatRoom_.name, name).equal(ChatRoom_.semesterId, semesterId).build().findFirst();
    }

    public void setJoined(de.tum.in.tumcampusapp.models.tumcabe.ChatRoom room, int join) {
        String[] nameSemester = splitName(room.getName());
        ChatRoom r = get(nameSemester[0], nameSemester[1]);
        r.setJoined(join == 1 ? 1 : 0);
        r.setRoom(room.getId());
        roomBox.put(r);
    }

    @Override
    public void onRequestCard(Context context) {
        ChatRoomManager manager = new ChatRoomManager(context);

        // Use this to make sure chat_message table exists
        new ChatMessageManager(context, 0);

        // Get all of the users lectures and save them as possible chat rooms
        TUMOnlineRequest<LecturesSearchRowSet> requestHandler = new TUMOnlineRequest<>(TUMOnlineConst.LECTURES_PERSONAL, context, true);
        Optional<LecturesSearchRowSet> lecturesList = requestHandler.fetch();
        if (lecturesList.isPresent() && lecturesList.get().getLehrveranstaltungen() != null) {
            List<LecturesSearchRow> lectures = lecturesList.get().getLehrveranstaltungen();
            manager.replaceInto(lectures);
        }

        // Join all new chat rooms
        if (Utils.getSettingBool(context, Const.AUTO_JOIN_NEW_ROOMS, false)) {
            List<String> newRooms = manager.getNewUnjoined();
            ChatMember currentChatMember = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
            for (String roomId : newRooms) {
                // Join chat room
                try {
                    de.tum.in.tumcampusapp.models.tumcabe.ChatRoom currentChatRoom = new de.tum.in.tumcampusapp.models.tumcabe.ChatRoom(roomId);
                    currentChatRoom = TUMCabeClient.getInstance(context).createRoom(currentChatRoom, new ChatVerification(context, currentChatMember));
                    if (currentChatRoom != null) {
                        manager.setJoined(currentChatRoom, 1);
                    }
                } catch (IOException e) {
                    Utils.log(e, " - error occured while creating the room!");
                } catch (NoPrivateKey noPrivateKey) {
                    return;
                }
            }
        }

        // Get all rooms that have unread messages
        Cursor cur = manager.getUnreadRooms();
        if (cur.moveToFirst()) {
            do {
                ChatMessagesCard card = new ChatMessagesCard(context);
                card.setChatRoom(cur.getString(0), cur.getInt(1), cur.getString(2) + ':' + cur.getString(0));
                card.apply();
            } while (cur.moveToNext());
        }
        cur.close();
    }

    private List<String> getNewUnjoined() {
        //TODO
        Cursor cursor = db.rawQuery("SELECT r.semester_id, r.name " +
                "FROM chat_room r, (SELECT semester_id FROM chat_room " +
                "WHERE (NOT semester_id IS NULL) AND semester_id!='' AND semester!='' " +
                "ORDER BY semester_id DESC LIMIT 1) AS new " +
                "WHERE r.semester_id=new.semester_id AND r.joined=-1", null);
        List<String> list = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0) + ':' + cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private Cursor getUnreadRooms() {
        //TODO
        return db.rawQuery("SELECT r.name,r.room,r.semester_id " +
                "FROM chat_room r, (SELECT room FROM chat_message " +
                "WHERE read=0 GROUP BY room) AS c " +
                "WHERE r.room=c.room " +
                "ORDER BY r.semester_id DESC, r.name", null);
    }
}