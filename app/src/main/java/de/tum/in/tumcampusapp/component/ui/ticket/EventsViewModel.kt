package de.tum.`in`.tumcampusapp.component.ui.ticket

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.utils.NonNullLiveData
import org.jetbrains.anko.doAsync

class EventsViewModel(
        application: Application,
        private val type: EventType
) : AndroidViewModel(application) {

    private val controller = EventsController(application.applicationContext)

    val events: NonNullLiveData<List<Event>>
        get() {
            val data = NonNullLiveData<List<Event>>()

            doAsync {
                val events = when (type) {
                    EventType.ALL -> controller.events
                    else -> controller.bookedEvents
                }
                data.postValue(events.sorted())
            }

            return data
        }

    class Factory(
            private val application: Application,
            private val eventType: EventType
    ) : ViewModelProvider.AndroidViewModelFactory(application) {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EventsViewModel(application, eventType) as T
        }

    }

}
