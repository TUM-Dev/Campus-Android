package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository

class EventsViewModel(
        private val localRepository: EventsLocalRepository,
        private val type: EventType
) : ViewModel() {

    val events: LiveData<List<Event>>
        get() = when (type) {
            EventType.ALL -> localRepository.getEvents()
            else -> localRepository.getBookedEvents()
        }

    class Factory(
            private val localRepository: EventsLocalRepository,
            private val eventType: EventType
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST") // no good way around this
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EventsViewModel(localRepository, eventType) as T
        }

    }

}
