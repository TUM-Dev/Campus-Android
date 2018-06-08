package de.tum.in.tumcampusapp.component.ui.chat;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomAndLastMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoomDbRow;

/**
 * Queries needed for the ChatRoomActivity/Controller.
 */
@Dao
public interface ChatRoomDao {

    @Query("SELECT r.*, m.timestamp, m.text, unread.count as nr_unread " +
           "FROM chat_room r " +
           "LEFT JOIN (SELECT count(*) as count, c.room "
                    + "FROM chat_message c, chat_room cr "
                    + "WHERE c.room = cr.room AND c._id > cr.last_read "
                    + "GROUP BY c.room) unread on (unread.room=r.room) " +
           "LEFT JOIN (SELECT MAX(timestamp) timestamp, text, room FROM chat_message GROUP BY room) m ON (m.room=r.room) " +
           "WHERE joined=1 " +
           "ORDER BY r.semester!='', r.semester_id DESC, datetime(m.timestamp) DESC, r.name")
    List<ChatRoomAndLastMessage> getAllRoomsJoinedList();

    @Query("SELECT r.*, m.timestamp, m.text, 0 as nr_unread " +
           "FROM chat_room r " +
           "LEFT JOIN (SELECT MAX(timestamp) timestamp, text, room FROM chat_message GROUP BY room) m ON (m.room=r.room) " +
           "WHERE joined=0 OR joined=-1 " +
           "ORDER BY r.semester!='', r.semester_id DESC, datetime(m.timestamp) DESC, r.name")
    List<ChatRoomAndLastMessage> getAllRoomsNotJoinedList();

    @Query("SELECT _id FROM chat_room WHERE name=:name AND semester_id=:semesterId")
    List<Integer> getGivenLecture(String name, String semesterId);

    @Query("UPDATE chat_room SET semester=:semester, _id=:id, contributor=:contributor WHERE name=:name AND semester_id=:semesterId")
    void updateRoom(String semester, int id, String contributor, String name, String semesterId);

    @Update
    void updateRoom(ChatRoomDbRow room);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceRoom(ChatRoomDbRow room);

    @Query("SELECT _id FROM chat_room")
    List<Integer> getLvIds();

    @Query("SELECT * FROM chat_room")
    List<ChatRoomDbRow> getAll();

    @Query("UPDATE chat_room SET joined=0 WHERE joined=1")
    void markAsNotJoined();

    @Query("UPDATE chat_room SET room=:room, joined=1, members=:members WHERE name=:name AND semester_id=:semesterId")
    void updateRoomToJoined(int room, int members, String name, String semesterId);

    @Query("UPDATE chat_room SET room=:room, joined=1 WHERE name=:name AND semester_id=:semesterId")
    void updateJoinedRooms(int room, String name, String semesterId);

    @Query("UPDATE chat_room SET room=:room, joined=0 WHERE name=:name AND semester_id=:semesterId")
    void updateLeftRooms(int room, String name, String semesterId);

    @Query("UPDATE chat_room SET members=:memberCount WHERE room=:roomId AND name=:roomName")
    void updateMemberCount(int memberCount, int roomId, String roomName);

    @Query("SELECT r.* " +
           "FROM chat_room r, (SELECT semester_id FROM chat_room " +
           "WHERE (NOT semester_id IS NULL) AND semester_id!='' AND semester!='' " +
           "ORDER BY semester_id DESC LIMIT 1) AS new " +
           "WHERE r.semester_id=new.semester_id AND r.joined=-1")
    List<ChatRoomDbRow> getNewUnjoined();

    @Query("SELECT r.* " +
           "FROM chat_room r " +
           "WHERE r.last_read < (SELECT MAX(_id) FROM chat_message m WHERE m.room = r.room)")
    List<ChatRoomDbRow> getUnreadRooms();

    @Query("DELETE FROM chat_room")
    void removeCache();
}
