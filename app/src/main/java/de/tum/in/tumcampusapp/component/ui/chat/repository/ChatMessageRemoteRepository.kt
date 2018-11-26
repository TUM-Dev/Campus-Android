package de.tum.`in`.tumcampusapp.component.ui.chat.repository

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.ui.chat.FcmChat
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ChatMessageRemoteRepository @Inject constructor(
        private val context: Context,
        private val localRepository: ChatMessageLocalRepository,
        private val tumCabeClient: TUMCabeClient
) {

    fun sendMessage(chatMessage: ChatMessage): Disposable {
        val broadcastManager = LocalBroadcastManager.getInstance(context)
        val verification = TUMCabeVerification.create(context, chatMessage)

        return sendMessage(chatMessage.room, verification)
                .subscribeOn(Schedulers.io())
                .subscribe({ message ->
                    message.sendingStatus = ChatMessage.STATUS_SENT
                    localRepository.replaceMessage(message)
                    localRepository.removeUnsent(chatMessage)

                    // Send broadcast to eventually open ChatActivity
                    val intent = Intent(Const.CHAT_BROADCAST_NAME).apply {
                        val fcmChat = FcmChat(message.room, message.member.id, 0)
                        putExtra(Const.FCM_CHAT, fcmChat)
                    }
                    broadcastManager.sendBroadcast(intent)
                }, { t ->
                    Utils.log(t)
                    chatMessage.sendingStatus = ChatMessage.STATUS_ERROR
                    localRepository.replaceMessage(chatMessage)
                    val intent = Intent(Const.CHAT_BROADCAST_NAME)
                    broadcastManager.sendBroadcast(intent)
                })
    }

    fun getMessages(
            roomId: Int,
            messageId: Long,
            verification: TUMCabeVerification
    ): Observable<List<ChatMessage>> = tumCabeClient.getMessages(roomId, messageId, verification)

    fun getNewMessages(
            roomId: Int,
            verification: TUMCabeVerification
    ): Observable<List<ChatMessage>> = tumCabeClient.getNewMessages(roomId, verification)

    fun sendMessage(
            roomId: Int,
            verification: TUMCabeVerification?
    ): Observable<ChatMessage> = tumCabeClient.sendMessage(roomId, verification)

}
