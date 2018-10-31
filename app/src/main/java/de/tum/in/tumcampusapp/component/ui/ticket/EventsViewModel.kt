package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType

class EventsViewModel(
        application: Application,
        private val type: EventType
) : AndroidViewModel(application) {

    private val controller = EventsController(application.applicationContext)

    val events: LiveData<List<Event>>
        get() = when (type) {
            EventType.ALL -> controller.events
            else -> controller.bookedEvents
        }

    class Factory(
            private val application: Application,
            private val eventType: EventType
    ) : ViewModelProvider.AndroidViewModelFactory(application) {

        @Suppress("UNCHECKED_CAST") // no good way around this
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EventsViewModel(application, eventType) as T
        }

    }

}
