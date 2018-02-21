package de.tum.`in`.tumcampusapp.component.ui.chat.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatVerification
import io.reactivex.Observable

object ChatMessageRemoteRepository {

    lateinit var tumCabeClient: TUMCabeClient

    fun getMessages(roomId: Int, messageId: Long, verification: ChatVerification): Observable<List<ChatMessage>>
            = tumCabeClient.getMessages(roomId, messageId, verification)

    fun getNewMessages(roomId: Int, verification: ChatVerification): Observable<List<ChatMessage>>
            = tumCabeClient.getNewMessages(roomId, verification)

    fun sendMessage(roomId: Int, chatMessage: ChatMessage): Observable<ChatMessage>
            = tumCabeClient.sendMessage(roomId, chatMessage)
}