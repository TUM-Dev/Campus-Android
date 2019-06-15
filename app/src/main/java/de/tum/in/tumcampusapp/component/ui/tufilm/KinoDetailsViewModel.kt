package de.tum.`in`.tumcampusapp.component.ui.tufilm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class KinoDetailsViewModel @Inject constructor(
        private val localRepository: KinoLocalRepository,
        private val eventsRemoteRepository: EventsRemoteRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _kino = MutableLiveData<Kino>()
    val kino: LiveData<Kino> = _kino

    private val _event= MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _aggregatedTicketStatus = MutableLiveData<TicketStatus?>()
    val aggregatedTicketStatus: LiveData<TicketStatus?> = _aggregatedTicketStatus

    fun fetchTicketCount(eventId: Int) {
        compositeDisposable += eventsRemoteRepository.fetchTicketStats(eventId)
                .subscribeOn(Schedulers.io())
                .doOnError(Utils::log)
                .subscribe(_aggregatedTicketStatus::postValue) {
                    _aggregatedTicketStatus.postValue(null)
                }
    }

    fun fetchKinoByPosition(position: Int) {
        compositeDisposable += localRepository.getKinoByPosition(position)
                .subscribeOn(Schedulers.io())
                .subscribe(_kino::postValue)
    }

    fun fetchEventByMovieId(movieId: String) {
        compositeDisposable += localRepository.getEventByMovieId(movieId)
                .subscribeOn(Schedulers.io())
                .subscribe(_event::postValue)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}
