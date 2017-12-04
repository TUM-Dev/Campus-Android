package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import android.database.Cursor;

@Dao
public interface ChatMessageDao {


    @Query("SELECT c.* FROM chat_message c, (SELECT c1._id FROM chat_message c1"+
           " LEFT JOIN chat_message c2 ON c2._id=c1.previous WHERE c2._id IS NULL AND c1.room=:room "+
           " ORDER BY c1._id DESC LIMIT 1) AS until WHERE c._id>=until._id AND room = :room ORDER BY c._id")
    Cursor getAll(int room);

    @Query("SELECT * FROM chat_message")
    Cursor getAll();

    //getAllUnsent?

    @Query("UPDATE chat_message SET read=1 WHERE read=0 AND room=:room")
    void markAsread(int room);

    @Query("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')")
    void deleteOldMessages();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceMessage(int id, int previous, int room, String text, String timestamp, String signature, int member, int read, int sending);

    @Query("SELECT read FROM chat_message WHERE _id=:id")
    Cursor getReadStatus(int id);


    @Query("SELECT c.* FROM chat_message c, (SELECT c1._id "
           + " FROM chat_message c1 LEFT JOIN chat_message c2 ON c2._id=c1.previous"
           + " WHERE (c2._id IS NULL OR c1.read=1) AND c1.room=:room"
           + " ORDER BY c1._id DESC"
           + " LIMIT 1) AS until"
           + " WHERE c._id>until._id AND c.room= :room"
           + " ORDER BY c._id")
    Cursor getUnread(int room);

    @Query("SELECT c.member, c.text FROM chat_message c, (SELECT c1._id FROM chat_message c1"+
           " LEFT JOIN chat_message c2 ON c2._id=c1.previous WHERE (c2._id IS NULL OR c1.read=1)"+
           " AND c1.room=:room ORDER BY c1._id DESC LIMIT 1) AS until WHERE c._id>until._id AND c.room=:room"+
           " ORDER BY c._id DESC LIMIT 5")
    Cursor getLastUnread(int room);
}
