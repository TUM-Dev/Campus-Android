package de.tum.in.tumcampusapp.component.ui.chat;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;

@Dao
public interface ChatMessageDao {

    @Query("DELETE FROM chat_message WHERE timestamp<datetime('now','-1 month')")
    void deleteOldEntries();

    @Query("SELECT c.* FROM chat_message c WHERE c.room=:room ORDER BY c.sending, c._id")
    List<ChatMessage> getAll(int room);

    @Query("UPDATE chat_room "
           + "SET last_read = "
                + "case when (select MAX(_id) from chat_message where room=:room) is null then -1 "
                + "else (select MAX(_id) from chat_message where room=:room) end "
           + "WHERE room=:room")
    void markAsRead(int room);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replaceMessage(ChatMessage m);

    @Query("DELETE FROM chat_message WHERE chat_message._id = 0 AND chat_message.text =:text")
    void removeUnsent(String text);

    @Query("DELETE FROM chat_message")
    void removeCache();

    @Query("SELECT c.* FROM chat_message c WHERE c.sending IN (1, 2) ORDER BY c.timestamp")
    List<ChatMessage> getUnsent();

    @Query("SELECT c.* FROM chat_message c WHERE c.room = :roomId AND c.sending IN (1, 2) ORDER BY c.timestamp")
    List<ChatMessage> getUnsentInChatRoom(int roomId);

    @Query("SELECT c.* FROM chat_message c, chat_room r "
           + "WHERE c.room=:room AND c.room = r.room AND c._id > r.last_read ORDER BY c.timestamp DESC LIMIT 5")
    List<ChatMessage> getLastUnread(int room);

    @Query("SELECT count(*) FROM chat_message c, chat_room r WHERE c.room=:room AND c.room = r.room AND c._id > r.last_read")
    int getNumberUnread(int room);
}
