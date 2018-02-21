package de.tum.`in`.tumcampusapp.repository


import de.tum.`in`.tumcampusapp.R.string.room
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ChatMessageLocalRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor();

    lateinit var db: TcaDb

    fun markAsRead(room: Int) = { db.chatMessageDao().markAsRead(room) }

    fun deleteOldEntries() = db.chatMessageDao().deleteOldEntries()

    fun addToUnsent(message: ChatMessage) =
            executor.execute { db.chatMessageDao().replaceMessage(message) }

    fun getAllChatMessagesList(room: Int): List<ChatMessage> =
            db.chatMessageDao().getAll(room)

    fun getUnsent(): List<ChatMessage> =
            db.chatMessageDao().getUnsent()

    fun replaceMessage(chatMessage: ChatMessage) =
            executor.execute { db.chatMessageDao().replaceMessage(chatMessage) }

}