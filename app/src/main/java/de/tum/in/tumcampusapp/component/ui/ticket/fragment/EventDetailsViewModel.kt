package de.tum.`in`.tumcampusapp.component.ui.ticket.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.di.EventId
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EventDetailsViewModel @Inject constructor(
    @EventId val eventId: Int,
    private val eventsRemoteRepository: EventsRemoteRepository,
    private val ticketsLocalRepository: TicketsLocalRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _aggregatedTicketStatus = MutableLiveData<TicketStatus?>()
    val aggregatedTicketStatus: LiveData<TicketStatus?> = _aggregatedTicketStatus

    init {
        fetchTicketCount()
    }

    fun fetchTicketCount() {
        compositeDisposable += eventsRemoteRepository.fetchTicketStats(eventId)
                .subscribeOn(Schedulers.io())
                .doOnError(Utils::log)
                .subscribe(_aggregatedTicketStatus::postValue) {
                    _aggregatedTicketStatus.postValue(null)
                }
    }

    fun isEventBooked(event: Event): Boolean = ticketsLocalRepository.getTicketCount(event) > 0

    fun getBookedTicketCount(event: Event): Int = ticketsLocalRepository.getTicketCount(event)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
