package de.tum.`in`.tumcampusapp.component.ui.chat


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMessage
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository
import de.tum.`in`.tumcampusapp.utils.ErrorHelper
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ChatMessageViewModel @Inject constructor(
        private val localRepository: ChatMessageLocalRepository,
        private val remoteRepository: ChatMessageRemoteRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    fun markAsRead(room: Int) = localRepository.markAsRead(room)

    fun addToUnsent(message: ChatMessage) = localRepository.addToUnsent(message)

    fun getAll(room: Int): List<ChatMessage> = localRepository.getAllChatMessagesList(room)

    fun getUnsentInChatRoom(room: ChatRoom): List<ChatMessage>{
        return localRepository.getUnsentInChatRoom(room.id)
    }

    fun fetchNewMessages(
            room: ChatRoom,
            verification: TUMCabeVerification
    ) {
        compositeDisposable += getNewMessages(room, verification)
                .subscribe(_messages::postValue, ErrorHelper::logAndIgnore)
    }

    fun fetchOlderMessages(
            room: ChatRoom,
            messageId: Long,
            verification: TUMCabeVerification
    ) {
        compositeDisposable += getOlderMessages(room, messageId, verification)
                .subscribe(_messages::postValue, ErrorHelper::logAndIgnore)
    }

    fun getOlderMessages(
            room: ChatRoom,
            messageId: Long,
            verification: TUMCabeVerification
    ): Observable<List<ChatMessage>> {
        return remoteRepository
                .getMessages(room.id, messageId, verification)
                .subscribeOn(Schedulers.io())
                .doOnNext { localRepository.replaceMessages(it) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNewMessages(room: ChatRoom,
                       verification: TUMCabeVerification): Observable<List<ChatMessage>> {
        return remoteRepository
                .getNewMessages(room.id, verification)
                .subscribeOn(Schedulers.io())
                .doOnNext { localRepository.replaceMessages(it) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}
