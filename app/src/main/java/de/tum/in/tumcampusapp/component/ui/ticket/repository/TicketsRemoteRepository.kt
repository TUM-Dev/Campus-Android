package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.model.TicketType
import io.reactivex.Observable
import javax.inject.Inject

class TicketsRemoteRepository @Inject constructor(
    private val tumCabeClient: TUMCabeClient,
    private val ticketsLocalRepository: TicketsLocalRepository
) {

    fun fetchTicketTypesForEvent(eventId: Int): Observable<List<TicketType>> {
        return tumCabeClient.fetchTicketTypes(eventId)
            .doOnNext(ticketsLocalRepository::addTicketTypes)
    }
}
