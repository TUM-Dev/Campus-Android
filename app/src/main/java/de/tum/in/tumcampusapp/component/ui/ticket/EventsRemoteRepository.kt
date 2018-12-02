package de.tum.`in`.tumcampusapp.component.ui.ticket

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import io.reactivex.Single

class EventsRemoteRepository(
        private val tumCabeClient: TUMCabeClient
) {

    fun fetchTicketStats(eventId: Int): Single<Int> {
        return tumCabeClient.fetchTicketStats(eventId)
                .map { it.sumBy { status -> status.availableTicketCount } }
    }

}
