package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import android.content.Context
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.Ticket
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Call
import javax.inject.Inject

class TicketsRemoteRepository @Inject constructor(
        private val context: Context,
        private val tumCabeClient: TUMCabeClient,
        private val ticketsLocalRepository: TicketsLocalRepository
) {

    fun fetchTickets(): Call<List<Ticket>> = tumCabeClient.fetchTickets(context)
    fun fetchTicketsRx(): Observable<List<Ticket>> = tumCabeClient.fetchTicketsRx(context)

    @Throws(NoPrivateKey::class)
    fun fetchTicket(ticketId: Int): Call<Ticket> {
        return tumCabeClient.fetchTicket(context, ticketId)
    }

    fun fetchTicketTypesForTickets(tickets: List<Ticket>): Completable {
        val sources = tickets.map { fetchTicketTypesForEvent(it.eventId) }
        return Observable
                .merge(sources)
                .ignoreElements()
    }

    fun fetchTicketTypesForEvent(eventId: Int): Observable<List<TicketType>> {
        return tumCabeClient.fetchTicketTypes(eventId)
                .doOnNext(ticketsLocalRepository::addTicketTypes)
    }

}
