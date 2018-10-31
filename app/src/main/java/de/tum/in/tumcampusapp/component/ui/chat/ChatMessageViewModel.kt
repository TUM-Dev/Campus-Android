package de.tum.`in`.tumcampusapp.component.ui.chat


import androidx.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ChatMessageViewModel(
        private val localRepository: ChatMessageLocalRepository,
        private val remoteRepository: ChatMessageRemoteRepository
) : ViewModel() {

    fun markAsRead(room: Int) = localRepository.markAsRead(room)

    fun deleteOldEntries() = localRepository.deleteOldEntries()

    fun addToUnsent(message: ChatMessage) = localRepository.addToUnsent(message)

    fun getAll(room: Int): List<ChatMessage> = localRepository.getAllChatMessagesList(room)

    fun getUnsent(): List<ChatMessage> = localRepository.getUnsent()

    fun getUnsentInChatRoom(room: ChatRoom): List<ChatMessage>{
        return localRepository.getUnsentInChatRoom(room.id)
    }

    fun getOlderMessages(room: ChatRoom, messageId: Long,
                         verification: TUMCabeVerification): Observable<List<ChatMessage>> {
        return remoteRepository
                .getMessages(room.id, messageId, verification)
                .subscribeOn(Schedulers.computation())
                .doOnNext { localRepository.replaceMessages(it) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNewMessages(room: ChatRoom,
                       verification: TUMCabeVerification): Observable<List<ChatMessage>> {
        return remoteRepository
                .getNewMessages(room.id, verification)
                .subscribeOn(Schedulers.computation())
                .doOnNext { localRepository.replaceMessages(it) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendMessage(roomId: Int, chatMessage: ChatMessage, context: Context): Disposable {
        val broadcastManager = LocalBroadcastManager.getInstance(context)
        val verification = TUMCabeVerification.create(context, chatMessage)

        return remoteRepository.sendMessage(roomId, verification)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
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
                    Utils.logwithTag("ChatMessageViewModel", t.message ?: "unknown")
                    chatMessage.sendingStatus = ChatMessage.STATUS_ERROR
                    localRepository.replaceMessage(chatMessage)
                    val intent = Intent(Const.CHAT_BROADCAST_NAME)
                    broadcastManager.sendBroadcast(intent)
                })
    }
}