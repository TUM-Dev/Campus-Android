package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class EventDetailsViewModel(
        private val eventId: Int,
        private val eventsRemoteRepository: EventsRemoteRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _ticketCount = MutableLiveData<Int?>()
    val ticketCount: LiveData<Int?> = _ticketCount

    init {
        fetchTicketCount()
    }

    fun fetchTicketCount() {
        compositeDisposable += eventsRemoteRepository.fetchTicketStats(eventId)
                .subscribeOn(Schedulers.io())
                .doOnError(Utils::log)
                .subscribe(_ticketCount::postValue) {
                    _ticketCount.postValue(null)
                }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    class Factory(
            private val eventId: Int,
            private val remoteRepo: EventsRemoteRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EventDetailsViewModel(eventId, remoteRepo) as T
        }

    }

}
