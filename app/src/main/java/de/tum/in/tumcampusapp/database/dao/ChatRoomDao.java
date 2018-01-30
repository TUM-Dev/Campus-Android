package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.chatRoom.ChatRoomDbRow;

@Dao
public interface ChatRoomDao {

    @Query("SELECT r.*, m.* " +
           "FROM chat_room r " +
           "LEFT JOIN (SELECT MAX(timestamp) timestamp, text, room FROM chat_message GROUP BY room) m ON (m.room=r.room) " +
           "WHERE joined=1 " +
           "ORDER BY r.semester!='', r.semester_id DESC, datetime(m.timestamp) DESC, r.name")
    List<ChatRoomDbRow> getAllRoomsJoinedList();

    @Query("SELECT r.*, m.* " +
           "FROM chat_room r " +
           "LEFT JOIN (SELECT MAX(timestamp) timestamp, text, room FROM chat_message GROUP BY room) m ON (m.room=r.room) " +
           "WHERE joined=0 OR joined=-1 " +
           "ORDER BY r.semester!='', r.semester_id DESC, datetime(m.timestamp) DESC, r.name")
    List<ChatRoomDbRow> getAllRoomsNotJoinedList();

    @Query("SELECT _id FROM chat_room WHERE name=:name AND semester_id=:semesterId")
    List<Integer> getGivenLecture(String name, String semesterId);

    @Query("UPDATE chat_room SET semester=:semester, _id=:id, contributor=:contributor WHERE name=:name AND semester_id=:semesterId")
    void updateRoom(String semester, int id, String contributor, String name, String semesterId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceRoom(ChatRoomDbRow room);

    @Query("SELECT _id FROM chat_room")
    List<Integer> getIds();

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

    @Query("SELECT r.* " +
           "FROM chat_room r, (SELECT semester_id FROM chat_room " +
           "WHERE (NOT semester_id IS NULL) AND semester_id!='' AND semester!='' " +
           "ORDER BY semester_id DESC LIMIT 1) AS new " +
           "WHERE r.semester_id=new.semester_id AND r.joined=-1")
    List<ChatRoomDbRow> getNewUnjoined();

    @Query("SELECT r.* " +
           "FROM chat_room r, (SELECT room FROM chat_message " +
           "WHERE read=0 GROUP BY room) AS c " +
           "WHERE r.room=c.room " +
           "ORDER BY r.semester_id DESC, r.name")
    List<ChatRoomDbRow> getUnreadRooms();

    @Query("DELETE FROM chat_room")
    void removeCache();
}
