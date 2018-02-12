package de.tum.`in`.tumcampusapp.repository


import android.support.v7.widget.RecyclerView
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.managers.ChatMessageManager
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage
import de.tum.`in`.tumcampusapp.models.dbEntities.Sync
import de.tum.`in`.tumcampusapp.notifications.Chat
import io.reactivex.Flowable
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ChatMessageLocalRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor();

    lateinit var db: TcaDb

    //ChatMessage methods

    fun getRead(id: Int): Int = db.chatMessageDao().getRead(id)

    fun markAsRead(room: Int) = { db.chatMessageDao().markAsRead(room) }

    fun deleteOldEntries() = db.chatMessageDao().deleteOldEntries()

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

    fun replaceMessage(chatMessage: ChatMessage) =
            executor.execute { db.chatMessageDao().replaceMessage(chatMessage) }

}