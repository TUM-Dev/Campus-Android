package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType

class EventsViewModel(
        private val controller: EventsController,
        private val type: EventType
) : ViewModel() {

    val events: LiveData<List<Event>>
        get() = when (type) {
            EventType.ALL -> controller.events
            else -> controller.bookedEvents
        }

    class Factory(
            private val controller: EventsController,
            private val eventType: EventType
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST") // no good way around this
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EventsViewModel(controller, eventType) as T
        }

    }

}
