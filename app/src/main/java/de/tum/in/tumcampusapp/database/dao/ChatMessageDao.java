package de.tum.in.tumcampusapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import io.reactivex.Flowable;

@Dao
public interface ChatMessageDao {

    String SQL_ORDER_BY = "ORDER BY c1._id DESC " +
                          "LIMIT 1) AS until " +
                          "WHERE c._id>=until._id AND c.room=:room " +
                          "ORDER BY c._id";

    String SQL_FROM = "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous ";

    String SQL_GET_ALL = "SELECT c.* FROM chat_message c, (SELECT c1._id " + SQL_FROM +
                         "WHERE c2._id IS NULL AND c1.room=:room " + SQL_ORDER_BY;

    String SQL_GET_UNREAD = "SELECT c.* FROM chat_message c, (SELECT c1._id " + SQL_FROM +
                            "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=:room " + SQL_ORDER_BY;

    String SQL_GET_LAST_UNREAD = "SELECT * FROM chat_message c, (SELECT c1._id " + SQL_FROM +
                                 "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=:room " +
                                 SQL_ORDER_BY + " LIMIT 5 ";

    String SQL_ALL_UNSENT_CURRENT_ROOM = "SELECT * FROM chat_message WHERE msg_id=0 AND sending=1 ORDER BY _id";

    String SQL_ALL_UNSENT = "SELECT * FROM chat_message WHERE sending=1 ORDER BY _id";

    @Query("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')")
    void deleteOldEntries();

    @Query(SQL_GET_ALL)
    List<ChatMessage> getAll(int room);

    @Query("UPDATE chat_message SET read=1 WHERE room=:room")
    void markAsRead(int room);

    @Query(SQL_GET_UNREAD)
    List<ChatMessage> getUnreadList(int room);

    @Query(SQL_GET_LAST_UNREAD)
    List<ChatMessage> getLastUnread(int room);

    @Query("SELECT read FROM chat_message WHERE _id=:id")
    int getRead(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceMessage(ChatMessage m);

    @Query(SQL_ALL_UNSENT)
    List<ChatMessage> getAllUnsent();

    @Query(SQL_ALL_UNSENT_CURRENT_ROOM)
    List<ChatMessage> getAllUnsentFromCurrentRoom();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToUnsent(ChatMessage message);

    @Query("DELETE FROM chat_message WHERE _id=:id")
    void removeUnsentMessage(int id);

    /**
     * Flowables
     */

    @Query(SQL_GET_ALL)
    Flowable<List<ChatMessage>> getAllFlow(int room);

    @Query(SQL_GET_UNREAD)
    Flowable<List<ChatMessage>> getUnreadFlow(int room);

    @Query(SQL_GET_LAST_UNREAD)
    Flowable<List<ChatMessage>> getLastUnreadFlow(int room);

    @Query("SELECT read FROM chat_message WHERE _id=:id")
    Flowable<Integer> getReadFlow(int id);

    @Query(SQL_ALL_UNSENT)
    Flowable<List<ChatMessage>> getAllUnsentFlow();

    @Query(SQL_ALL_UNSENT_CURRENT_ROOM)
    Flowable<List<ChatMessage>> getAllUnsentFromCurrentRoomFlow();

    @Query("DELETE FROM chat_message")
    void removeCache();
}
