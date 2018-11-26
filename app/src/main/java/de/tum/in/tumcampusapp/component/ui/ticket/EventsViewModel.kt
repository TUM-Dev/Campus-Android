package de.tum.`in`.tumcampusapp.component.ui.ticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.EventType
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.EventsRemoteRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.ticket.repository.TicketsRemoteRepository
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class EventsViewModel @Inject constructor(
        private val eventsLocalRepository: EventsLocalRepository,
        private val eventsRemoteRepository: EventsRemoteRepository,
        private val ticketsLocalRepository: TicketsLocalRepository,
        private val ticketsRemoteRepository: TicketsRemoteRepository,
        private val eventType: EventType
) : ViewModel() {

    val events: LiveData<List<Event>>
        get() = when (eventType) {
            EventType.ALL -> eventsLocalRepository.getEvents()
            EventType.BOOKED -> eventsLocalRepository.getBookedEvents()
        }

    val tickets: LiveData<List<Ticket>>
        get() = ticketsLocalRepository.getAll()

    private val _error = MutableLiveData<Int?>()
    val error: LiveData<Int?> = _error

    fun fetchEventsAndTickets(isLoggedIn: Boolean) {
        // TODO: Inject AppConfig, a wrapper around SharedPreferences
        fetchEvents()

        if (isLoggedIn) {
            fetchTickets()
        }
    }

    fun showError(errorMessageResId: Int) {
        _error.postValue(errorMessageResId)
        Timer().schedule(4000) {
            // We need to hide the Toast again. Otherwise, it will reappear when the user
            // rotates the device.
            _error.postValue(null)
        }
    }

    private fun fetchEvents() {
        eventsLocalRepository.removePastEventsWithoutTicket()
        eventsRemoteRepository.fetchEvents(object : Callback<List<Event>> {

            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                val events = response.body()
                if (response.isSuccessful && events != null) {
                    eventsLocalRepository.storeEvents(events)
                } else {
                    _error.postValue(R.string.error_something_wrong)
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Utils.log(t)
                _error.postValue(R.string.error_something_wrong)
            }

        })
    }

    private fun fetchTickets() {
        ticketsRemoteRepository.fetchTickets(object : Callback<List<Ticket>> {

            override fun onResponse(call: Call<List<Ticket>>, response: Response<List<Ticket>>) {
                val tickets = response.body()
                if (response.isSuccessful && tickets != null) {
                    ticketsLocalRepository.storeTickets(tickets)
                    ticketsRemoteRepository.fetchTicketTypesForTickets(tickets)
                } else {
                    _error.postValue(R.string.error_something_wrong)
                }
            }

            override fun onFailure(call: Call<List<Ticket>>, t: Throwable) {
                Utils.log(t)
                _error.postValue(R.string.error_something_wrong)
            }

        })
    }

}
