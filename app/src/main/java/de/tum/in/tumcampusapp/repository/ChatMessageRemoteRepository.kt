package de.tum.`in`.tumcampusapp.repository

import de.tum.`in`.tumcampusapp.api.TUMCabeClient
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatVerification
import io.reactivex.Observable
import kotlin.properties.Delegates

object ChatMessageRemoteRepository {

    /*var roomId : Int by Delegates.notNull<Int>()
    var messageId : Long by Delegates.notNull<Long>()
    lateinit var verification : ChatVerification*/
    lateinit var tumCabeClient: TUMCabeClient

    fun getMessages(roomId: Int, messageId: Long, verification: ChatVerification): Observable<List<ChatMessage>> = tumCabeClient.getMessages(roomId, messageId, verification)
    fun getNewMessages(roomId: Int, verification: ChatVerification): Observable<List<ChatMessage>> = tumCabeClient.getNewMessages(roomId, verification)
    fun sendMessage(roomId: Int, chatMessageCreate: ChatMessage): Observable<ChatMessage> = tumCabeClient.sendMessage(roomId, chatMessageCreate)
    fun updateMessage(roomId: Int, message: ChatMessage): Observable<ChatMessage> = tumCabeClient.updateMessage(roomId, message)
}