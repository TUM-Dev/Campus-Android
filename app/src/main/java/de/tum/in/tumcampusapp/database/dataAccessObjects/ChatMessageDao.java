package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
@Dao
public interface ChatMessageDao {

    @Query("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')")
    void deleteOldEntries();

    @Query("SELECT c.* FROM chat_message c, (SELECT c1._id " +
           "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
           "WHERE c2._id IS NULL AND c1.room=:room " +
           "ORDER BY c1._id DESC " +
           "LIMIT 1) AS until " +
           "WHERE c._id>=until._id AND c.room=:room " +
           "ORDER BY c._id")
    List<ChatMessage> getAll(int room);

    @Query("UPDATE chat_message SET read=1 WHERE read=0 AND room=:room")
    void markAsRead(int room);

    @Query("SELECT c.* FROM chat_message c, (SELECT c1._id "+
           "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous "+
           "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=:room "+
           "ORDER BY c1._id DESC "+
           "LIMIT 1) AS until "+
           "WHERE c._id>until._id AND c.room=:room "+
           "ORDER BY c._id")
    Cursor getUnread(int room);

    @Query("SELECT * FROM chat_message c, (SELECT c1._id " +
           "FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous " +
           "WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=:room " +
           "ORDER BY c1._id DESC " +
           "LIMIT 1) AS until " +
           "WHERE c._id>until._id AND c.room=:room " +
           "ORDER BY c._id DESC " +
           "LIMIT 5")
    List<ChatMessage> getLastUnread(int room);

    @Query("SELECT read FROM chat_message WHERE _id=:id")
    int getRead(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceMessage(ChatMessage m);

    @Query("SELECT * FROM chat_message WHERE sending=1 ORDER BY _id")
    List<ChatMessage> getAllUnsent();

    @Query("SELECT * FROM chat_message WHERE msg_id=0 AND sending=1 ORDER BY _id")
    List<ChatMessage> getAllUnsentFromCurrentRoom();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToUnsent(ChatMessage message);

    @Query("DELETE FROM chat_message WHERE _id=:id")
    void removeUnsentMessage(int id);


}
