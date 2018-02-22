package de.tum.in.tumcampusapp.component.ui.chat;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;

@Dao
public interface ChatMessageDao {

    @Query("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')")
    void deleteOldEntries();

    @Query("SELECT c.* FROM chat_message c WHERE c.room=:room ORDER BY c._id")
    List<ChatMessage> getAll(int room);

    @Query("UPDATE chat_message SET read=1 WHERE room=:room")
    void markAsRead(int room);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceMessage(ChatMessage m);

    @Query("DELETE FROM chat_message")
    void removeCache();

    @Query("SELECT c.* FROM chat_message c WHERE c.sending=1 ORDER BY c._id")
    List<ChatMessage> getUnsent();
}
