package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Event
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class EventsRemoteRepository @Inject constructor(
        private val context: Context,
        private val tumCabeClient: TUMCabeClient,
        private val eventsLocalRepository: EventsLocalRepository,
        private val ticketsLocalRepository: TicketsLocalRepository,
        private val ticketsRemoteRepository: TicketsRemoteRepository
) {

    fun downloadFromService() {
        val eventCallback = object : Callback<List<Event>> {

            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                val events = response.body() ?: return
                eventsLocalRepository.storeEvents(events)
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Utils.log(t)
            }
        }

        val ticketCallback = object : Callback<List<Ticket>> {
            override fun onResponse(call: Call<List<Ticket>>, response: Response<List<Ticket>>) {
                val tickets = response.body() ?: return
                ticketsLocalRepository.insert(*tickets.toTypedArray())
                ticketsRemoteRepository.fetchTicketTypesForTickets(tickets)
            }

            override fun onFailure(call: Call<List<Ticket>>, t: Throwable) {
                Utils.log(t)
            }
        }

        fetchEventsAndTickets(eventCallback, ticketCallback)
    }

    // TODO(thellmund) Without callback, use uni-directional data flow
    private fun fetchEventsAndTickets(
            eventCallback: Callback<List<Event>>,
            ticketCallback: Callback<List<Ticket>>
    ) {
        eventsLocalRepository.removePastEventsWithoutTicket()

        // Load all events and store them in the cache
        tumCabeClient.fetchEvents(eventCallback)

        // Load all tickets
        try {
            if (Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember::class.java) != null) {
                tumCabeClient.fetchTickets(context, ticketCallback)
            }
        } catch (e: NoPrivateKey) {
            Utils.log(e)
        }
    }

    fun fetchEvents(callback: Callback<List<Event>>) {
        eventsLocalRepository.removePastEventsWithoutTicket()
        tumCabeClient.fetchEvents(callback)
    }

}
