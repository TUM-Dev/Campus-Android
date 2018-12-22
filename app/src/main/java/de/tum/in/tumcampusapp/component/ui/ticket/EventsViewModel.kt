package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class Action {
    data class Refresh(val isLoggedIn: Boolean) : Action()
}

sealed class Result {
    data class EventsLoaded(val events: List<Event>) : Result()
    data class TicketsLoaded(val tickets: List<Ticket>) : Result()
    object ShowError : Result()
    object HideError : Result()
    object None : Result()
}

class EventsViewModel @Inject constructor(
        private val eventsLocalRepository: EventsLocalRepository,
        private val eventsRemoteRepository: EventsRemoteRepository,
        private val ticketsLocalRepository: TicketsLocalRepository,
        private val ticketsRemoteRepository: TicketsRemoteRepository,
        eventType: EventType
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val refreshRelay = PublishRelay.create<Action>()

    private val _viewState = MutableLiveData<EventsViewState>()
    val viewState: LiveData<EventsViewState> = _viewState

    init {
        val initialViewState = EventsViewState(isLoading = true)

        val eventsChanges = when (eventType) {
            EventType.ALL -> eventsLocalRepository.getEvents()
            EventType.BOOKED -> eventsLocalRepository.getBookedEvents()
        }
        val ticketsChanges = ticketsLocalRepository.getAll()

        val databaseChanges = Observable.merge(
                eventsChanges.map { Result.EventsLoaded(it) },
                ticketsChanges.map { Result.TicketsLoaded(it) }
        )

        val actions = refreshRelay.flatMap(this::processAction)

        compositeDisposable += Observable.merge(databaseChanges, actions)
                .scan(initialViewState, this::reduceState)
                .distinctUntilChanged()
                .subscribe(this::render)
    }

    // TODO: When to hide loading indicator?

    private fun processAction(action: Action): Observable<Result> {
        return when (action) {
            is Action.Refresh -> fetchEventsAndTickets(action.isLoggedIn)
        }
    }

    private fun reduceState(viewState: EventsViewState, result: Result): EventsViewState {
        return when (result) {
            is Result.EventsLoaded -> viewState.toEventsLoaded(result.events)
            is Result.TicketsLoaded -> viewState.toTicketsLoaded(result.tickets)
            Result.ShowError -> viewState.toError()
            Result.HideError -> viewState.toData()
            Result.None -> viewState // no changes, we return the current view state
        }
    }

    private fun fetchEventsAndTickets(isLoggedIn: Boolean): Observable<Result> {
        val eventsStream = loadAndStoreEvents()
                .andThen(Observable.just(Result.None as Result))
                .onErrorResumeNext { t: Throwable -> showErrorWithTimer() }

        val ticketsStream = if (isLoggedIn) {
            loadAndStoreTickets()
                    .andThen(Observable.just(Result.None as Result))
                    .onErrorResumeNext { t: Throwable -> showErrorWithTimer() }
        } else {
            Observable.empty()
        }

        return Observable.merge(eventsStream, ticketsStream)
    }

    private fun showErrorWithTimer(): Observable<Result> {
        return Observable.timer(4, TimeUnit.SECONDS, Schedulers.computation())
                .map { Result.HideError as Result }
                .startWith(Result.ShowError)
    }

    private fun loadAndStoreEvents(): Completable {
        return Observable
                .fromCallable { eventsLocalRepository.removePastEventsWithoutTicket() }
                .subscribeOn(Schedulers.io())
                .flatMap { eventsRemoteRepository.fetchEvents() }
                .subscribeOn(Schedulers.io())
                //.map { Result.EventsLoaded(it) }
                .flatMapCompletable {
                    eventsLocalRepository.storeEvents(it)
                    Completable.complete()
                }
    }

    private fun loadAndStoreTickets(): Completable {
        return ticketsRemoteRepository.fetchTickets()
                .subscribeOn(Schedulers.io())
                .doOnNext { loadAndStoreTicketTypes(it) }
                .flatMapCompletable {
                    ticketsLocalRepository.storeTickets(it)
                    Completable.complete()
                }
    }

    private fun loadAndStoreTicketTypes(tickets: List<Ticket>) {
        compositeDisposable += ticketsRemoteRepository.fetchTicketTypesForTickets(tickets)
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun refreshEventsAndTickets(isLoggedIn: Boolean) {
        refreshRelay.accept(Action.Refresh(isLoggedIn))
    }

    private fun render(viewState: EventsViewState) {
        _viewState.postValue(viewState)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}
