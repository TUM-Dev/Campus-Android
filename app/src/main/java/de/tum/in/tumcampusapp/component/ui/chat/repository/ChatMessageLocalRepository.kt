package de.tum.`in`.tumcampusapp.component.ui.chat.repository

import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.database.TcaDb
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ChatMessageLocalRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    lateinit var db: TcaDb

    fun markAsRead(room: Int) = db.chatMessageDao().markAsRead(room)

    fun deleteOldEntries() = db.chatMessageDao().deleteOldEntries()

    fun addToUnsent(message: ChatMessage) {
        executor.execute { db.chatMessageDao().replaceMessage(message) }
    }

    fun getAllChatMessagesList(room: Int): List<ChatMessage> = db.chatMessageDao().getAll(room)

    fun getUnsent(): List<ChatMessage> = db.chatMessageDao().unsent

    fun getUnsentInChatRoom(roomId: Int): List<ChatMessage> = db.chatMessageDao().getUnsentInChatRoom(roomId)

    fun replaceMessages(chatMessages: List<ChatMessage>) {
        chatMessages.forEach { replaceMessage(it) }
    }

    fun replaceMessage(chatMessage: ChatMessage) {
        executor.execute { db.chatMessageDao().replaceMessage(chatMessage) }
    }

    fun removeUnsent(chatMessage: ChatMessage) {
        executor.execute { db.chatMessageDao().removeUnsent(chatMessage.text) }
    }
}
