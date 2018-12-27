package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.database.TcaDb
import io.reactivex.Observable
import javax.inject.Inject

class EventsLocalRepository @Inject constructor(
        private val database: TcaDb
) {

    fun storeEvents(events: List<Event>) {
        database.eventDao().insert(events)
    }

    fun setDismissed(id: Int) {
        database.eventDao().setDismissed(id)
    }

    fun getEvents(): Observable<List<Event>> = database.eventDao().allFutureEvents

    fun getBookedEvents(): Observable<List<Event>> = database.eventDao().allBookedEvents

    fun getEventById(id: Int): Event? = database.eventDao().getEventById(id)

    fun getNextEventWithoutMovie(): Event? = database.eventDao().nextEventWithoutMovie

    fun removePastEventsWithoutTicket() {
        database.eventDao().removePastEventsWithoutTicket()
    }

}
