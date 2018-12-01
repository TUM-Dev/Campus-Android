package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class TicketsRemoteRepository @Inject constructor(
        private val context: Context,
        private val tumCabeClient: TUMCabeClient,
        private val ticketsLocalRepository: TicketsLocalRepository
) {

    fun fetchTickets(callback: Callback<List<Ticket>>) {
        // TODO(thellmund) Should not pass Context here, but TUMCabeVerification object (create in AppModule?)
        tumCabeClient.fetchTickets(context, callback)
    }

    fun fetchTicketTypesForTickets(tickets: List<Ticket>) {
        tickets.forEach { fetchTicketTypesForTicket(it) }
    }

    private fun fetchTicketTypesForTicket(ticket: Ticket) {
        tumCabeClient.fetchTicketTypes(ticket.eventId, object : Callback<List<TicketType>> {

            override fun onResponse(call: Call<List<TicketType>>,
                                    response: Response<List<TicketType>>) {
                val ticketTypes = response.body() ?: return
                ticketsLocalRepository.addTicketTypes(ticketTypes)
            }

            override fun onFailure(call: Call<List<TicketType>>, t: Throwable) {
                // if ticketTypes could not be retrieved from server, e.g. due to network problems
                Utils.log(t)
            }

        })
    }

}
