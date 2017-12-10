package de.tum.in.tumcampusapp.database.dataAccessObjects;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomWarnings;
import android.database.Cursor;

import com.google.j2objc.annotations.ReflectionSupport;

import java.util.List;

import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.UnsentChatMessage;
import de.tum.in.tumcampusapp.notifications.Chat;

/**
 * Created by olihalu on 12/8/17.
 */
@Dao
public interface UnsentChatMessageDao {

    @Query("SELECT member, text, room, msg_id, _id FROM unsent_chat_message ORDER BY _id")
    Cursor getAllUnsent();


    @Query("SELECT member, text, room, _id FROM unsent_chat_message ORDER BY _id")
    Cursor getAllUnsentFromCurrentRoom();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToUnsent(ChatMessage message);

    @Query("DELETE FROM unsent_chat_message WHERE _id=:id")
    void removeFromUnsent(int id);
}
