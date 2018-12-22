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

    fun getEvents(): Observable<List<Event>> {
        return database.eventDao().allFutureEvents
    }

    /**
     * @return all events for which a ticket exists
     */
    fun getBookedEvents(): Observable<List<Event>> {
        return database.ticketDao().all
                .map { it.mapNotNull { ticket -> getEventById(ticket.eventId) } }
    }

    fun getEventById(id: Int): Event? {
        return database.eventDao().getEventById(id)
    }

    fun getNextEventWithoutMovie(): Event? {
        return database.eventDao().nextEventWithoutMovie
    }

    fun removePastEventsWithoutTicket() {
        database.eventDao().removePastEventsWithoutTicket()
    }

}
