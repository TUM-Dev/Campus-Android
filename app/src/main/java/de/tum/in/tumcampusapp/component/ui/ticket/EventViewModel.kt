package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.arch.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for events.
 */
class EventViewModel(private val localRepository: EventLocalRepository,
                     private val remoteRepository: EventRemoteRepository,
                     private val compositeDisposable: CompositeDisposable) : ViewModel() {

    /**
     * Get all events from database
     */
    fun getAllEvents(): Flowable<List<Event>> =
            EventLocalRepository.getAllEvents()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .defaultIfEmpty(emptyList())

    /**
     * Get a event by its position (id)
     */
    fun getEventByPosition(position: Int): Flowable<Event> =
            EventLocalRepository.getEventByPosition(position)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    /**
     * Downloads events and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new events
     * Lastly updates last sync
     *
     */
    fun getEventsFromService(force: Boolean): Boolean =
            compositeDisposable.add(
                    Observable
                            .create<List<Event>> {
                                // TODO: add call to GET api/event/list/[i:earliest]/[i:latest]
                                /*
                                val latestId = EventLocalRepository.getLatestId()
                                if(latestId != null){
                                    remoteRepository.getAllEvents(latestId)
                                } else {
                                    remoteRepository.getAllEvents()
                                }*/
                                remoteRepository.getAllEvents()

                            }
                            .filter { localRepository.getLastSync() == null || force }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext { localRepository.clear() }
                            .doAfterNext { localRepository.updateLastSync() }
                            .doOnError { Utils.log(it) }
                            .flatMapIterable { it }
                            .filter { it.isFutureEvent() }
                            .subscribe { localRepository.addEvent(it) }
            )

    fun getPosition(date: String) = localRepository.getPosition(date)

}