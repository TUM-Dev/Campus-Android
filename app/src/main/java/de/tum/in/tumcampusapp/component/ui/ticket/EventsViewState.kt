package de.tum.`in`.tumcampusapp.component.ui.ticket

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event

data class EventsViewState(
        val events: List<Event> = emptyList(),
        val errorResId: Int? = null,
        val isLoading: Boolean = false
) {

    fun toLoading(): EventsViewState {
        return copy(isLoading = true)
    }

    fun toEventsLoaded(events: List<Event>): EventsViewState {
        return copy(events = events, isLoading = false)
    }

    fun toError(): EventsViewState {
        return copy(errorResId = R.string.error_something_wrong, isLoading = false)
    }

    fun toNoError(): EventsViewState {
        return copy(errorResId = null)
    }

    companion object {
        fun initial() = EventsViewState(isLoading = true)
    }

}
