package de.tum.`in`.tumcampusapp.component.ui.tufilm.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.model.Kino
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.tufilm.repository.KinoRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class KinoDetailsViewModel @Inject constructor(
        private val localRepository: KinoLocalRepository,
        private val remoteRepository: KinoRemoteRepository,
        private val eventsLocalRepository: EventsLocalRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _kino = MutableLiveData<Kino>()
    val kino: LiveData<Kino> = _kino

    private val _ticketCount = MediatorLiveData<Int>()
    val ticketCount: LiveData<Int> = _ticketCount

    private val _event= MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    init {
        _ticketCount.addSource(_event) {
            fetchTicketCount(it.id)
        }
    }

    fun fetchKino(movieId: String) {
        compositeDisposable += getKinoById(movieId)
                .subscribe(_kino::postValue)
    }

    private fun getKinoById(id: String): Flowable<Kino> =
            localRepository.getKinoById(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun fetchEventByMovieId(movieId: String) {
        compositeDisposable += getEventByMovieId(movieId)
                .subscribe(_event::postValue)
    }

    private fun getEventByMovieId(movieId: String): Flowable<Event> =
            localRepository.getEventByMovieId(movieId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun fetchTicketCount(eventId: Int) {
        remoteRepository.fetchAvailableTicketCount(eventId, object : Callback<List<TicketStatus>> {

            override fun onResponse(call: Call<List<TicketStatus>>, response: Response<List<TicketStatus>>) {
                val statuses = response.body() ?: return
                val sum = statuses.sumBy { it.availableTicketCount }
                _ticketCount.postValue(sum)
            }

            override fun onFailure(call: Call<List<TicketStatus>>, t: Throwable) {
                Utils.log(t)
            }

        })
    }

    fun isEventBooked(event: Event) = eventsLocalRepository.isEventBooked(event)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}
