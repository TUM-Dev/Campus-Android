package de.tum.`in`.tumcampusapp.viewmodel


import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.models.gcm.GCMChat
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatVerification
import de.tum.`in`.tumcampusapp.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.repository.ChatMessageRemoteRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for chat messages
 */

class ChatMessageViewModel(private val localRepository: ChatMessageLocalRepository,
                           private val remoteRepository: ChatMessageRemoteRepository,
                           private val compositeDisposable: CompositeDisposable) : ViewModel() {

    /**
     * Returns a flowable that emits a list of chat messages from the local repository
     */

    fun markAsRead(room: Int) =
            localRepository.markAsRead(room)

    fun deleteOldEntries() =
            localRepository.deleteOldEntries()

    fun addToUnsent(message: ChatMessage) =
            localRepository.addToUnsent(message)

    fun getAll(room: Int): List<ChatMessage> =
            localRepository.getAllChatMessagesList(room)

    fun getUnsent(): List<ChatMessage> =
            localRepository.getUnsent()

    fun getMessages(roomId: Int, messageId: Long, verification: ChatVerification): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.getMessages(roomId, messageId, verification) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({ t ->
                        t.forEach { localRepository.replaceMessage(it) }
                    })
            )

    fun getNewMessages(roomId: Int, verification: ChatVerification): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.getNewMessages(roomId, verification) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({ t ->
                        t.forEach { localRepository.replaceMessage(it) }
                    })
            )

    fun sendMessage(roomId: Int, chatMessage: ChatMessage, context: Context): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.sendMessage(roomId, chatMessage) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({
                        it.sendingStatus = ChatMessage.STATUS_SENT
                        localRepository.replaceMessage(it)

                        // Send broadcast to eventually open ChatActivity
                        val extras = Bundle()
                        extras.putSerializable("GCMChat", GCMChat(it.getRoom(), it.getMember().id, 0))
                        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("chat-message-received").putExtras(extras))
                    })
            )
}