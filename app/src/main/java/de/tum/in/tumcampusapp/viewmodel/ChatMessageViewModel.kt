package de.tum.`in`.tumcampusapp.viewmodel


import android.arch.lifecycle.ViewModel
import android.content.Context
import android.location.Location
import android.util.Log
import de.tum.`in`.tumcampusapp.activities.ChatActivity
import de.tum.`in`.tumcampusapp.adapters.ChatHistoryAdapter
import de.tum.`in`.tumcampusapp.auxiliary.Const
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.managers.ChatMessageManager
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMember
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatMessage
import de.tum.`in`.tumcampusapp.models.tumcabe.ChatVerification
import de.tum.`in`.tumcampusapp.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.repository.ChatMessageRemoteRepository
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.observers.InnerQueuedObserver
import io.reactivex.schedulers.Schedulers
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ViewModel for chat messages
 */

class ChatMessageViewModel(private val localRepository: ChatMessageLocalRepository,
                           private val remoteRepository: ChatMessageRemoteRepository,
                           private val compositeDisposable: CompositeDisposable) : ViewModel() {

    /**
     * Returns a flowable that emits a list of chat messages from the local repository
     */

    fun getRead(id: Int): Int =
            localRepository.getRead(id)

    fun markAsRead(room: Int) =
            localRepository.markAsRead(room)

    fun deleteOldEntries() =
            localRepository.deleteOldEntries()

    fun addToUnsent(message: ChatMessage) =
            localRepository.addToUnsent(message)

    fun replaceMessage(message: ChatMessage) =
            localRepository.replaceMessage(message)

    fun removeUnsentMessage(id: Int) =
            localRepository.removeUnsentMessage(id)

    fun getAllChatMessages(room: Int): Flowable<List<ChatMessage>> =
            localRepository.getAllChatMessages(room)
                    .defaultIfEmpty(emptyList())

    fun getAllChatMessagesList(room: Int): List<ChatMessage> =
            localRepository.getAllChatMessagesList(room)

    fun getLastUnread(room: Int): Flowable<List<ChatMessage>> =
            localRepository.getLastUnread(room)
                    .defaultIfEmpty(emptyList())

    fun getAllUnsent(): Flowable<List<ChatMessage>> =
            localRepository.getAllUnsent()
                    .defaultIfEmpty(emptyList())

    fun getAllUnsentList(): List<ChatMessage> =
            localRepository.getAllUnsentList()

    fun getAllUnsentFromCurrentRoom(): Flowable<List<ChatMessage>> =
            localRepository.getAllUnsentFromCurrentRoom()
                    .defaultIfEmpty(emptyList())

    fun getAllUnsentFromCurrentRoomList(): List<ChatMessage> =
            localRepository.getAllUnsentFromCurrentRoomList()

    fun getMessages(roomId: Int, messageId: Long, verification: ChatVerification): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.getMessages(roomId, messageId, verification) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({ t -> t.forEach { localRepository.replaceMessage(it) }
                    })
            )

    fun getNewMessages(roomId: Int, verification: ChatVerification): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.getNewMessages(roomId, verification) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({ t -> t.forEach { localRepository.replaceMessage(it) }
                    })
            )

    fun sendMessage(roomId: Int, chatMessageCreate: ChatMessage): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.sendMessage(roomId, chatMessageCreate) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({
                        it.sendingStatus = ChatMessage.STATUS_SENT
                        localRepository.replaceMessage(it) })
            )

    fun updateMessage(roomId: Int, message: ChatMessage): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .flatMap { remoteRepository.updateMessage(roomId, message) }
                    .observeOn(Schedulers.io())
                    .doOnError { Utils.logwithTag("ChatMessageViewModel", it.message) }
                    .subscribe({
                        it.sendingStatus = ChatMessage.STATUS_SENT
                        localRepository.replaceMessage(it) })
            )
}