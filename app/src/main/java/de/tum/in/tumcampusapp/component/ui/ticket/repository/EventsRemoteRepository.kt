package de.tum.`in`.tumcampusapp.component.ui.ticket.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.ticket.payload.TicketStatus
import io.reactivex.Single
import javax.inject.Inject

class EventsRemoteRepository @Inject constructor(
    private val tumCabeClient: TUMCabeClient
) {

    fun fetchTicketStats(eventId: Int): Single<TicketStatus> {
        return tumCabeClient.fetchTicketStats(eventId)
            .map { it.reduceRight { s, a -> TicketStatus(-1, a.contingent + s.contingent, a.sold + s.sold) } }
    }
}
