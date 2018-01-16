package de.tum.`in`.tumcampusapp.repository


import android.support.v7.widget.RecyclerView
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage
import de.tum.`in`.tumcampusapp.models.dbEntities.Sync
import de.tum.`in`.tumcampusapp.notifications.Chat
import io.reactivex.Flowable
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ChatMessageLocalRepository   {

    const val COL_ID = 0;
    const val COL_PREVIOUS = 1;
    const val COL_ROOM = 2;
    const val COL_TEXT = 3;
    const val COL_TIMESTAMP = 4;
    const val COL_SIGNATURE = 5;
    const val COL_MEMBER = 6;
    const val COL_READ = 7;
    const val COL_SENDING = 8;

    private val executor: Executor = Executors.newSingleThreadExecutor();

    lateinit var db: TcaDb

    //ChatMessage methods
    fun markAsRead(room: Int) =
            executor.execute { db.chatMessageDao().markAsRead(room) }

    fun deleteOldEntries() =
            executor.execute { db.chatMessageDao().deleteOldEntries() }

    fun addToUnsent(message: ChatMessage) =
            executor.execute { db.chatMessageDao().addToUnsent(message) }

    fun removeUnsentMessage(id: Int) =
            executor.execute { db.chatMessageDao().removeUnsentMessage(id) }

    fun getAllChatMessages(room: Int): Flowable<List<ChatMessage>> =
            db.chatMessageDao().getAllFlow(room)

    fun getAllChatMessagesList(room: Int): List<ChatMessage> =
            db.chatMessageDao().getAll(room)

    fun getLastUnread(room: Int): Flowable<List<ChatMessage>> =
            db.chatMessageDao().getLastUnreadFlow(room)

    fun getAllUnsent(): Flowable<List<ChatMessage>> =
            db.chatMessageDao().allUnsentFlow

    fun getAllUnsentList(): List<ChatMessage> =
            db.chatMessageDao().allUnsent

    fun getAllUnsentFromCurrentRoom(): Flowable<List<ChatMessage>> =
            db.chatMessageDao().allUnsentFromCurrentRoomFlow

    fun getAllUnsentFromCurrentRoomList(): List<ChatMessage> =
            db.chatMessageDao().allUnsentFromCurrentRoom

    fun addChatMessage(chatMessage: ChatMessage) =
            executor.execute { db.chatMessageDao().addToUnsent(chatMessage) }

    fun replaceMessage(chatMessage: ChatMessage) =
            executor.execute { db.chatMessageDao().replaceMessage(chatMessage) }

    // Sync methods??

/*
    fun removeUnsent(id: Int) = db.chatMessageDao().removeUnsentMessage(id)
*/
}