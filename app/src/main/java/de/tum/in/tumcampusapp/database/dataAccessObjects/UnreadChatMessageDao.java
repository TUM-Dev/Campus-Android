package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

@Dao
public interface UnreadChatMessageDao {

    @Delete()
    void removeFromUnsent(int id);

    @Query("SELECT member, text, room, msg_id, _id FROM unsent_chat_message ORDER BY _id")
    Cursor getAllUnsentUpdated();

    @Query("SELECT member, text, room, _id FROM unsent_chat_message WHERE msg_id=0 ORDER BY _id")
    Cursor getAllUnsent();

    @Insert()
    void addToUnsent(String text, int room, int member, int msgId);

}
