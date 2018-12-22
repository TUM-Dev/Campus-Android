package de.tum.`in`.tumcampusapp.component.ui.ticket

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket

data class EventsViewState(
        val events: List<Event> = emptyList(),
        val tickets: List<Ticket> = emptyList(),
        val errorResId: Int? = null,
        val isLoading: Boolean = false
) {

    fun toEventsLoaded(events: List<Event>): EventsViewState {
        return copy(events = events)
    }

    fun toTicketsLoaded(tickets: List<Ticket>): EventsViewState {
        return copy(tickets = tickets)
    }

    fun toError(): EventsViewState {
        return copy(errorResId = R.string.error_something_wrong)
    }

    fun toData(): EventsViewState {
        return copy(errorResId = null)
    }

}
