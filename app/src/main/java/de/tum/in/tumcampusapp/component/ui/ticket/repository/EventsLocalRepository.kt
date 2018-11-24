package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.database.TcaDb
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

    fun getEvents(): LiveData<List<Event>> {
        return database.eventDao().allFutureEvents
    }

    /**
     * @return all events for which a ticket exists
     */
    fun getBookedEvents(): MediatorLiveData<List<Event>> {
        val tickets = database.ticketDao().all
        val events = MediatorLiveData<List<Event>>()

        events.addSource<List<Ticket>>(tickets) { newTickets ->
            val bookedEvents = newTickets.mapNotNull { getEventById(it.eventId) }
            events.setValue(bookedEvents)
        }

        return events
    }

    fun isEventBooked(event: Event): Boolean {
        val ticket = database.ticketDao().getByEventId(event.id)
        return ticket != null
    }

    fun getEventById(id: Int): Event? {
        return database.eventDao().getEventById(id)
    }

    fun removePastEventsWithoutTicket() {
        database.eventDao().removePastEventsWithoutTicket()
    }

}
