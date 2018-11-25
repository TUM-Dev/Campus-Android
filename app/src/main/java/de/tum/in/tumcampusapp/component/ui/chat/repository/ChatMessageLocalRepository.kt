package de.tum.`in`.tumcampusapp.component.ui.chat.repository


import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.database.TcaDb
import org.jetbrains.anko.doAsync
import javax.inject.Inject

class ChatMessageLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    fun markAsRead(room: Int) = database.chatMessageDao().markAsRead(room)

    fun deleteOldEntries() = database.chatMessageDao().deleteOldEntries()

    fun addToUnsent(message: ChatMessage) {
        doAsync { database.chatMessageDao().replaceMessage(message) }
    }

    fun getAllChatMessagesList(room: Int): List<ChatMessage> = database.chatMessageDao().getAll(room)

    fun getUnsent(): List<ChatMessage> = database.chatMessageDao().unsent

    fun getUnsentInChatRoom(roomId: Int): List<ChatMessage> = database.chatMessageDao().getUnsentInChatRoom(roomId)

    fun replaceMessages(chatMessages: List<ChatMessage>) {
        chatMessages.forEach { replaceMessage(it) }
    }

    fun replaceMessage(chatMessage: ChatMessage) {
        doAsync { database.chatMessageDao().replaceMessage(chatMessage) }
    }

    fun removeUnsent(chatMessage: ChatMessage) {
        doAsync { database.chatMessageDao().removeUnsent(chatMessage.text) }
    }

}